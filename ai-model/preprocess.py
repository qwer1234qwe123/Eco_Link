"""
preprocess.py
EcoLink - 센서 데이터 전처리 스크립트
sensor_log.csv → 모델 학습용 Feature/Label 생성
"""

import pandas as pd
import numpy as np
import os

FILL_THRESHOLD = 80.0   # 포화 기준 (%)
TARGET_HOUR = 18         # 오후 6시 기준
DATA_DIR = "data"


def load_data() -> tuple[pd.DataFrame, pd.DataFrame]:
    sensor_df = pd.read_csv(f"{DATA_DIR}/sensor_log.csv", parse_dates=["log_time"])
    empty_df = pd.read_csv(f"{DATA_DIR}/empty_history.csv", parse_dates=["emptied_at"])
    return sensor_df, empty_df


def get_last_empty_time(can_id: int, current_time: pd.Timestamp,
                        empty_df: pd.DataFrame) -> float:
    """
    특정 쓰레기통의 마지막 비움 이후 경과 시간(시간 단위)을 반환합니다.
    비움 기록이 없으면 999 반환
    """
    past_empties = empty_df[
        (empty_df["can_id"] == can_id) &
        (empty_df["emptied_at"] < current_time)
    ]
    if past_empties.empty:
        return 999.0
    last_empty = past_empties["emptied_at"].max()
    return round((current_time - last_empty).total_seconds() / 3600, 2)


def compute_fill_rate(can_data: pd.DataFrame, current_idx: int,
                      window: int = 3) -> float:
    """
    최근 window 시간 동안의 평균 fill_level 증가 속도를 계산합니다.
    """
    start = max(0, current_idx - window)
    window_data = can_data.iloc[start:current_idx + 1]
    if len(window_data) < 2:
        return 0.0
    delta_fill = window_data["fill_level"].iloc[-1] - window_data["fill_level"].iloc[0]
    delta_hours = len(window_data) - 1
    return round(delta_fill / delta_hours, 4) if delta_hours > 0 else 0.0


def will_exceed_threshold_before_6pm(can_data: pd.DataFrame,
                                     current_idx: int,
                                     current_time: pd.Timestamp,
                                     fill_rate: float) -> int:
    """
    현재 시간 이후 당일 오후 6시 이전에 fill_level이 80%를 초과하는지 예측합니다.
    Label: 1 (수거 필요), 0 (수거 불필요)
    """
    # 오늘 18시 이후거나 이미 포화 상태면 판단 불필요
    today_6pm = current_time.replace(hour=TARGET_HOUR, minute=0, second=0, microsecond=0)
    if current_time >= today_6pm:
        return 0

    current_fill = can_data.iloc[current_idx]["fill_level"]
    if current_fill >= FILL_THRESHOLD:
        return 1

    # 남은 시간 동안 선형 증가 예측
    hours_remaining = (today_6pm - current_time).total_seconds() / 3600
    predicted_fill = current_fill + (fill_rate * hours_remaining)
    return 1 if predicted_fill >= FILL_THRESHOLD else 0


def build_features(sensor_df: pd.DataFrame, empty_df: pd.DataFrame) -> pd.DataFrame:
    """
    sensor_log 데이터에서 모델 학습용 Feature와 Label을 생성합니다.

    Feature:
        - fill_level         : 현재 적재율 (%)
        - fill_rate          : 시간당 평균 증가 속도
        - hours_since_empty  : 마지막 비움 이후 경과 시간
        - hour_of_day        : 현재 시간대 (0~23)
        - battery_level      : 배터리 잔량

    Label:
        - needs_collection   : 오후 6시 전 80% 초과 여부 (0 or 1)
    """
    records = []

    for can_id in sensor_df["can_id"].unique():
        can_data = sensor_df[sensor_df["can_id"] == can_id].reset_index(drop=True)

        for i in range(3, len(can_data)):
            row = can_data.iloc[i]
            current_time = row["log_time"]

            fill_rate = compute_fill_rate(can_data, i)
            hours_since_empty = get_last_empty_time(can_id, current_time, empty_df)
            label = will_exceed_threshold_before_6pm(can_data, i, current_time, fill_rate)

            records.append({
                "can_id": can_id,
                "log_time": current_time,
                "fill_level": row["fill_level"],
                "fill_rate": fill_rate,
                "hours_since_empty": hours_since_empty,
                "hour_of_day": current_time.hour,
                "battery_level": row["battery_level"],
                "needs_collection": label
            })

    return pd.DataFrame(records)


def main():
    os.makedirs(DATA_DIR, exist_ok=True)

    print("[EcoLink] 데이터 로드 중...")
    sensor_df, empty_df = load_data()

    print("[EcoLink] Feature 생성 중...")
    feature_df = build_features(sensor_df, empty_df)

    output_path = f"{DATA_DIR}/features.csv"
    feature_df.to_csv(output_path, index=False, encoding="utf-8-sig")

    pos = feature_df["needs_collection"].sum()
    neg = len(feature_df) - pos

    print(f"\n[완료] 저장 위치: {output_path}")
    print(f"  - 전체 샘플 수   : {len(feature_df)}개")
    print(f"  - 수거 필요 (1)  : {pos}개 ({pos/len(feature_df)*100:.1f}%)")
    print(f"  - 수거 불필요 (0): {neg}개 ({neg/len(feature_df)*100:.1f}%)")


if __name__ == "__main__":
    main()
