package com.stores.orders.model;

import java.math.BigDecimal;

/**
 * Order line: product (SKU), quantity and unit price.
 */
public record OrderItem(String sku, int quantity, BigDecimal unitPrice) {

  public OrderItem {
    if (sku == null || sku.isBlank()) {
      throw new IllegalArgumentException("SKU is required");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
    if (unitPrice == null || unitPrice.signum() < 0) {
      throw new IllegalArgumentException("Unit price cannot be negative");
    }
  }

  public BigDecimal subtotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
