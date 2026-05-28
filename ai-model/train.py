"""
train.py
EcoLink - AI 분류 모델 학습 스크립트 (DB 연동 버전)
MariaDB sensor_log + empty_history → RandomForestClassifier 학습 → model.pkl 저장
목표: 오늘 오후 6시 이전에 80% 이상 찰지 예측 (Yes/No)
"""

import pandas as pd
import numpy as np
import pickle
import pymysql
import os
from dotenv import load_dotenv
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score
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

MODEL_PATH = "model.pkl"
SCALER_PATH = "scaler.pkl"
TARGET_HOUR = 18
FILL_THRESHOLD = 80.0

FEATURE_COLS = [
    "fill_level",
    "fill_rate",
    "hours_since_empty",
    "hour_of_day",
    "battery_level"
]
LABEL_COL = "needs_collection"


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
    hours = (current_time - last_empty).total_seconds() / 3600
    return round(hours, 2)


def compute_fill_rate(can_data: pd.DataFrame, idx: int, window: int = 3) -> float:
    """시간당 평균 증가 속도 계산"""
    start = max(0, idx - window)
    w = can_data.iloc[start:idx + 1]
    if len(w) < 2:
        return 0.0
    delta = w["fill_level"].iloc[-1] - w["fill_level"].iloc[0]
    return round(max(0, delta / (len(w) - 1)), 4)


def will_exceed_before_6pm(fill_level: float, fill_rate: float, current_time) -> int:
    """오늘 오후 6시 전에 80% 초과 여부 판단"""
    today_6pm = current_time.replace(hour=TARGET_HOUR, minute=0, second=0, microsecond=0)
    if current_time >= today_6pm:
        return 0
    if fill_level >= FILL_THRESHOLD:
        return 1
    hours_left = (today_6pm - current_time).total_seconds() / 3600
    predicted = fill_level + (fill_rate * hours_left)
    return 1 if predicted >= FILL_THRESHOLD else 0


def build_features(sensor_df: pd.DataFrame, empty_df: pd.DataFrame) -> pd.DataFrame:
    """Feature + Label 생성"""
    records = []

    for can_id in sensor_df["can_id"].unique():
        can_data = sensor_df[sensor_df["can_id"] == can_id].reset_index(drop=True)

        for i in range(3, len(can_data)):
            row = can_data.iloc[i]
            current_time = row["log_time"]

            fill_rate = compute_fill_rate(can_data, i)
            hours_since_empty = get_hours_since_empty(can_id, current_time, empty_df)
            label = will_exceed_before_6pm(row["fill_level"], fill_rate, current_time)

            records.append({
                "fill_level": row["fill_level"],
                "fill_rate": fill_rate,
                "hours_since_empty": hours_since_empty,
                "hour_of_day": current_time.hour,
                "battery_level": row["battery_level"],
                LABEL_COL: label
            })

    return pd.DataFrame(records)


def train(df: pd.DataFrame):
    X = df[FEATURE_COLS]
    y = df[LABEL_COL]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    print("[EcoLink] 모델 학습 시작...")
    model = RandomForestClassifier(
        n_estimators=100,
        max_depth=10,
        random_state=42,
        class_weight="balanced"
    )
    model.fit(X_train_scaled, y_train)

    y_pred = model.predict(X_test_scaled)
    acc = accuracy_score(y_test, y_pred)

    print(f"\n[평가 결과]")
    print(f"  정확도(Accuracy): {acc * 100:.2f}%")
    print("\n[상세 리포트]")
    print(classification_report(y_test, y_pred,
                                target_names=["수거 불필요", "수거 필요"]))

    importances = pd.Series(model.feature_importances_, index=FEATURE_COLS)
    print("[Feature 중요도]")
    for feat, imp in importances.sort_values(ascending=False).items():
        print(f"  {feat:<22}: {imp:.4f}")

    return model, scaler


def save_model(model, scaler):
    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model, f)
    with open(SCALER_PATH, "wb") as f:
        pickle.dump(scaler, f)
    print(f"\n[완료] 모델 저장: {MODEL_PATH}")
    print(f"[완료] 스케일러 저장: {SCALER_PATH}")


def main():
    sensor_df, empty_df = load_from_db()

    print("[EcoLink] Feature 생성 중...")
    df = build_features(sensor_df, empty_df)

    if len(df) < 100:
        print(f"[경고] 학습 데이터가 너무 적습니다: {len(df)}개 (최소 100개 필요)")
        return

    print(f"[EcoLink] 학습 데이터: {len(df)}개 샘플")

    pos = df[LABEL_COL].sum()
    neg = len(df) - pos
    print(f"  → 수거 필요: {pos}개 ({pos/len(df)*100:.1f}%)")
    print(f"  → 수거 불필요: {neg}개 ({neg/len(df)*100:.1f}%)")

    model, scaler = train(df)
    save_model(model, scaler)


if __name__ == "__main__":
    main()