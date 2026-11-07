# Orders CI – continuous integration baseline

Minimal application (Java 21 + Spring Boot 3) that represents part of a neighborhood store chain's
order and dispatch system: **order total with discounts** and **stock validation backed by a database**.

- Expected behavior: [`docs/ESPECIFICACION.md`](docs/ESPECIFICACION.md)
- Git and evidence guide: [`docs/GUIA_GIT_Y_EVIDENCIAS.md`](docs/GUIA_GIT_Y_EVIDENCIAS.md)

## Database

Stock is no longer held in memory: every order is validated against the `inventory` table
(`sku`, `quantity`). Configure the connection through environment variables:

| Variable | Purpose |
|----------|---------|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/orders` |
| `DB_USER` | Database user |
| `DB_PASSWORD` | Database password |

The `dev` profile applies safe local defaults; `test` uses an in-memory H2 database; `prod`
requires the three variables and validates the existing schema (`spring.jpa.hibernate.ddl-auto=validate`).

## API documentation (Swagger / OpenAPI v3)

With the application running (`mvn spring-boot:run`), the interactive documentation and the
machine-readable contract are available at:

| Resource | URL |
|----------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON (for Postman/Insomnia/frontend codegen) | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

### Available endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/orders/total` | Validates stock and calculates subtotal, discount and total. |
| `GET` | `/api/info` | Returns the active environment name. |

For the frontend handoff, share the `http://localhost:8080/v3/api-docs` URL (or the generated
`v3/api-docs.yaml`): it can be imported directly into Postman/Insomnia or used to generate a typed
client. The Swagger UI also documents request/response schemas, examples and error codes
(`400` invalid data, `409` insufficient stock).

The contract is also committed as static files ready to share with the frontend team:
[`docs/openapi.json`](docs/openapi.json) and [`docs/openapi.yaml`](docs/openapi.yaml). To regenerate
them from a running application:

```bash
curl http://localhost:8080/v3/api-docs      -o docs/openapi.json
curl http://localhost:8080/v3/api-docs.yaml -o docs/openapi.yaml
```

## Run the tests (single command)

```bash
mvn test
```

Requires JDK 21 or higher and Maven 3.9+.

## With Docker (reproducible environment)

```bash
# 1) Test image and test execution inside the container
docker build --target tests -t orders-tests .
docker run --rm orders-tests

# 2) Application runtime image
docker build -t orders-app .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev orders-app
```

Test the API (the `inventory` table must contain rows for the requested SKUs):

```bash
curl http://localhost:8080/api/info
curl -X POST http://localhost:8080/api/orders/total \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"RICE","quantity":2,"unitPrice":50000},{"sku":"OIL","quantity":3,"unitPrice":20000}],"discountPercentage":10}'
```

## Environments

A single image; the environment is selected with `SPRING_PROFILES_ACTIVE` (`dev`, `test`, `prod`).
Credentials and the database are injected through environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`);
**never** stored in the repository.
