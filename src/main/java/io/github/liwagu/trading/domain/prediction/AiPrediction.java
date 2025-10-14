package io.github.liwagu.trading.domain.prediction;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiPrediction(
        String isin,
        String securityName,
        BigDecimal currentPrice,
        List<PredictionPoint> predictions,
        String signal,
        BigDecimal confidence,
        String trend,
        String aiSummary
) {
}
