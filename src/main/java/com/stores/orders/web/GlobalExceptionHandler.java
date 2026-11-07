package com.stores.orders.web;

import com.stores.orders.model.ApiError;
import com.stores.orders.model.InsufficientStockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleInvalidData(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(e.getMessage()));
  }

  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(e.getMessage()));
  }
}
