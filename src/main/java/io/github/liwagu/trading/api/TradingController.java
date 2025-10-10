package io.github.liwagu.trading.api;

import io.github.liwagu.trading.api.dto.OrderRequest;
import io.github.liwagu.trading.api.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This is the entry point for all trading operations.
 */
@RestController
@RequestMapping("/orders")
public class TradingController {

    @Autowired
    private TradingService tradingService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = tradingService.createOrder(orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        OrderResponse orderResponse = tradingService.getOrder(id);
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        OrderResponse orderResponse = tradingService.cancelOrder(id);
        return ResponseEntity.ok(orderResponse);
    }
}
