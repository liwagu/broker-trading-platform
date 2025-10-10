package io.github.liwagu.trading.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/**
 */
@Entity
public class BuyingPowerEntity {
    @Id
    private String portfolioId;
    private BigDecimal amount;

    public BuyingPowerEntity(String portfolioId, BigDecimal amount) {
        this.portfolioId = portfolioId;
        this.amount = amount;
    }

    public BuyingPowerEntity() {
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}