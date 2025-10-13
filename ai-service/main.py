from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import numpy as np
from datetime import datetime, timedelta

app = FastAPI(title="Kronos AI Prediction Service", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:3002"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Security data with historical trends
SECURITIES = {
    "US67066G1040": {"name": "NVIDIA Corp", "current_price": 100.0, "volatility": 0.03, "trend": "bullish"},
    "US0378331005": {"name": "Apple Inc", "current_price": 200.0, "volatility": 0.02, "trend": "neutral"},
    "US5949181045": {"name": "Microsoft Corp", "current_price": 35.5, "volatility": 0.025, "trend": "bullish"},
}

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

def generate_smart_prediction(isin: str, horizon_days: int = 5) -> PredictionResponse:
    """
    Generate realistic AI predictions using statistical modeling
    (MVP version - simulates Kronos behavior)
    """
    if isin not in SECURITIES:
        raise ValueError(f"Unknown ISIN: {isin}")
    
    security = SECURITIES[isin]
    current_price = security["current_price"]
    volatility = security["volatility"]
    trend = security["trend"]
    
    # Generate prediction points
    predictions = []
    base_date = datetime.now()
    
    # Trend parameters
    if trend == "bullish":
        drift = 0.015  # 1.5% daily growth
        signal = "BUY"
        confidence = np.random.uniform(0.78, 0.92)
    elif trend == "bearish":
        drift = -0.012
        signal = "SELL"
        confidence = np.random.uniform(0.75, 0.88)
    else:  # neutral
        drift = 0.002
        signal = "HOLD"
        confidence = np.random.uniform(0.65, 0.78)
    
    # Generate predictions with realistic variance
    for day in range(1, horizon_days + 1):
        # Geometric Brownian Motion simulation
        random_shock = np.random.normal(0, volatility)
        price_change = current_price * (drift + random_shock)
        predicted_price = current_price + (price_change * day)
        
        # Confidence intervals (wider for further predictions)
        confidence_width = volatility * current_price * (1 + 0.3 * day)
        
        predictions.append(PredictionPoint(
            date=(base_date + timedelta(days=day)).strftime("%Y-%m-%d"),
            predicted_price=round(predicted_price, 2),
            confidence_lower=round(predicted_price - confidence_width, 2),
            confidence_upper=round(predicted_price + confidence_width, 2)
        ))
    
    # Generate AI summary
    final_price = predictions[-1].predicted_price
    price_change_pct = ((final_price - current_price) / current_price) * 100
    
    if signal == "BUY":
        summary = f"Strong upward momentum detected. Predicted {price_change_pct:+.1f}% move in {horizon_days} days. Technical indicators suggest continued bullish trend."
    elif signal == "SELL":
        summary = f"Bearish signals detected. Predicted {price_change_pct:+.1f}% move in {horizon_days} days. Consider profit-taking or position reduction."
    else:
        summary = f"Neutral outlook. Predicted {price_change_pct:+.1f}% move in {horizon_days} days. Market consolidation expected."
    
    return PredictionResponse(
        isin=isin,
        security_name=security["name"],
        current_price=current_price,
        predictions=predictions,
        signal=signal,
        confidence=round(confidence, 2),
        trend=trend,
        ai_summary=summary
    )

@app.get("/")
async def root():
    return {
        "service": "Kronos AI Prediction Service",
        "status": "online",
        "model": "Kronos-mini (simulated for MVP)",
        "version": "1.0.0"
    }

@app.get("/health")
async def health():
    return {"status": "healthy"}

@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest):
    """
    Generate AI price predictions for a security
    """
    return generate_smart_prediction(request.isin, request.horizon_days)

@app.get("/predict/{isin}", response_model=PredictionResponse)
async def predict_get(isin: str, horizon_days: int = 5):
    """
    Generate AI price predictions (GET version for easy testing)
    """
    return generate_smart_prediction(isin, horizon_days)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)
