"""
train.py
EcoLink - AI 모델 학습 스크립트
features.csv → RandomForest 모델 학습 → model.pkl 저장
"""

import pandas as pd
import pickle
import os
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, accuracy_score
from sklearn.preprocessing import StandardScaler

DATA_DIR = "data"
MODEL_PATH = "model.pkl"
SCALER_PATH = "scaler.pkl"

# 학습에 사용할 Feature 컬럼
FEATURE_COLS = [
    "fill_level",
    "fill_rate",
    "hours_since_empty",
    "hour_of_day",
    "battery_level"
]
LABEL_COL = "needs_collection"


def load_features() -> pd.DataFrame:
    path = f"{DATA_DIR}/features.csv"
    if not os.path.exists(path):
        raise FileNotFoundError(
            f"[오류] {path} 파일이 없습니다.\n"
            "먼저 generate_mock_data.py → preprocess.py를 순서대로 실행해 주세요."
        )
    return pd.read_csv(path)


def train(df: pd.DataFrame):
    X = df[FEATURE_COLS]
    y = df[LABEL_COL]

    # 데이터 분할 (학습 80% / 테스트 20%)
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    # 정규화
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    # 모델 학습
    print("[EcoLink] 모델 학습 시작...")
    model = RandomForestClassifier(
        n_estimators=100,
        max_depth=10,
        random_state=42,
        class_weight="balanced"   # 수거 필요/불필요 불균형 대응
    )
    model.fit(X_train_scaled, y_train)

    # 평가
    y_pred = model.predict(X_test_scaled)
    acc = accuracy_score(y_test, y_pred)

    print("\n[평가 결과]")
    print(f"  정확도(Accuracy): {acc * 100:.2f}%")
    print("\n[상세 리포트]")
    print(classification_report(y_test, y_pred,
                                    target_names=["수거 불필요", "수거 필요"]))

    # Feature 중요도 출력
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
    df = load_features()
    print(f"[EcoLink] 학습 데이터 로드 완료: {len(df)}개 샘플")

    model, scaler = train(df)
    save_model(model, scaler)


if __name__ == "__main__":
    main()
