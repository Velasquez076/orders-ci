package com.stores.orders.service;

import com.stores.orders.model.InsufficientStockException;
import com.stores.orders.model.OrderItem;
import com.stores.orders.model.OrderRequest;
import com.stores.orders.model.OrderResult;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final InventoryService inventoryService;
  private final OrderCalculator calculator;

  public OrderService(InventoryService inventoryService, OrderCalculator calculator) {
    this.inventoryService = inventoryService;
    this.calculator = calculator;
  }

  /**
   * Validates stock (summing quantities by SKU) and calculates subtotal, discount and total.
   */
  @Transactional(readOnly = true)
  public OrderResult process(OrderRequest request) {
    BigDecimal percentage = request.discountPercentage() == null
        ? BigDecimal.ZERO : request.discountPercentage();

    BigDecimal subtotal = calculator.calculateSubtotal(request.items());
    BigDecimal discount = calculator.calculateDiscount(subtotal, percentage);
    BigDecimal total = calculator.calculateTotal(request.items(), percentage);

    validateStock(request);
    return new OrderResult(subtotal, discount, total);
  }

  private void validateStock(OrderRequest request) {
    Map<String, Integer> requestedBySku = new HashMap<>();
    for (OrderItem item : request.items()) {
      requestedBySku.merge(item.sku(), item.quantity(), Integer::sum);
    }
    requestedBySku.forEach((sku, quantity) -> {
      if (!inventoryService.hasStock(sku, quantity)) {
        throw new InsufficientStockException("Insufficient stock for: " + sku);
      }
    });
  }
}
