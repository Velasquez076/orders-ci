# Orders frontend

React + TypeScript front for the Orders API (`/api/orders/total`, `/api/info`).

## Commands

```bash
npm install
npm run gen:types   # regenerates src/api/schema.d.ts from contract/openapi.yaml
npm test            # runs the automated tests (single command)
npm run dev         # dev server; /api is proxied to http://localhost:8080
npm run build       # type check + production build
```

Set `VITE_PROXY_TARGET` to point the dev proxy at another backend (e.g. staging).
Set `VITE_API_BASE_URL` at build time when the API is served from a different origin.

## Contract

`contract/openapi.yaml` is a copy of `docs/openapi.yaml` from the backend repository
(`Velasquez076/orders-ci`). Update it and run `npm run gen:types` whenever the contract changes.

## Test map (defined before the code)

| Id | Behavior |
|----|----------|
| PF01 | Sends the order to `/api/orders/total` and returns the result unchanged |
| PF02 | A 409 becomes an error carrying the backend message |
| PF03 | A 400 without an `error` field shows a generic message |
| PF04 | Subtotal, discount and total are shown exactly as the API returns them (reproduces the discount bug) |
| PF05 | A 409 is shown to the user and no total is displayed |
| PF06 | Empty orders and discounts outside 0–100 are blocked before calling the API |
| PF07 | Order lines can be added and removed |
| PF08 | The active environment from `/api/info` is shown; an unreachable endpoint shows a neutral label |
| PF09 | Contract rules (items, quantity, price, SKU, discount range) are validated in the UI |

The UI never recalculates subtotal, discount or total: the backend is the only source of money values.
