package io.github.liwagu.trading.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for retrieving market data prices for securities.
 */
@Service
public class MarketDataService {

    /**
     * Returns the price of the given ISIN.
     * Supported ISINs:
     * - US67066G1040 -> 100.0
     * - US0378331005 -> 200.0
     * - US5949181045 -> 35.50
     *
     * @param isin the ISIN of the security
     * @return the price of the security
     * @throws IllegalArgumentException if the ISIN is unknown
     */
    public BigDecimal getPrice(String isin) {
        return switch (isin) {
            case "US67066G1040" -> new BigDecimal("100.00");
            case "US0378331005" -> new BigDecimal("200.00");
            case "US5949181045" -> new BigDecimal("35.50");
            default -> throw new IllegalArgumentException("Unknown ISIN: " + isin);
        };
    }
}
