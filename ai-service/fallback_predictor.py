"""
Fallback predictor using real market data with trend-based predictions.

This predictor uses real historical data from Yahoo Finance but applies
simple trend analysis instead of the Kronos model. Used when Kronos
initialization fails or is disabled.
"""

import logging
from datetime import timedelta
from typing import Dict, List

import numpy as np
import pandas as pd

from prediction_engine import Prediction, PredictionPoint, SECURITIES

logger = logging.getLogger(__name__)


class RealDataFallbackPredictor:
    """
    Fallback predictor that uses real market data with trend-based forecasting.

    This is a lightweight alternative to Kronos that:
    - Fetches real historical data from Yahoo Finance
    - Applies simple moving average and trend analysis
    - Generates reasonable price predictions
    - Returns results quickly without model downloads
    """

    def __init__(self):
        logger.info("✅ Initialized Real Data Fallback Predictor (no Kronos model required)")

    def predict(self, isin: str, horizon_days: int) -> Prediction:
        """
        Generate price prediction using real market data + trend analysis.

        Args:
            isin: Security identifier
            horizon_days: Number of days to predict

        Returns:
            Prediction dataclass
        """
        logger.info(f"📊 Generating trend-based prediction for {isin} using REAL market data")

        try:
            # Import market data functions
            from market_data import get_historical_data, get_current_price

            # Fetch real historical data
            lookback = min(60, horizon_days * 20)  # Use recent history
            df, timestamps = get_historical_data(
                isin=isin,
                lookback=lookback,
                interval='1d'  # Daily data for trend analysis
            )

            current_price = get_current_price(isin)

            # Calculate trend indicators
            prices = df['close'].values

            # Simple Moving Averages
            sma_short = np.mean(prices[-5:]) if len(prices) >= 5 else current_price
            sma_long = np.mean(prices[-20:]) if len(prices) >= 20 else current_price

            # Price momentum
            price_change = (prices[-1] - prices[0]) / prices[0] if len(prices) > 0 else 0
            volatility = np.std(prices) if len(prices) > 1 else prices[-1] * 0.02

            # Determine trend direction
            if sma_short > sma_long * 1.01:
                trend_direction = 1  # Upward
                trend = "bullish"
            elif sma_short < sma_long * 0.99:
                trend_direction = -1  # Downward
                trend = "bearish"
            else:
                trend_direction = 0  # Sideways
                trend = "neutral"

            # Generate predictions as PredictionPoint objects
            predictions: List[PredictionPoint] = []
            base_date = pd.Timestamp.now()

            # Trend-based forecast
            daily_change_pct = price_change / len(prices) if len(prices) > 0 else 0.001
            predicted_price = current_price

            for day in range(1, horizon_days + 1):
                # Apply trend with some randomness
                trend_factor = 1 + (daily_change_pct * 1.5)  # Amplify trend slightly
                noise_factor = 1 + (np.random.randn() * 0.01)  # Small random noise

                predicted_price = predicted_price * trend_factor * noise_factor

                # Calculate confidence interval based on volatility
                confidence = 0.80  # Base confidence
                confidence_width = volatility * 0.5

                prediction_date = (base_date + timedelta(days=day)).strftime('%Y-%m-%d')
                predictions.append(PredictionPoint(
                    date=prediction_date,
                    predicted_price=float(predicted_price),
                    confidence_lower=float(predicted_price - confidence_width),
                    confidence_upper=float(predicted_price + confidence_width)
                ))

            # Determine trading signal
            final_price = predictions[-1].predicted_price
            price_change_pct = ((final_price - current_price) / current_price) * 100

            if price_change_pct > 2:
                signal = "BUY"
                confidence_score = 0.75
            elif price_change_pct < -2:
                signal = "SELL"
                confidence_score = 0.72
            else:
                signal = "HOLD"
                confidence_score = 0.70

            ai_summary = (
                f"Trend-based analysis predicts {price_change_pct:+.1f}% movement in {horizon_days} days. "
                f"Market {trend} with {volatility:.2f} volatility. "
                f"Real data from Yahoo Finance ({len(prices)} points). "
                f"[Kronos model unavailable - using statistical forecasting]"
            )

            security_name = SECURITIES.get(isin, {}).get("name", "Unknown Security")

            result = Prediction(
                isin=isin,
                security_name=security_name,
                current_price=current_price,
                predictions=predictions,
                signal=signal,
                confidence=confidence_score,
                trend=trend,
                ai_summary=ai_summary
            )

            logger.info(f"✅ Trend-based prediction completed for {isin}")
            return result

        except Exception as e:
            logger.error(f"❌ Prediction failed: {e}")
            raise
