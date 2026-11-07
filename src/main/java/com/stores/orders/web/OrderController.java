package com.stores.orders.web;

import com.stores.orders.model.OrderRequest;
import com.stores.orders.model.OrderResult;
import com.stores.orders.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/total")
  public OrderResult calculateTotal(@RequestBody OrderRequest request) {
    return orderService.process(request);
  }
}
