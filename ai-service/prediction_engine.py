"""Prediction engine abstractions for Kronos integration.

This module centralises how the FastAPI service loads a predictor instance.
It attempts to initialise the Kronos model when the library is available and
will gracefully fall back to a statistical simulator otherwise. This lets us
ship a working demo today while keeping the upgrade path to the real Kronos
weights straightforward.
"""

from __future__ import annotations

import logging
import os
from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Dict, Iterable, List

import numpy as np


logger = logging.getLogger(__name__)


# Default securities and heuristics that mirror the previous MVP behaviour.
SECURITIES: Dict[str, Dict[str, object]] = {
    "US67066G1040": {
        "name": "NVIDIA Corp",
        "current_price": 100.0,
        "volatility": 0.03,
        "trend": "bullish",
    },
    "US0378331005": {
        "name": "Apple Inc",
        "current_price": 200.0,
        "volatility": 0.02,
        "trend": "neutral",
    },
    "US5949181045": {
        "name": "Microsoft Corp",
        "current_price": 35.5,
        "volatility": 0.025,
        "trend": "bullish",
    },
}


@dataclass
class PredictionPoint:
    """Normalised representation of a single horizon forecast."""

    date: str
    predicted_price: float
    confidence_lower: float
    confidence_upper: float


@dataclass
class Prediction:
    """Container for a full Kronos prediction payload."""

    isin: str
    security_name: str
    current_price: float
    predictions: List[PredictionPoint]
    signal: str
    confidence: float
    trend: str
    ai_summary: str


class BasePredictor:
    """Common interface implemented by concrete predictor backends."""

    def predict(self, isin: str, horizon_days: int) -> Prediction:
        raise NotImplementedError


class KronosPredictorBackend(BasePredictor):
    """Adapter that loads the official Kronos foundation model."""

    def __init__(self, model_name: str | None = None, device: str | None = None):
        model_name = model_name or os.getenv("KRONOS_MODEL_NAME", "shiyu-coder/Kronos-mini")
        device = device or os.getenv("KRONOS_DEVICE")

        try:
            from kronos import KronosPredictor  # type: ignore
        except Exception as exc:  # pragma: no cover - exercised when kronos missing
            raise RuntimeError(
                "Kronos library not available. Install it or set KRONOS_MODE=simulated."
            ) from exc

        logger.info("Loading Kronos predictor %s with device %s", model_name, device or "auto")
        if device:
            self._predictor = KronosPredictor.from_pretrained(model_name, device=device)
        else:
            self._predictor = KronosPredictor.from_pretrained(model_name)

    def predict(self, isin: str, horizon_days: int) -> Prediction:  # pragma: no cover - depends on kronos
        forecast = self._predictor.predict(isin=isin, horizon=horizon_days)

        predictions: List[PredictionPoint] = []
        for item in forecast.predictions:  # type: ignore[attr-defined]
            predictions.append(
                PredictionPoint(
                    date=item.date,
                    predicted_price=float(item.predicted_price),
                    confidence_lower=float(item.confidence_interval[0]),
                    confidence_upper=float(item.confidence_interval[1]),
                )
            )

        return Prediction(
            isin=forecast.isin,
            security_name=forecast.security_name,
            current_price=float(forecast.current_price),
            predictions=predictions,
            signal=forecast.signal,
            confidence=float(forecast.confidence),
            trend=forecast.trend,
            ai_summary=forecast.ai_summary,
        )


class SimulatedPredictorBackend(BasePredictor):
    """A statistical simulator that mirrors the original MVP behaviour."""

    def predict(self, isin: str, horizon_days: int) -> Prediction:
        if isin not in SECURITIES:
            raise ValueError(f"Unknown ISIN: {isin}")

        security = SECURITIES[isin]
        current_price = float(security["current_price"])
        volatility = float(security["volatility"])
        trend = str(security["trend"])

        base_date = datetime.now()

        if trend == "bullish":
            drift = 0.015
            signal = "BUY"
            confidence = float(np.random.uniform(0.78, 0.92))
            summary_template = (
                "Strong upward momentum detected. Predicted {pct:+.1f}% move in {days} days."
                " Technical indicators suggest continued bullish trend."
            )
        elif trend == "bearish":
            drift = -0.012
            signal = "SELL"
            confidence = float(np.random.uniform(0.75, 0.88))
            summary_template = (
                "Bearish signals detected. Predicted {pct:+.1f}% move in {days} days."
                " Consider profit-taking or position reduction."
            )
        else:
            drift = 0.002
            signal = "HOLD"
            confidence = float(np.random.uniform(0.65, 0.78))
            summary_template = (
                "Neutral outlook. Predicted {pct:+.1f}% move in {days} days."
                " Market consolidation expected."
            )

        predictions: List[PredictionPoint] = []
        for day in range(1, horizon_days + 1):
            random_shock = float(np.random.normal(0, volatility))
            price_change = current_price * (drift + random_shock)
            predicted_price = current_price + (price_change * day)
            confidence_width = volatility * current_price * (1 + 0.3 * day)

            predictions.append(
                PredictionPoint(
                    date=(base_date + timedelta(days=day)).strftime("%Y-%m-%d"),
                    predicted_price=round(predicted_price, 2),
                    confidence_lower=round(predicted_price - confidence_width, 2),
                    confidence_upper=round(predicted_price + confidence_width, 2),
                )
            )

        final_price = predictions[-1].predicted_price
        price_change_pct = ((final_price - current_price) / current_price) * 100
        ai_summary = summary_template.format(pct=price_change_pct, days=horizon_days)

        return Prediction(
            isin=isin,
            security_name=str(security["name"]),
            current_price=current_price,
            predictions=predictions,
            signal=signal,
            confidence=round(confidence, 2),
            trend=trend,
            ai_summary=ai_summary,
        )


def _determine_backend() -> BasePredictor:
    mode = os.getenv("KRONOS_MODE", "auto").lower()

    if mode == "simulated":
        logger.info("Using simulated Kronos predictor backend due to KRONOS_MODE override")
        return SimulatedPredictorBackend()

    try:
        return KronosPredictorBackend()
    except RuntimeError as exc:
        if mode == "auto":
            logger.warning("Falling back to simulated predictor: %s", exc)
            return SimulatedPredictorBackend()
        raise


PREDICTOR_BACKEND: BasePredictor = _determine_backend()


def generate_prediction(isin: str, horizon_days: int) -> Prediction:
    """Facade used by the FastAPI routes."""

    if horizon_days <= 0:
        raise ValueError("horizon_days must be positive")

    return PREDICTOR_BACKEND.predict(isin, horizon_days)

