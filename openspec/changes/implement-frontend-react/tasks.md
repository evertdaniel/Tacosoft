# Tasks: Implement React Frontend for Tacosoft

## Implementation Tasks

- [x] **1.1** [PR1] Vite React scaffold (`frontend/package.json`, `vite.config.ts`, `src/test/setup.ts`) — build/lint/test pass — 350 — —
- [x] **2.1** [PR2a] Axios + auth/tenant stores (`src/api/axios.ts`, `src/stores/auth.store.ts`, `tenant.store.ts`) — headers/401/persist/expiry — 260 — 1.1
- [x] **2.2** [PR2b] Login UI + protected routes (`src/api/auth.api.ts`, `src/features/auth/*`, `src/router/*`) — login + block dashboard — 240 — 2.1
- [x] **3.1** [PR3] Responsive shell + role nav (`src/components/layout/*`) — desktop sidebar; mobile 375px; role filter — 360 — 2.2
- [x] **4.1** [PR4] Dashboard hook + KPI page (`src/api/reports.api.ts`, `src/features/dashboard/*`) — dashboard; 4 KPIs/skeleton/error — 280 — 3.1
- [ ] **5.1** [PR5] Tables grid + WS topic (`src/api/tables.api.ts`, `src/features/tables/*`, `useWebSocket.ts`) — list/status; WS invalidates — 320 — 4.1
- [ ] **6.1** [PR6a] Menu list views (`src/features/menu/pages/*`, `src/api/menu.api.ts`) — section/category/product/option lists — 300 — 4.1
- [ ] **6.2** [PR6b] Menu create/edit forms (`src/features/menu/components/*Form.tsx`) — submit create/edit forms — 320 — 6.1
- [ ] **7.1** [PR7a] Orders list/create/detail (`src/features/orders/*`, `src/api/orders.api.ts`) — create order; detail — 340 — 5.1, 6.1
- [ ] **7.2** [PR7b] Kitchen + WS orders (`src/features/orders/pages/KitchenPage.tsx`, `useWebSocket.ts`) — WS refreshes kitchen — 280 — 7.1
- [ ] **8.1** [PR8] Billing invoices + payment (`src/features/billing/*`, `src/api/billing.api.ts`) — unpaid list; invoice pay — 280 — 7.1
- [ ] **9.1** [PR9] Cash registers + X/Z reports (`src/features/cash/*`, `src/api/cash.api.ts`) — open/close; X/Z — 260 — 4.1
- [ ] **10.1** [PR10a] Sales/product/financial reports (`src/features/reports/pages/*ReportPage.tsx`, `DateRangeFilter.tsx`) — charts/empty state — 300 — 4.1
- [ ] **10.2** [PR10b] Footfall/staff planning reports (`src/features/reports/pages/*ReportPage.tsx`) — date filter reports — 240 — 10.1
- [ ] **11.1** [PR11] Supplier CRUD (`src/features/suppliers/*`, `src/api/suppliers.api.ts`) — search, create, deactivate — 260 — 4.1
- [ ] **12.1** [PR12] PWA manifest + offline test (`vite.config.ts`, `public/manifest.json`, `src/test/pwa-offline.test.tsx`) — manifest; offline pass — 220 — 11.1

## Review Workload Forecast

| Field | Value |
|---|---|
| Changed lines | 4.5k–6k (no lockfiles) |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | 1 → 2a → 2b → 3 → 4 → 5 → 6a → 6b → 7a → 7b → 8 → 9 → 10a → 10b → 11 → 12 |
| Chain strategy | stacked-to-main |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: stacked-to-main
400-line budget risk: High

### Estimated Lines per Work Unit

| PR | Branch | Est. lines |
|---|---|---|
| 1 | frontend/scaffold | 350 |
| 2a | frontend/auth-core | 260 |
| 2b | frontend/auth-ui | 240 |
| 3 | frontend/shell | 360 |
| 4 | frontend/dashboard | 280 |
| 5 | frontend/tables | 320 |
| 6a | frontend/menu-read | 300 |
| 6b | frontend/menu-write | 320 |
| 7a | frontend/orders-core | 340 |
| 7b | frontend/kitchen | 280 |
| 8 | frontend/billing | 280 |
| 9 | frontend/cash | 260 |
| 10a | frontend/reports-charts | 300 |
| 10b | frontend/reports-staff | 240 |
| 11 | frontend/suppliers | 260 |
| 12 | frontend/pwa | 220 |
