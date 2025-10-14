from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional

from prediction_engine import Prediction, generate_prediction

app = FastAPI(title="Kronos AI Prediction Service", version="1.1.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:3002"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class PredictionRequest(BaseModel):
    isin: str
    horizon_days: Optional[int] = 5


class PredictionPoint(BaseModel):
    date: str
    predicted_price: float
    confidence_lower: float
    confidence_upper: float


class PredictionResponse(BaseModel):
    isin: str
    security_name: str
    current_price: float
    predictions: List[PredictionPoint]
    signal: str  # "BUY", "SELL", "HOLD"
    confidence: float
    trend: str
    ai_summary: str


def _to_response(prediction: Prediction) -> PredictionResponse:
    return PredictionResponse(
        isin=prediction.isin,
        security_name=prediction.security_name,
        current_price=prediction.current_price,
        predictions=[
            PredictionPoint(
                date=point.date,
                predicted_price=point.predicted_price,
                confidence_lower=point.confidence_lower,
                confidence_upper=point.confidence_upper,
            )
            for point in prediction.predictions
        ],
        signal=prediction.signal,
        confidence=prediction.confidence,
        trend=prediction.trend,
        ai_summary=prediction.ai_summary,
    )


@app.get("/")
async def root():
    return {
        "service": "Kronos AI Prediction Service",
        "status": "online",
        "version": app.version,
    }


@app.get("/health")
async def health():
    return {"status": "healthy"}


@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """Generate AI price predictions for a security."""

    try:
        return _to_response(generate_prediction(request.isin, request.horizon_days))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@app.get("/predict/{isin}", response_model=PredictionResponse)
async def predict_get(isin: str, horizon_days: int = 5):
    """Generate AI price predictions (GET version for easy testing)."""

    try:
        return _to_response(generate_prediction(isin, horizon_days))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=5001)
