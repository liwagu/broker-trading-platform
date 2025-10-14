package io.github.liwagu.trading.domain.prediction;

public class AiPredictionException extends RuntimeException {
    public AiPredictionException(String message) {
        super(message);
    }

    public AiPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}
