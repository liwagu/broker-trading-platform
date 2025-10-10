package io.github.liwagu.trading.api.dto;

import io.github.liwagu.trading.domain.OrderSide;
import io.github.liwagu.trading.domain.OrderStatus;

import java.math.BigDecimal;

public class OrderResponse {
    private Long id;
    private String portfolioId;
    private String isin;
    private OrderSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderStatus status;

    public OrderResponse() {
    }

    public OrderResponse(Long id, String portfolioId, String isin, OrderSide side,
                        BigDecimal quantity, BigDecimal price, OrderStatus status) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.isin = isin;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}