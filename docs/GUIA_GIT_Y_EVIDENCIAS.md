# Guía de Git y de evidencias

> Reemplacen `<URL>` por la dirección del repositorio y los nombres por los de cada integrante.
> Cada integrante hace sus commits **desde su propio equipo**, para que el historial muestre autoría propia.

## 0. Configuración (cada integrante, una vez)

```bash
git config --global user.name  "Nombre Apellido"
git config --global user.email "correo-asociado-a-su-cuenta@ejemplo.com"
```

## 1. Orden sugerido de commits (desarrollo guiado por pruebas)

| Orden | Quién | Qué sube | Mensaje sugerido |
|-------|-------|----------|------------------|
| 1 | Integrante A | `.gitignore`, `README.md`, `docs/ESPECIFICACION.md` | `docs: especificación del comportamiento esperado` |
| 2 | Integrante B | `pom.xml`, `OrdersApplication.java`, `OrderCalculatorTest.java` | `test: pruebas del cálculo de total con descuentos (rojo)` |
| 3 | Integrante C | `model/*`, `OrderCalculator.java` | `feat: calculadora de pedido (verde)` |
| 4 | Integrante D | `InventoryServiceTest.java`, `InventoryService.java`, `InventoryItem.java`, `InventoryRepository.java` | `feat: validación de existencias con base de datos y pruebas` |
| 5 | Integrante A | `OrderServiceTest.java`, `OrderService.java`, controladores y pruebas web | `feat: caso de uso de pedido y API REST` |
| 6 | Integrante B | `Dockerfile`, `.dockerignore` | `build: Dockerfile con etapa de pruebas y de ejecución` |
| 7 | Integrante C | `application*.properties`, configuración JPA/DataSource | `config: perfiles dev, test y prod con base de datos real` |
| 8 | Integrante D | `.github/workflows/ci.yml`, `scripts/evidencias.sh` | `ci: ejecutar mvn test en cada push` |

El commit 2 debe hacerse **antes** que el 3: así el historial demuestra que las pruebas se escribieron primero.

Cada commit:

```bash
git add <archivos>
git commit -m "mensaje"
git pull --rebase origin main
git push origin main
```

Primer integrante (crea el repositorio vacío en GitHub y lo conecta):

```bash
git init -b main
git remote add origin <URL>
```

## 2. Evidencia: recuperar una versión anterior de un archivo

```bash
# Alguien edita mal OrderCalculator.java (por ejemplo, aplica el descuento dos veces) y lo sube
git log --oneline -- src/main/java/com/stores/orders/service/OrderCalculator.java
mvn test                                  # la prueba CP04 debe FALLAR con el cambio erróneo
git restore --source=<HASH_BUENO> -- src/main/java/com/stores/orders/service/OrderCalculator.java
mvn test                                  # vuelve a pasar
git commit -am "fix: restaura OrderCalculator a la versión correcta"
```

Guarden la salida completa (con `git log` y los dos `mvn test`) en `evidencias/02_recuperar_version.txt`.

## 3. Evidencia: clonar en otro directorio/equipo y ejecutar las pruebas

```bash
cd /tmp   # o en otro equipo
git clone <URL> clon-pedidos
cd clon-pedidos
mvn test
```

Guarden la salida en `evidencias/03_clonar_y_probar.txt`.

## 4. Evidencia: contenedor

```bash
./scripts/evidencias.sh tests        # mvn test local con fecha  -> evidencias/01_local_tests.txt
./scripts/evidencias.sh docker      # build + run de pruebas     -> evidencias/05_docker_tests.txt
```

Para demostrar qué cambia entre ambientes con la **misma imagen**:

La API necesita PostgreSQL, así que se levanta con Docker Compose (contenedor `api` + contenedor `db`):

```bash
cp .env.example .env                       # credenciales propias, .env no se sube al repo
docker compose up -d --build
curl http://localhost:8080/api/info        # {"environment":"development"}
SPRING_PROFILES_ACTIVE=prod docker compose up -d api
curl http://localhost:8080/api/info        # {"environment":"production"}
docker compose down
```

Errores encontrados al dockerizar y su corrección: [`DOCKER_INCIDENCIAS.md`](DOCKER_INCIDENCIAS.md).

## 5. Antes de entregar

- Revisar que no haya contraseñas, claves ni datos reales en el repositorio ni en las capturas.
- Que la fecha sea visible y legible en cada captura o salida.
- Si algo falla, **no lo borren**: documenten el error y la corrección; cuenta como evidencia.
