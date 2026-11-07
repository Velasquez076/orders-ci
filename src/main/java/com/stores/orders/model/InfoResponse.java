package com.stores.orders.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Information about the running environment.
 */
@Schema(description = "Runtime environment information")
public record InfoResponse(
    @Schema(description = "Active environment name", example = "development")
    String environment) {

}
