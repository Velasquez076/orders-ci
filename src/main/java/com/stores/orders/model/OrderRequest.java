package com.stores.orders.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

/**
 * Input data: order items and discount percentage (0 to 100).
 */
@Schema(description = "Order calculation input")
public record OrderRequest(
    @Schema(
        description = "Order lines; at least one item is required",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<OrderItem> items,
    @Schema(
        description = "Discount percentage applied once over the subtotal (0 to 100)",
        example = "10",
        minimum = "0",
        maximum = "100",
        defaultValue = "0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    BigDecimal discountPercentage) {

}
