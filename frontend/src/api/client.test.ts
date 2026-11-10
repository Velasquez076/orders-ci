import { describe, expect, it } from 'vitest';
import { calculateTotal, getEnvironment, ApiRequestError } from './client';
import { server, http, HttpResponse } from '../test/server';

const order = {
  items: [
    { sku: 'RICE', quantity: 2, unitPrice: 50000 },
    { sku: 'OIL', quantity: 3, unitPrice: 20000 },
  ],
  discountPercentage: 10,
};

describe('API client', () => {
  // PF01 – sends the order and returns the backend result without changes
  it('PF01 posts the order to /api/orders/total and returns the result', async () => {
    let sent: unknown;
    server.use(
      http.post('/api/orders/total', async ({ request }) => {
        sent = await request.json();
        return HttpResponse.json({ subtotal: 160000, discount: 16000, total: 144000 });
      }),
    );

    const result = await calculateTotal(order);

    expect(sent).toEqual(order);
    expect(result).toEqual({ subtotal: 160000, discount: 16000, total: 144000 });
  });

  // PF02 – a 409 becomes an ApiRequestError with the backend message
  it('PF02 maps 409 to ApiRequestError with the backend message', async () => {
    server.use(
      http.post('/api/orders/total', () =>
        HttpResponse.json({ error: 'Insufficient stock for: RICE' }, { status: 409 }),
      ),
    );

    const error = await calculateTotal(order).catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiRequestError);
    expect((error as ApiRequestError).status).toBe(409);
    expect((error as ApiRequestError).message).toBe('Insufficient stock for: RICE');
  });

  // PF03 – a 400 without an `error` field still produces a readable message
  it('PF03 falls back to a generic message when a 400 has no error field', async () => {
    server.use(
      http.post('/api/orders/total', () => HttpResponse.text('bad json', { status: 400 })),
    );

    const error = await calculateTotal(order).catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiRequestError);
    expect((error as ApiRequestError).status).toBe(400);
    expect((error as ApiRequestError).message).toMatch(/no son válidos/i);
  });

  it('reads the active environment from /api/info', async () => {
    await expect(getEnvironment()).resolves.toBe('development');
  });
});
