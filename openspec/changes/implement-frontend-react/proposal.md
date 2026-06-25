# Proposal: Implement React Frontend for Tacosoft

## Intent

Tacosoft currently exposes a Spring Boot REST + WebSocket backend but has no end-user frontend. This change delivers a web application so restaurant staff can authenticate, switch tenants/roles, and operate all backend domains from a single responsive interface.

## Scope

### In Scope
- Vite + React + TypeScript project scaffold under `frontend/`.
- JWT login, protected routes, Axios interceptors for `Authorization` and `x-restaurant-id`.
- Multi-tenant role switching via shell header and tenant store.
- Responsive app shell: sidebar, topbar, mobile navigation, role-based nav visibility.
- Dashboard skeleton consuming `/reports/dashboard`.
- Domain CRUD/operations: tables, menu, orders, billing, cash registers, reports, suppliers.
- Real-time updates via STOMP/SockJS WebSocket, refreshing TanStack Query caches.
- Strict TDD: Vitest + React Testing Library + MSW for hooks, stores, components, and pages.
- 12 stacked PRs to `main`, each under 400 changed lines.

### Out of Scope
- Production deployment/hosting configuration.
- Native mobile application.
- Advanced offline support beyond PWA manifest and service-worker scaffolding in slice 12.
- Backend API changes.

## Capabilities

### New Capabilities
- `frontend-auth`: login, JWT storage, logout on 401/403, role decoding.
- `frontend-shell`: responsive layout, tenant/role selector, navigation guards.
- `frontend-dashboard`: KPI cards and date-filtered dashboard report.
- `frontend-tables`: table grid, status transitions, layout positioning.
- `frontend-menu`: section/category/product/option CRUD list views.
- `frontend-orders`: order creation, list, detail status, kitchen view.
- `frontend-billing`: invoice list, payment flow.
- `frontend-cash`: open/close register, X/Z report summaries.
- `frontend-reports`: sales, product, financial, footfall, staff planning charts.
- `frontend-suppliers`: supplier CRUD.
- `frontend-websocket`: STOMP connection and cache invalidation.
- `frontend-pwa`: manifest and service-worker scaffolding.

### Modified Capabilities
- None (pure frontend addition).

## Approach

Use the exploration-recommended stack: Vite, React, TypeScript strict, Tailwind CSS + Headless UI, React Router v6, Axios, TanStack Query, Zustand, `@stomp/stompjs` + `sockjs-client`, Vitest + RTL + MSW.

- Server state lives in TanStack Query; global UI state (auth, tenant, sidebar, websocket status) in Zustand.
- Axios interceptors attach JWT and tenant header; 401/403 clears `localStorage` and redirects to `/login`.
- Token and selected restaurant are stored in `localStorage`; shared-device deployments should later consider `sessionStorage`.
- WebSocket messages are treated defensively and trigger cache refreshes from REST as the source of truth.
- Feature-based folders under `src/features/` keep domains isolated.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `frontend/` | New | Complete React application scaffold and source. |
| `openspec/specs/` | New | Frontend capability specs for auth, shell, dashboard, domains, websocket, PWA. |
| `openspec/changes/implement-frontend-react/` | New | Proposal, specs, design, tasks, verification. |

## First-Slice Boundaries

The first slice is split into four stacked PRs to stay within the 400-line review budget while establishing the foundation:

- **PR #1 (`frontend/scaffold`)**: Vite + React + TypeScript + Tailwind + ESLint/Prettier + Vitest setup.
- **PR #2 (`frontend/auth`)**: Login page, auth store, protected route wrapper, Axios interceptors.
- **PR #3 (`frontend/shell`)**: Responsive shell with sidebar/topbar, tenant selector, role-based navigation.
- **PR #4 (`frontend/dashboard`)**: Dashboard page fetching `/reports/dashboard`, KPI cards, tests.

Each PR includes its own tests under Strict TDD.

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| JWT storage on shared devices | Medium | Document `localStorage` default; recommend `sessionStorage` for shared kiosks. |
| WebSocket payload typing drift | Medium | Treat payloads defensively; refresh from REST on every event. |
| Large slice exceeds 400-line budget | Medium | Split read/list and write/mutate into sub-PRs; keep boilerplate in PR #1. |
| CORS origin mismatch in production | Low | Add deployment note to update `app.cors.allowed-origins`. |
| Mobile layout complexity | Low | Mobile-first Tailwind; validate on common breakpoints in tests/stories. |
| JWT expiry handling | Medium | Design scheduled logout before token expiry; no silent refresh until backend supports it. |

## Rollback Plan

- Revert the merged PR branch(es); backend remains untouched.
- If frontend is already deployed, point traffic away from the static build or remove the build artifact.
- `localStorage` keys are namespaced; clear via logout or version-bump key prefix if schema changes.

## Dependencies
- Backend running at `http://localhost:8080` with CORS allowing the frontend origin.
- Node.js 20+ and npm/pnpm for frontend tooling.

## Success Criteria

- [ ] Users can log in, switch restaurants/roles, and view the dashboard.
- [ ] Every domain endpoint has a corresponding read or write screen.
- [ ] All slices include passing Vitest + RTL tests; coverage ≥ 70% per slice.
- [ ] 401/403 clears storage and redirects to login.
- [ ] WebSocket events update orders and tables without full page reload.
- [ ] UI is usable on 320 px mobile and 1440 px desktop.
