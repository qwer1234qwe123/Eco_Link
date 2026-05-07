"""
generate_mock_data.py
EcoLink - 가상 센서 데이터 생성 스크립트
실제 센서 데이터가 없을 때 AI 모델 학습용 데이터를 생성합니다.
"""

import random
import pandas as pd
from datetime import datetime, timedelta
import os

# ────────────────────────────────────────────
# 설정값
# ────────────────────────────────────────────
NUM_CANS = 10          # 쓰레기통 수
DAYS = 60              # 생성할 날짜 수
INTERVAL_HOURS = 1     # 센서 측정 간격 (시간)
FILL_THRESHOLD = 80.0  # 포화 기준 (%)
OUTPUT_DIR = "data"    # 저장 폴더


def generate_sensor_log(can_id: int, days: int = DAYS) -> pd.DataFrame:
    """
    특정 쓰레기통의 sensor_log 데이터를 생성합니다.
    - 시간이 지날수록 fill_level이 증가
    - 80% 이상이 되면 비움 이벤트 발생 (fill_level 초기화)
    """
    records = []
    base_time = datetime.now() - timedelta(days=days)

    fill_level = random.uniform(5, 25)      # 초기 적재율
    battery = random.uniform(80, 100)       # 초기 배터리

    total_hours = days * 24 // INTERVAL_HOURS

    for i in range(total_hours):
        log_time = base_time + timedelta(hours=i * INTERVAL_HOURS)

        # 시간대별 쓰레기 증가 속도 (출근/점심/퇴근 시간에 빠름)
        hour = log_time.hour
        if 8 <= hour <= 10 or 12 <= hour <= 14 or 17 <= hour <= 19:
            fill_increase = random.uniform(2.0, 5.0)
        elif 0 <= hour <= 5:
            fill_increase = random.uniform(0.0, 0.5)
        else:
            fill_increase = random.uniform(0.5, 2.0)

        fill_level = min(fill_level + fill_increase, 100.0)
        battery = max(battery - random.uniform(0.01, 0.05), 0.0)
        overflow = fill_level >= 95.0

        records.append({
            "can_id": can_id,
            "fill_level": round(fill_level, 2),
            "battery_level": round(battery, 2),
            "overflow_flag": overflow,
            "log_time": log_time
        })

        # 80% 이상이면 비워짐 처리
        if fill_level >= FILL_THRESHOLD:
            fill_level = random.uniform(3, 10)

    return pd.DataFrame(records)


def generate_empty_history(sensor_df: pd.DataFrame) -> pd.DataFrame:
    """
    sensor_log에서 비움 이벤트가 발생한 시점을 추출해
    empty_history 데이터를 생성합니다.
    """
    records = []

    for can_id in sensor_df["can_id"].unique():
        can_data = sensor_df[sensor_df["can_id"] == can_id].reset_index(drop=True)

        for i in range(1, len(can_data)):
            prev = can_data.loc[i - 1, "fill_level"]
            curr = can_data.loc[i, "fill_level"]

            # 이전보다 급격히 줄어들면 → 비움 이벤트
            if prev - curr > 30:
                records.append({
                    "can_id": can_id,
                    "before_level": round(prev, 2),
                    "after_level": round(curr, 2),
                    "emptied_at": can_data.loc[i, "log_time"],
                    "note": "mock_auto_empty"
                })

    return pd.DataFrame(records)


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    all_sensor_logs = []
    all_empty_histories = []

    print(f"[EcoLink] 쓰레기통 {NUM_CANS}개 × {DAYS}일 데이터 생성 중...")

    for can_id in range(1, NUM_CANS + 1):
        sensor_df = generate_sensor_log(can_id, days=DAYS)
        empty_df = generate_empty_history(sensor_df)

        all_sensor_logs.append(sensor_df)
        all_empty_histories.append(empty_df)

        print(f"  → can_id={can_id} 완료 | 센서 로그: {len(sensor_df)}건 | 비움 기록: {len(empty_df)}건")

    sensor_log_df = pd.concat(all_sensor_logs, ignore_index=True)
    empty_history_df = pd.concat(all_empty_histories, ignore_index=True)

    sensor_log_df.to_csv(f"{OUTPUT_DIR}/sensor_log.csv", index=False, encoding="utf-8-sig")
    empty_history_df.to_csv(f"{OUTPUT_DIR}/empty_history.csv", index=False, encoding="utf-8-sig")

    print(f"\n[완료] 데이터 저장 위치: ./{OUTPUT_DIR}/")
    print(f"  - sensor_log.csv     : {len(sensor_log_df)}행")
    print(f"  - empty_history.csv  : {len(empty_history_df)}행")


if __name__ == "__main__":
    main()
