package com.stores.orders.model;

import java.math.BigDecimal;

/**
 * Calculation result: subtotal, discount amount and total to pay.
 */
public record OrderResult(BigDecimal subtotal, BigDecimal discount, BigDecimal total) {

}
