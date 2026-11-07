package com.stores.orders.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Error payload returned by the API.
 */
@Schema(description = "Error response")
public record ApiError(
    @Schema(
        description = "Human-readable error message",
        example = "Insufficient stock for: RICE")
    String error) {

}
