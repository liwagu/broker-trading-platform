# Kronos AI Prediction Service

FastAPI microservice providing AI-powered price predictions for securities.

## Features

- Real-time price predictions using AI algorithms
- 5-day forecast with confidence intervals
- Trading signals (BUY/SELL/HOLD) with confidence scores
- CORS enabled for frontend integration

## Installation

```bash
pip install -r requirements.txt
```

## Running

```bash
python main.py
```

Or with uvicorn:
```bash
uvicorn main:app --reload --port 5000
```

## API Endpoints

### GET /
Service info and status

### GET /health
Health check

### POST /predict
Generate predictions
```json
{
  "isin": "US67066G1040",
  "horizon_days": 5
}
```

### GET /predict/{isin}
Quick prediction (query param: horizon_days)

## Integration

The service runs on `http://localhost:5000` and can be called from:
- Frontend (Next.js)
- Backend (Spring Boot)
- Direct API calls

## Future Enhancement

Replace the smart mock predictor with actual Kronos model:
```python
from kronos import KronosPredictor
predictor = KronosPredictor.from_pretrained("shiyu-coder/Kronos-mini")
```
