import { useEffect, useState } from 'react';
import { getEnvironment } from '../api/client';

export function EnvironmentBadge() {
  const [environment, setEnvironment] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let active = true;
    getEnvironment()
      .then((name) => active && setEnvironment(name))
      .catch(() => active && setFailed(true));
    return () => {
      active = false;
    };
  }, []);

  if (failed) return <span className="badge badge--unknown">Ambiente desconocido</span>;
  if (environment === null) return <span className="badge">Cargando ambiente…</span>;
  return <span className="badge">Ambiente: {environment}</span>;
}
