package com.stores.orders.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Order line: product (SKU), quantity and unit price.
 */
@Schema(description = "Order line")
public record OrderItem(
    @Schema(
        description = "Product SKU",
        example = "RICE",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String sku,
    @Schema(
        description = "Quantity ordered (greater than zero)",
        example = "2",
        minimum = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    int quantity,
    @Schema(
        description = "Unit price (zero or greater)",
        example = "50000",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal unitPrice) {

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
