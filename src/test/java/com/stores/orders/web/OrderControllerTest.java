package com.stores.orders.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stores.orders.model.InsufficientStockException;
import com.stores.orders.model.OrderResult;
import com.stores.orders.service.OrderService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private static final String BODY = """
            {"items":[{"sku":"RICE","quantity":2,"unitPrice":50000}],
             "discountPercentage":10}
            """;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderService orderService;

    @Test
    void WE01_returns200WithSubtotalDiscountAndTotal() throws Exception {
        when(orderService.process(any())).thenReturn(new OrderResult(
                new BigDecimal("100000.00"), new BigDecimal("10000.00"), new BigDecimal("90000.00")));

        mvc.perform(post("/api/orders/total").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(90000.0));
    }

    @Test
    void WE02_returns409WhenThereIsNoStock() throws Exception {
        when(orderService.process(any())).thenThrow(new InsufficientStockException("No stock: RICE"));

        mvc.perform(post("/api/orders/total").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No stock: RICE"));
    }

    @Test
    void WE03_returns400WhenDataIsInvalid() throws Exception {
        when(orderService.process(any())).thenThrow(new IllegalArgumentException("Discount out of range"));

        mvc.perform(post("/api/orders/total").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Discount out of range"));
    }
}
