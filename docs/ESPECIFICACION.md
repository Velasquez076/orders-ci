# Especificación del comportamiento esperado (se define ANTES de programar)

Aplicación mínima que representa una parte del sistema de pedidos de la cadena de tiendas de barrio:
**cálculo del total de un pedido con descuento** y **validación de existencias**.

## Reglas de negocio

| Id | Regla |
|----|-------|
| R1 | Subtotal = suma de (cantidad × precio unitario) de cada ítem. |
| R2 | El descuento es un porcentaje entre 0 y 100 y se aplica **una sola vez**, sobre el subtotal del pedido. |
| R3 | Total = subtotal − descuento. Nunca es negativo. |
| R4 | Los valores se redondean a 2 decimales (HALF_UP). |
| R5 | Un pedido debe tener al menos un ítem; cantidad > 0; precio ≥ 0. |
| R6 | Un pedido solo se acepta si hay existencias suficientes de cada SKU (se suman las cantidades del mismo SKU). Las existencias se consultan en la base de datos (tabla `inventory`). |

## Casos esperados (entrada → resultado)

| Prueba | Entrada | Resultado esperado |
|--------|---------|--------------------|
| CP01 | 2 × 10.000 + 3 × 5.000 | Subtotal 35.000 |
| CP02 | 3 × 10.000, descuento 0 % | Total 30.000,00 |
| CP03 | 1 × 100.000, descuento 10 % | Total 90.000,00 |
| CP04 (error del caso) | 2 × 50.000 + 3 × 20.000, descuento 10 % | Total 144.000,00 (NO 129.600, que resulta de aplicar el descuento dos veces) |
| CP05 | 2 × 10.000, descuento 100 % | Total 0,00 |
| CP06 | descuento 100,01 % | Error de validación |
| CP07 | descuento −5 % | Error de validación |
| CP08 | 3 × 3.333,33, descuento 15 % | Total 8.499,99 |
| CP09 | pedido sin ítems | Error de validación |
| IN01 | existencias 10, piden 4 | Hay existencias |
| IN02 | existencias 10, piden 10 | Hay existencias |
| IN03 | existencias 10, piden 11 | No hay existencias |
| IN04 | SKU inexistente | No hay existencias |
| IN05 | descontar 3 de 10 | Quedan 7 |
| IN06 | descontar 11 de 10 | Error; el inventario no cambia |
| PS01 | caso CP04 con existencias suficientes | Subtotal 160.000, descuento 16.000, total 144.000 |
| PS02 | piden 6, existen 5 | Error de existencias (409) |
| PS03 | mismo SKU en dos líneas (3 + 3), existen 5 | Error de existencias |
| PS04 | procesar un pedido | El inventario no cambia |
| WE01–WE03 | POST /api/orders/total | 200 con total / 409 sin existencias / 400 datos inválidos |
