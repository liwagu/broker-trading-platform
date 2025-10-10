package io.github.liwagu.trading.api.dto;

import java.math.BigDecimal;

public class OrderRequest {
    private String portfolioId;
    private String isin;
    private String side;
    private BigDecimal quantity;

    public OrderRequest() {
    }

    public OrderRequest(String portfolioId, String isin, String side, BigDecimal quantity) {
        this.portfolioId = portfolioId;
        this.isin = isin;
        this.side = side;
        this.quantity = quantity;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}