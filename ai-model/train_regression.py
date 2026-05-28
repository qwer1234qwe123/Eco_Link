"""
train_regression.py
EcoLink - 회귀 모델 학습 스크립트 (DB 연동 버전)
MariaDB sensor_log + empty_history → RandomForestRegressor 학습 → regression_model.pkl 저장
목표: 현재 상태에서 몇 시간 후에 80%에 도달하는지 예측
"""

import pandas as pd
import numpy as np
import pickle
import pymysql
import os
from dotenv import load_dotenv
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.preprocessing import StandardScaler

# ────────────────────────────────────────────
# .env 파일 로드
# ────────────────────────────────────────────
load_dotenv()

DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": int(os.getenv("DB_PORT", 3306)),
    "user": os.getenv("DB_USER", "root"),
    "password": os.getenv("DB_PASSWORD"),
    "database": os.getenv("DB_NAME", "bingo"),
    "charset": "utf8mb4"
}

MODEL_PATH = "regression_model.pkl"
SCALER_PATH = "regression_scaler.pkl"
FILL_THRESHOLD = 80.0

FEATURE_COLS = [
    "fill_level",
    "fill_rate",
    "hours_since_empty",
    "hour_of_day",
    "battery_level"
]
LABEL_COL = "hours_until_full"


def load_from_db() -> tuple:
    """MariaDB에서 sensor_log, empty_history 데이터 로드"""
    print("[EcoLink] DB 연결 중...")
    conn = pymysql.connect(**DB_CONFIG)

    sensor_df = pd.read_sql("""
        SELECT s.id, s.can_id, s.fill_level, s.battery_level, s.log_time
        FROM sensor_log s
        ORDER BY s.can_id, s.log_time
    """, conn, parse_dates=["log_time"])

    empty_df = pd.read_sql("""
        SELECT id, can_id, before_level, after_level, emptied_at
        FROM empty_history
        ORDER BY can_id, emptied_at
    """, conn, parse_dates=["emptied_at"])

    conn.close()

    print(f"  → sensor_log: {len(sensor_df)}개")
    print(f"  → empty_history: {len(empty_df)}개")
    return sensor_df, empty_df


def get_hours_since_empty(can_id: int, current_time, empty_df: pd.DataFrame) -> float:
    """마지막 비움 이후 경과 시간 계산"""
    past = empty_df[
        (empty_df["can_id"] == can_id) &
        (empty_df["emptied_at"] < current_time)
    ]
    if past.empty:
        return 999.0
    last_empty = past["emptied_at"].max()
    return round((current_time - last_empty).total_seconds() / 3600, 2)


def compute_fill_rate(can_data: pd.DataFrame, idx: int, window: int = 3) -> float:
    """시간당 평균 증가 속도 계산"""
    start = max(0, idx - window)
    w = can_data.iloc[start:idx + 1]
    if len(w) < 2:
        return 0.0
    delta = w["fill_level"].iloc[-1] - w["fill_level"].iloc[0]
    return round(max(0, delta / (len(w) - 1)), 4)


def build_regression_features(sensor_df: pd.DataFrame,
                               empty_df: pd.DataFrame) -> pd.DataFrame:
    """회귀 모델용 Feature + Label 생성"""
    records = []

    for can_id in sensor_df["can_id"].unique():
        can_data = sensor_df[sensor_df["can_id"] == can_id].reset_index(drop=True)

        for i in range(3, len(can_data) - 1):
            row = can_data.iloc[i]
            current_fill = row["fill_level"]
            current_time = row["log_time"]

            if current_fill >= FILL_THRESHOLD:
                hours_until_full = 0.0
            else:
                future_data = can_data.iloc[i + 1:]
                full_rows = future_data[future_data["fill_level"] >= FILL_THRESHOLD]

                if full_rows.empty:
                    continue

                full_time = full_rows.iloc[0]["log_time"]
                hours_until_full = (full_time - current_time).total_seconds() / 3600
                hours_until_full = round(hours_until_full, 2)

                if hours_until_full > 24:
                    continue

            fill_rate = compute_fill_rate(can_data, i)
            hours_since_empty = get_hours_since_empty(can_id, current_time, empty_df)

            records.append({
                "fill_level": current_fill,
                "fill_rate": fill_rate,
                "hours_since_empty": hours_since_empty,
                "hour_of_day": current_time.hour,
                "battery_level": row["battery_level"],
                LABEL_COL: hours_until_full
            })

    return pd.DataFrame(records)


def train(df: pd.DataFrame):
    X = df[FEATURE_COLS]
    y = df[LABEL_COL]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    print("[EcoLink] 회귀 모델 학습 시작...")
    model = RandomForestRegressor(
        n_estimators=100,
        max_depth=10,
        random_state=42
    )
    model.fit(X_train_scaled, y_train)

    y_pred = model.predict(X_test_scaled)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    print(f"\n[평가 결과]")
    print(f"  평균 오차 (MAE): {mae:.2f}시간")
    print(f"  R² 점수: {r2:.4f} (1.0에 가까울수록 정확)")

    importances = pd.Series(model.feature_importances_, index=FEATURE_COLS)
    print("\n[Feature 중요도]")
    for feat, imp in importances.sort_values(ascending=False).items():
        print(f"  {feat:<22}: {imp:.4f}")

    return model, scaler


def save_model(model, scaler):
    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model, f)
    with open(SCALER_PATH, "wb") as f:
        pickle.dump(scaler, f)
    print(f"\n[완료] 회귀 모델 저장: {MODEL_PATH}")
    print(f"[완료] 스케일러 저장: {SCALER_PATH}")


def main():
    sensor_df, empty_df = load_from_db()

    print("[EcoLink] 회귀 Feature 생성 중...")
    df = build_regression_features(sensor_df, empty_df)

    if len(df) < 100:
        print(f"[경고] 학습 데이터가 너무 적습니다: {len(df)}개 (최소 100개 필요)")
        return

    print(f"[EcoLink] 학습 데이터: {len(df)}개 샘플")

    model, scaler = train(df)
    save_model(model, scaler)


if __name__ == "__main__":
    main()