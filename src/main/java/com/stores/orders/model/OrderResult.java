package com.stores.orders.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Calculation result: subtotal, discount amount and total to pay.
 */
@Schema(description = "Order calculation result")
public record OrderResult(
    @Schema(description = "Sum of quantity times unit price", example = "160000.00")
    BigDecimal subtotal,
    @Schema(description = "Discount amount", example = "16000.00")
    BigDecimal discount,
    @Schema(description = "Total to pay", example = "144000.00")
    BigDecimal total) {

}
