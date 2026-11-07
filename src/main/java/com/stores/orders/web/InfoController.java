package com.stores.orders.web;

import com.stores.orders.model.InfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Allows checking which environment (profile) the same image is running in.
 */
@Tag(name = "Info", description = "Runtime environment information")
@RestController
public class InfoController {

  private final String environment;

  public InfoController(@Value("${app.environment:unknown}") String environment) {
    this.environment = environment;
  }

  @Operation(summary = "Get the active environment", description = "Returns the name of the active Spring profile.")
  @ApiResponse(
      responseCode = "200",
      description = "Active environment",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = InfoResponse.class)))
  @GetMapping("/api/info")
  public InfoResponse info() {
    return new InfoResponse(environment);
  }
}
