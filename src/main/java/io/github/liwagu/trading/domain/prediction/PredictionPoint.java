package io.github.liwagu.trading.domain.prediction;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PredictionPoint(
        String date,
        BigDecimal predictedPrice,
        BigDecimal confidenceLower,
        BigDecimal confidenceUpper
) {
}
