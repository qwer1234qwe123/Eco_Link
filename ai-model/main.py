"""
main.py
EcoLink - FastAPI 예측 서버
분류 모델: 오늘 오후 6시 전 80% 도달 여부 (YES/NO)
회귀 모델: 몇 시간 후 80%에 도달하는지 (시간)
"""

import pickle
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
import os
from datetime import datetime, timedelta

# ────────────────────────────────────────────
# 모델 로드
# ────────────────────────────────────────────
if not os.path.exists("model.pkl"):
    raise RuntimeError("[오류] model.pkl 파일이 없습니다. train.py를 먼저 실행해 주세요.")

with open("model.pkl", "rb") as f:
    clf_model = pickle.load(f)   # 분류 모델

with open("scaler.pkl", "rb") as f:
    clf_scaler = pickle.load(f)  # 분류 스케일러

# 회귀 모델 (없으면 None)
reg_model = None
reg_scaler = None
if os.path.exists("regression_model.pkl"):
    with open("regression_model.pkl", "rb") as f:
        reg_model = pickle.load(f)
    with open("regression_scaler.pkl", "rb") as f:
        reg_scaler = pickle.load(f)

# ────────────────────────────────────────────
# FastAPI 앱
# ────────────────────────────────────────────
app = FastAPI(
    title="EcoLink AI 예측 서버",
    description="쓰레기통 포화 시점 예측 API",
    version="2.0.0"
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
    needs_collection: bool
    confidence: float
    predicted_status: str
    message: str
    hours_until_full: Optional[float] = None
    predicted_full_time: Optional[str] = None


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

    # 분류 모델 예측
    clf_features = clf_scaler.transform(features)
    clf_result = int(clf_model.predict(clf_features)[0])
    clf_proba = clf_model.predict_proba(clf_features)[0]
    confidence = round(float(max(clf_proba)), 4)
    needs = bool(clf_result)

    # 회귀 모델 예측
    hours_until_full = None
    predicted_full_time = None

    if reg_model is not None:
        reg_features = reg_scaler.transform(features)
        hours_until_full = round(float(reg_model.predict(reg_features)[0]), 1)
        hours_until_full = max(0.0, hours_until_full)
        full_time = datetime.now() + timedelta(hours=hours_until_full)
        predicted_full_time = full_time.strftime("%H시 %M분")

    return PredictResponse(
        can_id=req.can_id,
        needs_collection=needs,
        confidence=confidence,
        predicted_status="수거 필요" if needs else "정상",
        message=(
            f"쓰레기통 {req.can_id}번 - 오늘 오후 6시 이전 포화 예측됨 (신뢰도 {confidence*100:.1f}%)"
            if needs else
            f"쓰레기통 {req.can_id}번 - 오늘 수거 불필요 (신뢰도 {confidence*100:.1f}%)"
        ),
        hours_until_full=hours_until_full,
        predicted_full_time=predicted_full_time
    )


# ────────────────────────────────────────────
# API 엔드포인트
# ────────────────────────────────────────────
@app.get("/")
def root():
    return {"status": "EcoLink AI 서버 정상 동작 중", "version": "2.0.0"}


@app.get("/health")
def health():
    return {
        "status": "ok",
        "classification_model": "RandomForestClassifier",
        "regression_model": "RandomForestRegressor" if reg_model else "미학습",
        "version": "2.0.0"
    }


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    """단일 쓰레기통 예측 (분류 + 회귀)"""
    try:
        return run_predict(req)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/predict/batch", response_model=list[PredictResponse])
def predict_batch(req: BatchPredictRequest):
    """전체 쓰레기통 일괄 예측"""
    try:
        return [run_predict(item) for item in req.items]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/predict/time")
def predict_time(req: PredictRequest):
    """회귀 모델 전용 - 포화까지 남은 시간 예측"""
    if reg_model is None:
        raise HTTPException(
            status_code=503,
            detail="회귀 모델이 아직 학습되지 않았습니다. train_regression.py를 실행해 주세요."
        )
    try:
        features = np.array([[
            req.fill_level,
            req.fill_rate,
            req.hours_since_empty,
            req.hour_of_day,
            req.battery_level
        ]])
        reg_features = reg_scaler.transform(features)
        hours = round(float(reg_model.predict(reg_features)[0]), 1)
        hours = max(0.0, hours)
        full_time = datetime.now() + timedelta(hours=hours)

        return {
            "can_id": req.can_id,
            "hours_until_full": hours,
            "predicted_full_time": full_time.strftime("%H시 %M분"),
            "message": f"약 {hours}시간 후 포화 예상 ({full_time.strftime('%H시 %M분')})"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)