import { EnvironmentBadge } from './components/EnvironmentBadge';
import { OrderForm } from './components/OrderForm';

export default function App() {
  return (
    <main>
      <header>
        <h1>Cálculo de pedido</h1>
        <EnvironmentBadge />
      </header>
      <OrderForm />
    </main>
  );
}
