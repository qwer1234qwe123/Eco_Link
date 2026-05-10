"""
train_regression.py
EcoLink - 회귀 모델 학습 스크립트
목표: 현재 상태에서 몇 시간 후에 80%에 도달하는지 예측
"""

import pandas as pd
import numpy as np
import pickle
import os
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.preprocessing import StandardScaler

DATA_DIR = "data"
MODEL_PATH = "regression_model.pkl"
SCALER_PATH = "regression_scaler.pkl"

FEATURE_COLS = [
    "fill_level",
    "fill_rate",
    "hours_since_empty",
    "hour_of_day",
    "battery_level"
]
LABEL_COL = "hours_until_full"  # 몇 시간 후 80% 도달하는지


def load_sensor_data() -> pd.DataFrame:
    path = f"{DATA_DIR}/sensor_log.csv"
    if not os.path.exists(path):
        raise FileNotFoundError(
            f"[오류] {path} 파일이 없습니다.\n"
            "먼저 generate_mock_data.py를 실행해 주세요."
        )
    return pd.read_csv(path, parse_dates=["log_time"])


def build_regression_features(sensor_df: pd.DataFrame) -> pd.DataFrame:
    """
    sensor_log 데이터에서 회귀 모델용 Feature와 Label 생성

    Label: hours_until_full
        - 현재 시점에서 몇 시간 후에 80%에 도달하는지
        - 이미 80% 이상이면 0
        - 앞으로도 80% 안 되면 999 (제외)
    """
    records = []

    for can_id in sensor_df["can_id"].unique():
        can_data = sensor_df[sensor_df["can_id"] == can_id].reset_index(drop=True)

        for i in range(3, len(can_data) - 1):
            row = can_data.iloc[i]
            current_fill = row["fill_level"]
            current_time = row["log_time"]

            # 현재 이미 포화 상태면 0
            if current_fill >= 80:
                hours_until_full = 0.0
            else:
                # 이후 데이터에서 처음으로 80% 넘는 시점 찾기
                future_data = can_data.iloc[i+1:]
                full_rows = future_data[future_data["fill_level"] >= 80]

                if full_rows.empty:
                    continue  # 80% 안 되는 경우 제외

                full_time = full_rows.iloc[0]["log_time"]
                hours_until_full = (full_time - current_time).total_seconds() / 3600
                hours_until_full = round(hours_until_full, 2)

                # 너무 먼 미래는 제외 (24시간 이상)
                if hours_until_full > 24:
                    continue

            # fill_rate 계산
            start = max(0, i - 3)
            window = can_data.iloc[start:i+1]
            if len(window) >= 2:
                delta_fill = window["fill_level"].iloc[-1] - window["fill_level"].iloc[0]
                fill_rate = max(0, delta_fill / (len(window) - 1))
            else:
                fill_rate = 0.0

            # hours_since_empty 계산
            prev_data = can_data.iloc[:i]
            empties = prev_data[prev_data["fill_level"] < prev_data["fill_level"].shift(1) - 30]
            if empties.empty:
                hours_since_empty = 999.0
            else:
                last_empty_time = empties.iloc[-1]["log_time"]
                hours_since_empty = round(
                    (current_time - last_empty_time).total_seconds() / 3600, 2
                )

            records.append({
                "can_id": can_id,
                "log_time": current_time,
                "fill_level": current_fill,
                "fill_rate": round(fill_rate, 4),
                "hours_since_empty": hours_since_empty,
                "hour_of_day": current_time.hour,
                "battery_level": row["battery_level"],
                "hours_until_full": hours_until_full
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

    print("\n[평가 결과]")
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
    print("[EcoLink] 센서 데이터 로드 중...")
    sensor_df = load_sensor_data()

    print("[EcoLink] 회귀 Feature 생성 중...")
    df = build_regression_features(sensor_df)

    os.makedirs(DATA_DIR, exist_ok=True)
    df.to_csv(f"{DATA_DIR}/regression_features.csv", index=False, encoding="utf-8-sig")

    print(f"[EcoLink] 학습 데이터: {len(df)}개 샘플")

    model, scaler = train(df)
    save_model(model, scaler)


if __name__ == "__main__":
    main()
