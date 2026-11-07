package com.stores.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI ordersOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Orders API")
            .version("v1")
            .description("REST API to calculate order totals with discounts and validate stock "
                + "against the database.")
            .contact(new Contact().name("Orders team")));
  }
}
