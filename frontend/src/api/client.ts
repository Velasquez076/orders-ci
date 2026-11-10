import type { components } from './schema';

export type OrderRequest = components['schemas']['OrderRequest'];
export type OrderItem = components['schemas']['OrderItem'];
export type OrderResult = components['schemas']['OrderResult'];

export class ApiRequestError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiRequestError';
    this.status = status;
  }
}

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

const FALLBACK_MESSAGES: Record<number, string> = {
  400: 'Los datos no son válidos.',
  409: 'No hay existencias suficientes para este pedido.',
};
const DEFAULT_FALLBACK = 'Ocurrió un error inesperado en el servidor.';

async function readErrorMessage(response: Response): Promise<string> {
  const fallback = FALLBACK_MESSAGES[response.status] ?? DEFAULT_FALLBACK;
  try {
    const data: unknown = await response.json();
    const error = (data as { error?: unknown } | null)?.error;
    return typeof error === 'string' && error.length > 0 ? error : fallback;
  } catch {
    return fallback;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: { Accept: 'application/json', ...(init?.headers ?? {}) },
  });
  if (!response.ok) {
    throw new ApiRequestError(response.status, await readErrorMessage(response));
  }
  return (await response.json()) as T;
}

/** POST /api/orders/total – returns the backend result untouched. */
export function calculateTotal(body: OrderRequest): Promise<OrderResult> {
  return request<OrderResult>('/api/orders/total', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

/** GET /api/info – name of the active Spring profile. */
export async function getEnvironment(): Promise<string> {
  const data = await request<{ environment: string }>('/api/info');
  return data.environment;
}
