package io.github.liwagu.trading.domain.prediction;

public class AiPredictionNotAvailableException extends RuntimeException {
    public AiPredictionNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
