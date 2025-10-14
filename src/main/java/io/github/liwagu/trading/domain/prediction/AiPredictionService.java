package io.github.liwagu.trading.domain.prediction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Service
public class AiPredictionService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AiPredictionService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${prediction.service.base-url:${PREDICTION_SERVICE_BASE_URL:http://localhost:5001}}") String baseUrl,
            @Value("${prediction.service.connect-timeout:${PREDICTION_SERVICE_CONNECT_TIMEOUT:PT2S}}") Duration connectTimeout,
            @Value("${prediction.service.read-timeout:${PREDICTION_SERVICE_READ_TIMEOUT:PT5S}}") Duration readTimeout) {
        this(restTemplateBuilder
                        .setConnectTimeout(connectTimeout)
                        .setReadTimeout(readTimeout)
                        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .build(),
                baseUrl);
    }

    AiPredictionService(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public AiPrediction fetchPrediction(String isin, int horizonDays) {
        if (horizonDays <= 0) {
            throw new IllegalArgumentException("horizonDays must be positive");
        }

        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("predict", isin)
                .queryParam("horizon_days", horizonDays)
                .build()
                .toUri();

        try {
            AiPrediction prediction = restTemplate.getForObject(uri, AiPrediction.class);
            if (prediction == null) {
                throw new AiPredictionException("AI service returned an empty response");
            }
            return prediction;
        } catch (RestClientResponseException responseException) {
            throw new AiPredictionException(
                    "AI service responded with status %d: %s".formatted(
                            responseException.getRawStatusCode(), responseException.getStatusText()),
                    responseException);
        } catch (Exception exception) {
            throw new AiPredictionException("Failed to reach AI prediction service", exception);
        }
    }
}
