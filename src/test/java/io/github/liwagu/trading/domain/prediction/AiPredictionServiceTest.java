package io.github.liwagu.trading.domain.prediction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class AiPredictionServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private AiPredictionService predictionService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        predictionService = new AiPredictionService(restTemplate, "http://localhost:5001");
    }

    @Test
    void fetchPredictionParsesResponsePayload() {
        String responseBody = """
                {
                  "isin": "US67066G1040",
                  "security_name": "NVIDIA Corp",
                  "current_price": 100.0,
                  "predictions": [
                    {
                      "date": "2025-01-01",
                      "predicted_price": 105.5,
                      "confidence_lower": 101.0,
                      "confidence_upper": 110.0
                    }
                  ],
                  "signal": "BUY",
                  "confidence": 0.85,
                  "trend": "bullish",
                  "ai_summary": "Uptrend"
                }
                """;

        server.expect(once(), requestTo("http://localhost:5001/predict/US67066G1040?horizon_days=5"))
                .andExpect(method(GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        AiPrediction prediction = predictionService.fetchPrediction("US67066G1040", 5);

        server.verify();

        assertThat(prediction.isin()).isEqualTo("US67066G1040");
        assertThat(prediction.securityName()).isEqualTo("NVIDIA Corp");
        assertThat(prediction.currentPrice()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(prediction.predictions()).hasSize(1);
        assertThat(prediction.signal()).isEqualTo("BUY");
        assertThat(prediction.aiSummary()).isEqualTo("Uptrend");
    }

    @Test
    void fetchPredictionWrapsServerErrors() {
        server.expect(once(), requestTo("http://localhost:5001/predict/US0378331005?horizon_days=5"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> predictionService.fetchPrediction("US0378331005", 5))
                .isInstanceOf(AiPredictionException.class)
                .hasMessageContaining("AI service responded with status");
    }

    @Test
    void fetchPredictionRejectsNonPositiveHorizon() {
        assertThatThrownBy(() -> predictionService.fetchPrediction("US5949181045", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horizonDays must be positive");
    }
}
