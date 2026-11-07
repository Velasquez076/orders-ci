package com.stores.orders.service;

import com.stores.orders.model.OrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Business rule: the discount is applied ONCE, over the order subtotal, and the
 * result is rounded to two decimals (HALF_UP).
 */
@Component
public class OrderCalculator {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  public BigDecimal calculateSubtotal(List<OrderItem> items) {
    validateItems(items);
    return items.stream()
        .map(OrderItem::subtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal percentage) {
    validatePercentage(percentage);
    return subtotal.multiply(percentage).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
  }

  public BigDecimal calculateTotal(List<OrderItem> items, BigDecimal percentage) {
    BigDecimal subtotal = calculateSubtotal(items);
    BigDecimal discount = calculateDiscount(subtotal, percentage);
    return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
  }

  private void validateItems(List<OrderItem> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("The order must have at least one item");
    }
  }

  private void validatePercentage(BigDecimal percentage) {
    if (percentage == null || percentage.signum() < 0 || percentage.compareTo(ONE_HUNDRED) > 0) {
      throw new IllegalArgumentException("The discount must be between 0 and 100");
    }
  }
}
