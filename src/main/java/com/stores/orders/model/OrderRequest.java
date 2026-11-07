package com.stores.orders.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Input data: order items and discount percentage (0 to 100).
 */
public record OrderRequest(List<OrderItem> items, BigDecimal discountPercentage) {

}
