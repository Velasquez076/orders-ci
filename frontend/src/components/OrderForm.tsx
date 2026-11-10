import { useId, useState, type FormEvent } from 'react';
import { ApiRequestError, calculateTotal, type OrderResult } from '../api/client';
import { validateOrder, type LineInput } from '../validation';

interface Line {
  key: number;
  sku: string;
  quantity: string;
  unitPrice: string;
}

// The contract does not mark OrderResult fields as required, so a missing value shows a dash.
const formatMoney = (value: number | undefined) =>
  value === undefined ? '—' : new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 }).format(value);

let nextKey = 1;
const emptyLine = (): Line => ({ key: nextKey++, sku: '', quantity: '', unitPrice: '' });

/** Lines the user has not touched at all are ignored instead of reported. */
const isBlank = (line: Line) => line.sku.trim() === '' && line.quantity === '' && line.unitPrice === '';

export function OrderForm() {
  const discountId = useId();
  const [lines, setLines] = useState<Line[]>([emptyLine()]);
  const [discount, setDiscount] = useState('0');
  const [result, setResult] = useState<OrderResult | null>(null);
  const [errors, setErrors] = useState<string[]>([]);
  const [submitting, setSubmitting] = useState(false);

  const updateLine = (key: number, field: keyof Omit<Line, 'key'>, value: string) =>
    setLines((current) => current.map((l) => (l.key === key ? { ...l, [field]: value } : l)));

  const removeLine = (key: number) =>
    setLines((current) => current.filter((l) => l.key !== key));

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setResult(null);

    const filled = lines.filter((l) => !isBlank(l));
    const inputs: LineInput[] = filled.map((l) => ({
      sku: l.sku,
      quantity: Number(l.quantity),
      unitPrice: Number(l.unitPrice),
    }));
    const discountValue = discount === '' ? 0 : Number(discount);

    const validation = validateOrder(inputs, discountValue);
    if (validation.length > 0) {
      setErrors(validation);
      return;
    }

    setErrors([]);
    setSubmitting(true);
    try {
      const response = await calculateTotal({
        items: inputs.map((i) => ({ sku: i.sku.trim(), quantity: i.quantity, unitPrice: i.unitPrice })),
        discountPercentage: discountValue,
      });
      setResult(response);
    } catch (error) {
      setErrors([error instanceof ApiRequestError ? error.message : 'No se pudo calcular el pedido.']);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      {lines.map((line, index) => (
        <fieldset key={line.key}>
          <legend>Línea {index + 1}</legend>
          <label>
            SKU
            <input value={line.sku} onChange={(e) => updateLine(line.key, 'sku', e.target.value)} />
          </label>
          <label>
            Cantidad
            <input
              type="number"
              min={1}
              step={1}
              value={line.quantity}
              onChange={(e) => updateLine(line.key, 'quantity', e.target.value)}
            />
          </label>
          <label>
            Precio unitario
            <input
              type="number"
              min={0}
              step="any"
              value={line.unitPrice}
              onChange={(e) => updateLine(line.key, 'unitPrice', e.target.value)}
            />
          </label>
          <button type="button" aria-label={`Quitar línea ${index + 1}`} onClick={() => removeLine(line.key)}>
            Quitar
          </button>
        </fieldset>
      ))}

      <button type="button" onClick={() => setLines((current) => [...current, emptyLine()])}>
        Agregar línea
      </button>

      <label htmlFor={discountId}>Descuento (%)</label>
      <input
        id={discountId}
        type="number"
        min={0}
        max={100}
        step="any"
        value={discount}
        onChange={(e) => setDiscount(e.target.value)}
      />

      <button type="submit" disabled={submitting}>
        {submitting ? 'Calculando…' : 'Calcular'}
      </button>

      {errors.length > 0 && (
        <div role="alert">
          {errors.map((message) => (
            <p key={message}>{message}</p>
          ))}
        </div>
      )}

      {result && (
        <dl aria-label="Resultado">
          <dt>Subtotal</dt>
          <dd data-testid="subtotal">{formatMoney(result.subtotal)}</dd>
          <dt>Descuento</dt>
          <dd data-testid="discount">{formatMoney(result.discount)}</dd>
          <dt>Total</dt>
          <dd data-testid="total">{formatMoney(result.total)}</dd>
        </dl>
      )}
    </form>
  );
}
