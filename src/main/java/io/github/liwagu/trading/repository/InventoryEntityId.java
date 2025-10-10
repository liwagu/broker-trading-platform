package io.github.liwagu.trading.repository;

import java.io.Serializable;

/**
 */
public class InventoryEntityId implements Serializable {
    private String portfolioId;
    private String isin;

    public InventoryEntityId(String portfolioId, String isin) {
        this.portfolioId = portfolioId;
        this.isin = isin;
    }

    public InventoryEntityId() {
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getIsin() {
        return isin;
    }
}
