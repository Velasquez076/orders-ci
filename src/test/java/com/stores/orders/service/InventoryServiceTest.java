package com.stores.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stores.orders.model.InsufficientStockException;
import com.stores.orders.model.InventoryItem;
import com.stores.orders.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Tests for stock validation against the database. */
@DataJpaTest
@ActiveProfiles("test")
@Import(InventoryService.class)
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(new InventoryItem("RICE", 10));
    }

    @Test
    void IN01_hasStockWhenQuantityIsLessThanInventory() {
        assertThat(inventoryService.hasStock("RICE", 4)).isTrue();
    }

    @Test
    void IN02_hasStockWhenQuantityEqualsInventory() {
        assertThat(inventoryService.hasStock("RICE", 10)).isTrue();
    }

    @Test
    void IN03_hasNoStockWhenQuantityExceedsInventory() {
        assertThat(inventoryService.hasStock("RICE", 11)).isFalse();
    }

    @Test
    void IN04_unknownSkuHasNoStock() {
        assertThat(inventoryService.hasStock("DOES-NOT-EXIST", 1)).isFalse();
    }

    @Test
    void IN05_decrementReducesStock() {
        inventoryService.decrement("RICE", 3);

        assertThat(inventoryService.getStock("RICE")).isEqualTo(7);
    }

    @Test
    void IN06_decrementingMoreThanAvailableThrowsAndKeepsInventory() {
        assertThatThrownBy(() -> inventoryService.decrement("RICE", 11))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(inventoryService.getStock("RICE")).isEqualTo(10);
    }
}
