package com.stores.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Stock available for a product, persisted in the {@code inventory} table.
 */
@Entity
@Table(name = "inventory")
public class InventoryItem {

  @Id
  @Column(name = "sku", nullable = false, length = 64)
  private String sku;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  protected InventoryItem() {
  }

  public InventoryItem(String sku, int quantity) {
    this.sku = sku;
    this.quantity = quantity;
  }

  public String getSku() {
    return sku;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
