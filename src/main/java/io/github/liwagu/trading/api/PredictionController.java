package io.github.liwagu.trading.api;

import io.github.liwagu.trading.domain.prediction.AiPrediction;
import io.github.liwagu.trading.domain.prediction.AiPredictionException;
import io.github.liwagu.trading.domain.prediction.AiPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/predictions")
public class PredictionController {

    private final AiPredictionService aiPredictionService;

    public PredictionController(AiPredictionService aiPredictionService) {
        this.aiPredictionService = aiPredictionService;
    }

    @GetMapping("/{isin}")
    public ResponseEntity<AiPrediction> getPrediction(
            @PathVariable String isin,
            @RequestParam(name = "horizon_days", defaultValue = "5") int horizonDays) {
        try {
            AiPrediction prediction = aiPredictionService.fetchPrediction(isin, horizonDays);
            return ResponseEntity.ok(prediction);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(BAD_REQUEST, illegalArgumentException.getMessage(), illegalArgumentException);
        } catch (AiPredictionException aiPredictionException) {
            throw new ResponseStatusException(BAD_GATEWAY, aiPredictionException.getMessage(), aiPredictionException);
        }
    }
}
