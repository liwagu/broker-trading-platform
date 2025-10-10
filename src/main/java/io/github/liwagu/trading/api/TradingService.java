package io.github.liwagu.trading.api;

import io.github.liwagu.trading.domain.MarketDataService;
import io.github.liwagu.trading.domain.OrderSide;
import io.github.liwagu.trading.domain.OrderStatus;
import io.github.liwagu.trading.api.dto.OrderRequest;
import io.github.liwagu.trading.api.dto.OrderResponse;
import io.github.liwagu.trading.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TradingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BuyingPowerRepository buyingPowerRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MarketDataService marketDataService;

    public OrderResponse createOrder(OrderRequest orderRequest) {
        OrderSide side = OrderSide.valueOf(orderRequest.getSide());

        if (side == OrderSide.BUY) {
            return createBuyOrder(orderRequest);
        } else {
            return createSellOrder(orderRequest);
        }
    }

    private OrderResponse createBuyOrder(OrderRequest orderRequest) {
        String portfolioId = orderRequest.getPortfolioId();
        String isin = orderRequest.getIsin();
        BigDecimal quantity = orderRequest.getQuantity();

        BigDecimal price = marketDataService.getPrice(isin);
        BigDecimal totalCost = price.multiply(quantity);

        BuyingPowerEntity buyingPower = buyingPowerRepository.findById(portfolioId)
                .orElse(new BuyingPowerEntity(portfolioId, new BigDecimal("5000.00")));

        if (buyingPower.getAmount().compareTo(totalCost) < 0) {
            throw new InsufficientBuyingPowerException("Insufficient buying power");
        }

        BigDecimal newBuyingPower = buyingPower.getAmount().subtract(totalCost);
        buyingPowerRepository.save(new BuyingPowerEntity(portfolioId, newBuyingPower));

        InventoryEntityId inventoryId = new InventoryEntityId(portfolioId, isin);
        InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                .orElse(new InventoryEntity(portfolioId, isin, BigDecimal.ZERO));

        BigDecimal newQuantity = inventory.getQuantity().add(quantity);
        inventoryRepository.save(new InventoryEntity(portfolioId, isin, newQuantity));

        OrderEntity orderEntity = new OrderEntity(portfolioId, isin, OrderStatus.CREATED,
                OrderSide.BUY, quantity, price);
        orderEntity = orderRepository.save(orderEntity);

        return mapToOrderResponse(orderEntity);
    }

    private OrderResponse createSellOrder(OrderRequest orderRequest) {
        String portfolioId = orderRequest.getPortfolioId();
        String isin = orderRequest.getIsin();
        BigDecimal quantity = orderRequest.getQuantity();

        InventoryEntityId inventoryId = new InventoryEntityId(portfolioId, isin);
        InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                .orElse(new InventoryEntity(portfolioId, isin, BigDecimal.ZERO));

        if (inventory.getQuantity().compareTo(quantity) < 0) {
            throw new InsufficientInventoryException("Insufficient inventory");
        }

        BigDecimal price = marketDataService.getPrice(isin);
        BigDecimal totalRevenue = price.multiply(quantity);

        BigDecimal newInventoryQuantity = inventory.getQuantity().subtract(quantity);
        inventoryRepository.save(new InventoryEntity(portfolioId, isin, newInventoryQuantity));

        BuyingPowerEntity buyingPower = buyingPowerRepository.findById(portfolioId)
                .orElse(new BuyingPowerEntity(portfolioId, new BigDecimal("5000.00")));

        BigDecimal newBuyingPower = buyingPower.getAmount().add(totalRevenue);
        buyingPowerRepository.save(new BuyingPowerEntity(portfolioId, newBuyingPower));

        OrderEntity orderEntity = new OrderEntity(portfolioId, isin, OrderStatus.CREATED,
                OrderSide.SELL, quantity, price);
        orderEntity = orderRepository.save(orderEntity);

        return mapToOrderResponse(orderEntity);
    }

    public OrderResponse getOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        return mapToOrderResponse(orderEntity);
    }

    public OrderResponse cancelOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (orderEntity.getStatus() != OrderStatus.CREATED) {
            throw new OrderCannotBeCancelledException("Order cannot be cancelled");
        }

        String portfolioId = orderEntity.getPortfolioId();
        String isin = orderEntity.getIsin();
        BigDecimal quantity = orderEntity.getQuantity();
        BigDecimal price = orderEntity.getPrice();

        if (orderEntity.getSide() == OrderSide.BUY) {
            BigDecimal totalCost = price.multiply(quantity);

            BuyingPowerEntity buyingPower = buyingPowerRepository.findById(portfolioId)
                    .orElse(new BuyingPowerEntity(portfolioId, new BigDecimal("5000.00")));
            BigDecimal restoredBuyingPower = buyingPower.getAmount().add(totalCost);
            buyingPowerRepository.save(new BuyingPowerEntity(portfolioId, restoredBuyingPower));

            InventoryEntityId inventoryId = new InventoryEntityId(portfolioId, isin);
            InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                    .orElse(new InventoryEntity(portfolioId, isin, BigDecimal.ZERO));
            BigDecimal restoredQuantity = inventory.getQuantity().subtract(quantity);
            inventoryRepository.save(new InventoryEntity(portfolioId, isin, restoredQuantity));
        } else {
            BigDecimal totalRevenue = price.multiply(quantity);

            InventoryEntityId inventoryId = new InventoryEntityId(portfolioId, isin);
            InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                    .orElse(new InventoryEntity(portfolioId, isin, BigDecimal.ZERO));
            BigDecimal restoredQuantity = inventory.getQuantity().add(quantity);
            inventoryRepository.save(new InventoryEntity(portfolioId, isin, restoredQuantity));

            BuyingPowerEntity buyingPower = buyingPowerRepository.findById(portfolioId)
                    .orElse(new BuyingPowerEntity(portfolioId, new BigDecimal("5000.00")));
            BigDecimal restoredBuyingPower = buyingPower.getAmount().subtract(totalRevenue);
            buyingPowerRepository.save(new BuyingPowerEntity(portfolioId, restoredBuyingPower));
        }

        orderEntity.setStatus(OrderStatus.CANCELLED);
        orderEntity = orderRepository.save(orderEntity);

        return mapToOrderResponse(orderEntity);
    }

    private OrderResponse mapToOrderResponse(OrderEntity orderEntity) {
        return new OrderResponse(
                orderEntity.getId(),
                orderEntity.getPortfolioId(),
                orderEntity.getIsin(),
                orderEntity.getSide(),
                orderEntity.getQuantity(),
                orderEntity.getPrice(),
                orderEntity.getStatus()
        );
    }

    public static class InsufficientBuyingPowerException extends RuntimeException {
        public InsufficientBuyingPowerException(String message) {
            super(message);
        }
    }

    public static class InsufficientInventoryException extends RuntimeException {
        public InsufficientInventoryException(String message) {
            super(message);
        }
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    public static class OrderCannotBeCancelledException extends RuntimeException {
        public OrderCannotBeCancelledException(String message) {
            super(message);
        }
    }
}