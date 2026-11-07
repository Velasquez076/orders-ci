package com.stores.orders.web;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Allows checking which environment (profile) the same image is running in.
 */
@RestController
public class InfoController {

  private final String environment;

  public InfoController(@Value("${app.environment:unknown}") String environment) {
    this.environment = environment;
  }

  @GetMapping("/api/info")
  public Map<String, String> info() {
    return Map.of("environment", environment);
  }
}
