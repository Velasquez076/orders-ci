# Docker: ejecución de pruebas e incidencias

Fecha de ejecución: **2026-11-08** · Rama `feature/docker`

- Primera ejecución: Docker Engine de Ubuntu 29.1.3 · Docker Compose 2.40.3
- Segunda ejecución: Docker Desktop 4.98 (Docker 29.9.3 · Compose 5.6.1)

## 1. Pruebas ejecutadas

| # | Prueba | Comando | Resultado |
|---|--------|---------|-----------|
| 1 | Pruebas automatizadas en contenedor | `./scripts/evidencias.sh docker` | ✅ `Tests run: 25, Failures: 0, Errors: 0, Skipped: 0` – `BUILD SUCCESS` (Docker Engine de Ubuntu) |
| 2 | Arranque API + BD (perfil `dev`) | `docker compose up -d --build` | ✅ `db` y `api` en estado `healthy` |
| 3 | Ambiente activo | `curl localhost:8080/api/info` | ✅ `{"environment":"development"}` |
| 4 | Pedido con stock suficiente | `POST /api/orders/total` (RICE×2, OIL×3, 10 % dto.) | ✅ `200` `{"subtotal":160000.00,"discount":16000.00,"total":144000.00}` |
| 5 | Pedido con stock insuficiente | `POST /api/orders/total` (SUGAR×10, hay 5) | ✅ `409` `{"error":"Insufficient stock for: SUGAR"}` |
| 6 | Documentación Swagger | `GET /swagger-ui.html` | ✅ `200` |
| 7 | Misma imagen, perfil `prod` | `SPRING_PROFILES_ACTIVE=prod docker compose up -d api` | ✅ `healthy`, `{"environment":"production"}`; `ddl-auto=validate` acepta el esquema |
| 8 | API en un solo contenedor (comando anterior del README) | `docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev orders-app` | ❌ El contenedor termina con código `1` → ver incidencia 1 |
| 9 | Pruebas automatizadas en Docker Desktop, lanzadas desde `scripts/` | `cd scripts && ./evidencias.sh docker` | ❌ `open Dockerfile: no such file or directory` → ver incidencia 2 |
| 10 | Repetición de la prueba 9 con el script corregido | `./evidencias.sh docker` o `./scripts/evidencias.sh docker` | ✅ `Tests run: 25, Failures: 0, Errors: 0` – `BUILD SUCCESS` (12:05:33, contexto `desktop-linux`; salida en `evidencias/05_docker_tests.txt`) |

## 2. Incidencias

### Incidencia 1 – La API no arranca en un contenedor aislado (prueba 8)

- **Error observado:**

  ```
  Caused by: org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection
  Caused by: org.postgresql.util.PSQLException: Connection to localhost:5432 refused.
  ```

- **Causa:**
  1. El perfil `dev` usa por defecto `jdbc:postgresql://localhost:5432/orders`. Dentro de un contenedor,
     `localhost` es el propio contenedor de la API, donde no hay PostgreSQL.
  2. `DB_USER` y `DB_PASSWORD` no tienen valor por defecto.
  3. Con `spring.sql.init.mode=always`, Spring ejecuta `schema.sql` al arrancar y necesita la conexión desde el inicio.
- **Corrección:** se ejecuta como aplicación multicontenedor con `docker-compose.yml`:
  - `api` se conecta con `DB_URL=jdbc:postgresql://db:5432/orders`, usando el nombre del servicio como host.
  - `depends_on: condition: service_healthy`: la API solo arranca cuando PostgreSQL responde a `pg_isready`.
  - Credenciales en `.env` (no versionado); se versiona solo `.env.example`.
  - El comando del README y de la guía se cambió por `docker compose up`.
- **Verificación:** con esta corrección, las pruebas 2 a 7 pasan.

### Incidencia 2 – Las pruebas no se ejecutan si el script se lanza desde `scripts/` (prueba 9)

- **Error observado:**

  ```
  ERROR: failed to build: failed to solve: failed to read dockerfile: open Dockerfile: no such file or directory
  docker: Error response from daemon: pull access denied for orders-tests, repository does not exist
  ```

- **Causa:** el script usa rutas relativas (`docker build ... .` y `mkdir -p evidencias`) y dependía de la carpeta
  desde la que se lanzaba. Desde `scripts/` no encuentra el `Dockerfile`, así que la imagen `orders-tests` no se
  construye y el `docker run` posterior intenta descargarla de Docker Hub. Además creó una carpeta
  `scripts/evidencias/` con la salida fallida.
- **Corrección:** `scripts/evidencias.sh` ahora se mueve a la raíz del repositorio al empezar
  (`cd "$(dirname "$0")/.."`), así que funciona desde cualquier carpeta. Se eliminó `scripts/evidencias/`.
- **Verificación:** prueba 10 (25/25).

### Observación – Aviso del constructor de Docker (prueba 1)

- **Mensaje:** `DEPRECATED: The legacy builder is deprecated and will be removed in a future release.`
- **Impacto:** ninguno. Es un aviso: la imagen se construye y las 25 pruebas pasan.
- **Causa:** el Docker Engine de Ubuntu no tenía el plugin `buildx` (BuildKit).
- **Resolución:** con Docker Desktop, que incluye `docker-buildx-plugin`, el aviso ya no aparece (prueba 10).
