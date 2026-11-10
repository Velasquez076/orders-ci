import { describe, expect, it } from 'vitest';
import { validateOrder } from './validation';

const line = (sku: string, quantity: number, unitPrice: number) => ({ sku, quantity, unitPrice });

describe('validateOrder (mirrors contract rules R5 and discount range)', () => {
  // PF09 – rules from the OpenAPI schema are replicated in the UI
  it('PF09 accepts a valid order', () => {
    expect(validateOrder([line('RICE', 2, 50000)], 10)).toEqual([]);
  });

  it('PF09 rejects an empty order', () => {
    expect(validateOrder([], 0)).toContain('El pedido debe tener al menos un ítem.');
  });

  it('PF09 rejects quantity below 1 and a non-integer quantity', () => {
    expect(validateOrder([line('RICE', 0, 1000)], 0)).not.toEqual([]);
    expect(validateOrder([line('RICE', 1.5, 1000)], 0)).not.toEqual([]);
  });

  it('PF09 rejects a negative unit price and a blank SKU', () => {
    expect(validateOrder([line('RICE', 1, -1)], 0)).not.toEqual([]);
    expect(validateOrder([line('  ', 1, 1000)], 0)).not.toEqual([]);
  });

  it('PF09 rejects a discount outside 0–100', () => {
    expect(validateOrder([line('RICE', 1, 1000)], -1)).not.toEqual([]);
    expect(validateOrder([line('RICE', 1, 1000)], 100.01)).not.toEqual([]);
    expect(validateOrder([line('RICE', 1, 1000)], 100)).toEqual([]);
  });
});
