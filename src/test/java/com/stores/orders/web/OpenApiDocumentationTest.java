package com.stores.orders.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiDocumentationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void exposesOpenApiContractWithAllEndpoints() {
    ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("Orders API")
        .contains("/api/orders/total")
        .contains("/api/info");
  }

  @Test
  void swaggerUiIsAvailable() {
    ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui.html", String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
  }
}
