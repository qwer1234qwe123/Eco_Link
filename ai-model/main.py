"""
main.py
EcoLink - FastAPI 예측 서버
Spring Boot에서 호출하는 AI 예측 API 서버
"""

import pickle
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
import os

# ────────────────────────────────────────────
# 모델 & 스케일러 로드
# ────────────────────────────────────────────
MODEL_PATH = "model.pkl"
SCALER_PATH = "scaler.pkl"

if not os.path.exists(MODEL_PATH):
    raise RuntimeError(
        "[오류] model.pkl 파일이 없습니다.\n"
        "먼저 train.py를 실행해 주세요."
    )

with open(MODEL_PATH, "rb") as f:
    model = pickle.load(f)

with open(SCALER_PATH, "rb") as f:
    scaler = pickle.load(f)

# ────────────────────────────────────────────
# FastAPI 앱
# ────────────────────────────────────────────
app = FastAPI(
    title="EcoLink AI 예측 서버",
    description="쓰레기통 포화 시점 예측 API (오후 6시 이전 80% 도달 여부)",
    version="1.0.0"
)


# ────────────────────────────────────────────
# 요청 / 응답 스키마
# ────────────────────────────────────────────
class PredictRequest(BaseModel):
    can_id: int = Field(..., description="쓰레기통 ID")
    fill_level: float = Field(..., ge=0, le=100, description="현재 적재율 (%)")
    fill_rate: float = Field(..., description="시간당 평균 증가 속도")
    hours_since_empty: float = Field(..., description="마지막 비움 이후 경과 시간")
    hour_of_day: int = Field(..., ge=0, le=23, description="현재 시간대")
    battery_level: float = Field(..., ge=0, le=100, description="배터리 잔량 (%)")


class PredictResponse(BaseModel):
    can_id: int
    needs_collection: bool          # 수거 필요 여부
    confidence: float               # 예측 신뢰도 (0.0 ~ 1.0)
    predicted_status: str           # "수거 필요" or "정상"
    message: str


class BatchPredictRequest(BaseModel):
    items: list[PredictRequest]


# ────────────────────────────────────────────
# 예측 함수
# ────────────────────────────────────────────
def run_predict(req: PredictRequest) -> PredictResponse:
    features = np.array([[
        req.fill_level,
        req.fill_rate,
        req.hours_since_empty,
        req.hour_of_day,
        req.battery_level
    ]])

    features_scaled = scaler.transform(features)
    result = int(model.predict(features_scaled)[0])
    proba = model.predict_proba(features_scaled)[0]
    confidence = round(float(max(proba)), 4)

    needs = bool(result)
    return PredictResponse(
        can_id=req.can_id,
        needs_collection=needs,
        confidence=confidence,
        predicted_status="수거 필요" if needs else "정상",
        message=(
            f"쓰레기통 {req.can_id}번 - 오늘 오후 6시 이전 포화 예측됨 (신뢰도 {confidence*100:.1f}%)"
            if needs else
            f"쓰레기통 {req.can_id}번 - 오늘 수거 불필요 (신뢰도 {confidence*100:.1f}%)"
        )
    )


# ────────────────────────────────────────────
# API 엔드포인트
# ────────────────────────────────────────────
@app.get("/")
def root():
    return {"status": "EcoLink AI 서버 정상 동작 중"}


@app.get("/health")
def health():
    return {"status": "ok", "model": "RandomForestClassifier", "version": "1.0.0"}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    """
    단일 쓰레기통 포화 예측

    Spring Boot에서 아래와 같이 호출합니다:
    POST http://localhost:8000/predict
    Body: { "can_id": 1, "fill_level": 65.0, ... }
    """
    try:
        return run_predict(req)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/predict/batch", response_model=list[PredictResponse])
def predict_batch(req: BatchPredictRequest):
    """
    여러 쓰레기통 일괄 포화 예측

    Spring Boot에서 수거 대상 선별 시 사용합니다.
    """
    try:
        return [run_predict(item) for item in req.items]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# ────────────────────────────────────────────
# 실행
# uvicorn main:app --host 0.0.0.0 --port 8000 --reload
# ────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
