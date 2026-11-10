import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';

// Default handlers mirror the backend contract (contract/openapi.yaml).
// Each test can override them with server.use(...).
export const server = setupServer(
  http.post('/api/orders/total', () =>
    HttpResponse.json({ subtotal: 160000, discount: 16000, total: 144000 }, { status: 200 }),
  ),
  http.get('/api/info', () => HttpResponse.json({ environment: 'development' })),
);

export { http, HttpResponse };
