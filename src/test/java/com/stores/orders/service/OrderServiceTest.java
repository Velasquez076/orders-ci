package com.stores.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stores.orders.model.InsufficientStockException;
import com.stores.orders.model.OrderItem;
import com.stores.orders.model.OrderRequest;
import com.stores.orders.model.OrderResult;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the full use case: validate stock and calculate the total. */
class OrderServiceTest {

    private InventoryService inventoryService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        inventoryService = mock(InventoryService.class);
        orderService = new OrderService(inventoryService, new OrderCalculator());
    }

    @Test
    void PS01_calculatesSubtotalDiscountAndTotalWhenStockIsAvailable() {
        when(inventoryService.hasStock("RICE", 2)).thenReturn(true);
        when(inventoryService.hasStock("OIL", 3)).thenReturn(true);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItem("RICE", 2, new BigDecimal("50000")),
                        new OrderItem("OIL", 3, new BigDecimal("20000"))),
                new BigDecimal("10"));

        OrderResult result = orderService.process(request);

        assertThat(result.subtotal()).isEqualByComparingTo("160000.00");
        assertThat(result.discount()).isEqualByComparingTo("16000.00");
        assertThat(result.total()).isEqualByComparingTo("144000.00");
    }

    @Test
    void PS02_rejectsOrderWhenAnyItemExceedsStock() {
        when(inventoryService.hasStock("RICE", 6)).thenReturn(false);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItem("RICE", 6, new BigDecimal("1000"))), BigDecimal.ZERO);

        assertThatThrownBy(() -> orderService.process(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("RICE");
    }

    @Test
    void PS03_sumsQuantitiesWhenTheSameSkuAppearsSeveralTimes() {
        // 3 + 3 = 6 > 5 stock, even though each line on its own would fit
        when(inventoryService.hasStock("RICE", 6)).thenReturn(false);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItem("RICE", 3, new BigDecimal("1000")),
                        new OrderItem("RICE", 3, new BigDecimal("1000"))),
                BigDecimal.ZERO);

        assertThatThrownBy(() -> orderService.process(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void PS04_processingAnOrderDoesNotModifyInventory() {
        when(inventoryService.hasStock("RICE", 2)).thenReturn(true);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItem("RICE", 2, new BigDecimal("1000"))), BigDecimal.ZERO);

        orderService.process(request);

        verify(inventoryService, never()).decrement(anyString(), anyInt());
    }
}
