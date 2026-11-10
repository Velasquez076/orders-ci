export interface LineInput {
  sku: string;
  quantity: number;
  unitPrice: number;
}

/**
 * Mirrors the rules declared in contract/openapi.yaml (OrderItem / OrderRequest).
 * The backend remains the source of truth; this only gives early feedback.
 * It never calculates money values.
 */
export function validateOrder(items: LineInput[], discountPercentage: number): string[] {
  const errors: string[] = [];

  if (items.length === 0) {
    errors.push('El pedido debe tener al menos un ítem.');
  }

  items.forEach((item, index) => {
    const n = index + 1;
    if (item.sku.trim() === '') {
      errors.push(`Línea ${n}: el SKU es obligatorio.`);
    }
    if (!Number.isInteger(item.quantity) || item.quantity < 1) {
      errors.push(`Línea ${n}: la cantidad debe ser un entero mayor que cero.`);
    }
    if (!Number.isFinite(item.unitPrice) || item.unitPrice < 0) {
      errors.push(`Línea ${n}: el precio unitario no puede ser negativo.`);
    }
  });

  if (!Number.isFinite(discountPercentage) || discountPercentage < 0 || discountPercentage > 100) {
    errors.push('El descuento debe estar entre 0 y 100.');
  }

  return errors;
}
