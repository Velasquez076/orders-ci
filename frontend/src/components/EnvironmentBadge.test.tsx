import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { EnvironmentBadge } from './EnvironmentBadge';
import { server, http, HttpResponse } from '../test/server';

describe('EnvironmentBadge', () => {
  // PF08 – shows the environment reported by /api/info
  it('PF08 renders the active environment from /api/info', async () => {
    server.use(http.get('/api/info', () => HttpResponse.json({ environment: 'staging' })));

    render(<EnvironmentBadge />);

    expect(await screen.findByText(/staging/i)).toBeInTheDocument();
  });

  it('shows a neutral message when /api/info is unreachable', async () => {
    server.use(http.get('/api/info', () => HttpResponse.error()));

    render(<EnvironmentBadge />);

    expect(await screen.findByText(/ambiente desconocido/i)).toBeInTheDocument();
  });
});
