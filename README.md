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

The `dev` profile defaults `DB_URL` to a local PostgreSQL (user and password are still required); `test` uses an in-memory H2 database; `prod`
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

Requires JDK 21 or higher and Maven 3.9+. Without Java/Maven installed, run them in Docker
(see [Automated tests inside a container](#1-automated-tests-inside-a-container)).

## With Docker (reproducible environment)

The project runs as a **multi-container application**: one container for the API and one for
PostgreSQL, orchestrated with Docker Compose.

```
docker compose (project "orders-ci")
 ├─ db   postgres:16-alpine   volume orders-ci_db-data, healthcheck pg_isready
 └─ api  orders-app :8080     DB_URL=jdbc:postgresql://db:5432/orders, starts when db is healthy

orders-tests (separate image)  runs `mvn test` with in-memory H2, no database container
```

| File | Purpose |
|------|---------|
| [`Dockerfile`](Dockerfile) | Multi-stage build: `tests` (runs `mvn test`), `build` (packages the jar), `runtime` (JRE 21 image, default target) |
| [`docker-compose.yml`](docker-compose.yml) | Services `db` (PostgreSQL 16, with healthcheck) and `api` (runtime image, starts when `db` is healthy) |
| [`.env.example`](.env.example) | Template for the variables; copy it to `.env` (git-ignored, never committed) |
| [`db/init/01-inventory.sql`](db/init/01-inventory.sql) | Creates `inventory` and loads sample stock (`RICE`=100, `OIL`=50, `SUGAR`=5) the first time the DB volume is created |
| [`scripts/evidencias.sh`](scripts/evidencias.sh) | Runs the tests and saves the dated output in `evidencias/` |

### 0) Prerequisites: install Docker Desktop (Ubuntu)

Docker Desktop includes the Docker engine, `docker compose` and a graphical dashboard to see the
containers. Tested on Ubuntu 26.04 (x86_64). Official guide:
<https://docs.docker.com/desktop/setup/install/linux/ubuntu/>

```bash
# 1. Virtualization permission (Docker Desktop runs a small VM through KVM)
sudo usermod -aG kvm $USER

# 2. Docker's official apt repository
sudo apt-get update
sudo apt-get install -y ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update

# 3. If Ubuntu's own compose package is installed, remove it (it conflicts, see Troubleshooting)
sudo apt-get remove -y docker-compose-v2

# 4. Download and install Docker Desktop
cd ~/Downloads
curl -LO https://desktop.docker.com/linux/main/amd64/docker-desktop-amd64.deb
sudo apt-get install -y ./docker-desktop-amd64.deb
```

Open **Docker Desktop** from the applications menu (or `systemctl --user start docker-desktop`)
and accept the terms. Check it:

```bash
docker context ls        # "desktop-linux" must be marked with *
docker compose version
```

> Logging out is only needed if Docker Desktop reports a KVM/virtualization error: the new `kvm`
> group applies to new sessions. On a desktop session Ubuntu usually already grants access to
> `/dev/kvm` (`getfacl /dev/kvm` shows `user:<you>:rw-`).

### There are two kinds of tests

| I want to... | Command | Needs the database container? |
|---|---|---|
| Run the **25 automated tests** (JUnit: discounts, stock, endpoints, OpenAPI) | `./scripts/evidencias.sh docker` | **No** – uses in-memory H2 |
| **Try the running application** (API + PostgreSQL) | `docker compose up -d --build`, then `curl` / Swagger | **Yes** |

All commands are run from the **repository root** (the `orders-ci/` folder). In VS Code use
**File → Open Folder → `orders-ci`** and every new terminal (`` Ctrl+` ``) opens there.

### 1) Automated tests inside a container

```bash
./scripts/evidencias.sh docker
```

What it does:

1. Builds the image `orders-tests` (`docker build --target tests`).
2. Runs `mvn test` inside a container (`orders-tests-ctr`, removed when it finishes).
3. Saves the full output, with the date, in `evidencias/05_docker_tests.txt` (delivery evidence).

Expected result at the end of the output:

```
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The first run takes a few minutes (downloads Maven, the JDK and the dependencies); later runs use
the cache. Equivalent manual commands:

```bash
docker build --target tests -t orders-tests .
docker run --rm orders-tests
```

> The `./` prefix is required: `evidencias.sh docker` alone gives `command not found`, because the
> shell does not look for commands in the current folder. The script moves to the repository root
> by itself, so `./evidencias.sh docker` from inside `scripts/` also works.

### 2) Run the application (API + database)

```bash
cp .env.example .env                  # first time only; set your own DB_USER / DB_PASSWORD
docker compose up -d --build          # builds orders-app and starts db + api
docker compose ps                     # wait until both services show "healthy" (~20 s)
```

Try the API (each command shows the expected response):

```bash
# Active environment
curl http://localhost:8080/api/info
# {"environment":"development"}

# Order with enough stock -> 200
curl -X POST http://localhost:8080/api/orders/total \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"RICE","quantity":2,"unitPrice":50000},{"sku":"OIL","quantity":3,"unitPrice":20000}],"discountPercentage":10}'
# {"subtotal":160000.00,"discount":16000.00,"total":144000.00}

# Order without enough stock (only 5 SUGAR) -> 409
curl -X POST http://localhost:8080/api/orders/total \
  -H "Content-Type: application/json" \
  -d '{"items":[{"sku":"SUGAR","quantity":10,"unitPrice":3000}],"discountPercentage":0}'
# {"error":"Insufficient stock for: SUGAR"}
```

Without the terminal: open <http://localhost:8080/swagger-ui.html>, choose an endpoint,
**Try it out** → **Execute**.

Query the database directly:

```bash
docker compose exec db psql -U orders_user -d orders -c "SELECT * FROM inventory;"
```

(use the `DB_USER` from your `.env`).

Same image, production profile (only the environment variables change):

```bash
SPRING_PROFILES_ACTIVE=prod docker compose up -d api
curl http://localhost:8080/api/info   # {"environment":"production"}
```

Useful commands:

| Command | What it does |
|---------|--------------|
| `docker compose ps` | State of the containers |
| `docker compose logs -f api` | Follow the API logs (`Ctrl+C` to exit) |
| `docker compose restart api` | Restart only the API |
| `docker compose down` | Stop and remove the containers (data is kept in the volume) |
| `docker compose down -v` | Also delete the database volume; sample data reloads on the next `up` |

### 3) See the containers in Docker Desktop

- **Containers**: group **`orders-ci`** with `orders-ci-db-1` (PostgreSQL) and `orders-ci-api-1`
  (API). Clicking one shows its **Logs**, **Inspect** (environment variables, network) and **Exec**
  (a terminal inside the container). The `8080:8080` link on `api` opens the API in the browser.
  While the automated tests run, `orders-tests-ctr` appears briefly.
- **Images**: `orders-app`, `orders-tests`, `postgres:16-alpine`.
- **Volumes**: `orders-ci_db-data` (database files).
- **Builds**: history of the image builds, with their logs.

### Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `evidencias.sh: command not found` | Missing `./` | `./scripts/evidencias.sh docker` |
| `failed to read dockerfile: open Dockerfile: no such file or directory` | `docker build` run outside the repository root | `cd` to `orders-ci/` (the current script already does it) |
| `trying to overwrite '/usr/libexec/docker/cli-plugins/docker-compose', which is also in package docker-compose-v2` (installing Docker Desktop) | Ubuntu's `docker-compose-v2` and Docker's `docker-compose-plugin` ship the same file | `sudo apt-get remove -y docker-compose-v2 && sudo apt-get install -f -y` |
| `Connection to localhost:5432 refused` | API image run alone (`docker run ... orders-app`): inside a container `localhost` is the container itself | Use `docker compose up` (the API reaches the database at `db:5432`) |
| `Set DB_USER in .env` | `.env` missing | `cp .env.example .env` |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | Something else uses port 8080 | Stop it, or set `API_PORT=8081` in `.env` |
| Orders answer `409` for every SKU | `inventory` empty (volume created before the init script) | `docker compose down -v && docker compose up -d` |

Execution results and the incidents found while testing: [`docs/DOCKER_INCIDENCIAS.md`](docs/DOCKER_INCIDENCIAS.md).

## Environments

A single image; the environment is selected with `SPRING_PROFILES_ACTIVE` (`dev`, `test`, `prod`).
Credentials and the database are injected through environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`);
**never** stored in the repository.
