package io.github.liwagu.trading.api;

import io.github.liwagu.trading.api.dto.ErrorResponse;
import io.github.liwagu.trading.api.TradingService.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TradingExceptionHandler {

    @ExceptionHandler(InsufficientBuyingPowerException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBuyingPower(InsufficientBuyingPowerException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientInventory(InsufficientInventoryException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(404, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderCannotBeCancelledException.class)
    public ResponseEntity<ErrorResponse> handleOrderCannotBeCancelled(OrderCannotBeCancelledException ex) {
        ErrorResponse errorResponse = new ErrorResponse(400, ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}