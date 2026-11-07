package com.stores.orders.web;

import com.stores.orders.model.ApiError;
import com.stores.orders.model.OrderRequest;
import com.stores.orders.model.OrderResult;
import com.stores.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Order total calculation and stock validation")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @Operation(
      summary = "Calculate an order total",
      description = "Validates stock in the database, then returns subtotal, discount and total. "
          + "The discount is applied once over the subtotal.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Order calculated successfully",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResult.class))),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid request data",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(
          responseCode = "409",
          description = "Insufficient stock for one or more SKUs",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/total")
  public OrderResult calculateTotal(@RequestBody OrderRequest request) {
    return orderService.process(request);
  }
}
