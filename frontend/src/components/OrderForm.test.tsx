import { describe, expect, it } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { OrderForm } from './OrderForm';
import { server, http, HttpResponse } from '../test/server';

function lineInputs(index: number) {
  const row = screen.getAllByRole('group', { name: /línea/i })[index];
  return {
    sku: within(row).getByLabelText(/sku/i) as HTMLInputElement,
    quantity: within(row).getByLabelText(/cantidad/i) as HTMLInputElement,
    unitPrice: within(row).getByLabelText(/precio unitario/i) as HTMLInputElement,
  };
}

describe('OrderForm', () => {
  // PF04 – reproduces the reported bug from the UI side:
  // the screen shows exactly what the backend returns (single 10 % discount),
  // and never recalculates the discount on the client.
  it('PF04 shows subtotal, discount and total exactly as the API returns them', async () => {
    const user = userEvent.setup();
    render(<OrderForm />);

    await user.type(lineInputs(0).sku, 'RICE');
    await user.type(lineInputs(0).quantity, '2');
    await user.type(lineInputs(0).unitPrice, '50000');
    await user.click(screen.getByRole('button', { name: /agregar línea/i }));
    await user.type(lineInputs(1).sku, 'OIL');
    await user.type(lineInputs(1).quantity, '3');
    await user.type(lineInputs(1).unitPrice, '20000');
    await user.type(screen.getByLabelText(/descuento/i), '10');
    await user.click(screen.getByRole('button', { name: /calcular/i }));

    expect(await screen.findByTestId('subtotal')).toHaveTextContent('160.000');
    expect(screen.getByTestId('discount')).toHaveTextContent('16.000');
    expect(screen.getByTestId('total')).toHaveTextContent('144.000');
    expect(screen.getByTestId('total')).not.toHaveTextContent('129.600');
  });

  // PF05 – a 409 shows the backend message
  it('PF05 shows the insufficient-stock message when the API returns 409', async () => {
    server.use(
      http.post('/api/orders/total', () =>
        HttpResponse.json({ error: 'Insufficient stock for: RICE' }, { status: 409 }),
      ),
    );
    const user = userEvent.setup();
    render(<OrderForm />);

    await user.type(lineInputs(0).sku, 'RICE');
    await user.type(lineInputs(0).quantity, '6');
    await user.type(lineInputs(0).unitPrice, '1000');
    await user.click(screen.getByRole('button', { name: /calcular/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Insufficient stock for: RICE');
    expect(screen.queryByTestId('total')).not.toBeInTheDocument();
  });

  // PF06 – invalid data is blocked before calling the API
  it('PF06 does not call the API when there are no lines with data or the discount is out of range', async () => {
    let calls = 0;
    server.use(
      http.post('/api/orders/total', () => {
        calls += 1;
        return HttpResponse.json({ subtotal: 0, discount: 0, total: 0 });
      }),
    );
    const user = userEvent.setup();
    render(<OrderForm />);

    await user.click(screen.getByRole('button', { name: /calcular/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent(/al menos un ítem/i);

    await user.type(lineInputs(0).sku, 'RICE');
    await user.type(lineInputs(0).quantity, '1');
    await user.type(lineInputs(0).unitPrice, '1000');
    await user.type(screen.getByLabelText(/descuento/i), '101');
    await user.click(screen.getByRole('button', { name: /calcular/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/entre 0 y 100/i);
    expect(calls).toBe(0);
  });

  // PF07 – lines can be added and removed
  it('PF07 adds and removes order lines', async () => {
    const user = userEvent.setup();
    render(<OrderForm />);

    expect(screen.getAllByRole('group', { name: /línea/i })).toHaveLength(1);
    await user.click(screen.getByRole('button', { name: /agregar línea/i }));
    expect(screen.getAllByRole('group', { name: /línea/i })).toHaveLength(2);
    await user.click(screen.getAllByRole('button', { name: /quitar/i })[1]);
    await waitFor(() =>
      expect(screen.getAllByRole('group', { name: /línea/i })).toHaveLength(1),
    );
  });
});
