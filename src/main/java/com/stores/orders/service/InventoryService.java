package com.stores.orders.service;

import com.stores.orders.model.InsufficientStockException;
import com.stores.orders.model.InventoryItem;
import com.stores.orders.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Queries and updates stock in the database.
 */
@Service
public class InventoryService {

  private final InventoryRepository repository;

  public InventoryService(InventoryRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public int getStock(String sku) {
    return repository.findById(sku)
        .map(InventoryItem::getQuantity)
        .orElse(0);
  }

  @Transactional(readOnly = true)
  public boolean hasStock(String sku, int quantity) {
    return getStock(sku) >= quantity;
  }

  @Transactional
  public void decrement(String sku, int quantity) {
    InventoryItem item = repository.findById(sku)
        .orElseThrow(() -> new InsufficientStockException("Insufficient stock for: " + sku));
    if (item.getQuantity() < quantity) {
      throw new InsufficientStockException("Insufficient stock for: " + sku);
    }
    item.setQuantity(item.getQuantity() - quantity);
    repository.save(item);
  }
}
