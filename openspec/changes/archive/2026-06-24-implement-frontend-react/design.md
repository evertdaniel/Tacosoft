# Design: Implement React Frontend for Tacosoft

## Technical Approach

Add `frontend/` as a Vite + React + TypeScript app. TanStack Query owns server state; Zustand owns UI state (auth, tenant, sidebar, websocket). Axios injects JWT and `x-restaurant-id`, clears on 401/403. Slice 1 delivers scaffold, auth, shell, and dashboard; later slices add domains, WebSocket, PWA.

## Architecture Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Build tool | Vite React TS | Fast HMR, PWA plugin. |
| Styling | Tailwind + Headless UI + Lucide | Small, responsive, accessible. |
| Server state | TanStack Query | Caching, sync built in. |
| Client state | Zustand | Minimal boilerplate. |
| HTTP client | Axios interceptors | JWT/tenant/401 handling. |
| Routing | React Router v6 | Nested routes, guards. |
| Token storage | `localStorage` | Default; document kiosk alt. |
| Pattern | Container/Presentational | Fast; scale later. |
| Folders | Feature-based `src/features/` | Domain isolation. |
| WebSocket | `@stomp/stompjs` + SockJS | Matches Spring broker. |
| Testing | Vitest + RTL + MSW | Native Vite, fast. |
| Tooling | ESLint + Prettier + strict TS | Matches backend discipline. |

## Data Flow & State Management

```
UI Event → Hook/Store → Axios → Backend
                ↓              ↑
         TanStack Query ←──────┘
                ↓
         Zustand (auth, tenant, sidebar)
```

Auth persists `token`, `user`, and `currentRestaurant` to `localStorage` and Zustand auth store; `restaurantRoles` initializes the tenant store. Interceptors read them; 401/403 resets and navigates to login. Token expiry is handled by decoding `exp` and scheduling logout; no silent refresh until the backend supports refresh tokens.

```
App → QueryClientProvider → RouterProvider
  /login → LoginPage → LoginForm
  / (ProtectedRoute) → Shell → TopBar/Sidebar/MobileNav/Outlet → DashboardPage → KpiGrid → KpiCard[4]
```

## File Changes (Slice 1)

| Category | Files | Description |
|----------|-------|-------------|
| Tooling | `package.json`, `vite.config.ts`, `tsconfig.json`, `tailwind.config.js`, `.eslintrc.cjs`, `.prettierrc`, `.env.example` | Config. |
| API | `api/axios.ts`, `auth.api.ts`, `reports.api.ts` | API clients. |
| State | `stores/auth.store.ts`, `tenant.store.ts`, `ui.store.ts` | Zustand stores. |
| Router | `router/index.tsx`, `guarded-routes.tsx` | Router. |
| Features | `features/auth/pages/LoginPage.tsx`, `components/LoginForm.tsx`, `dashboard/pages/DashboardPage.tsx`, `components/KpiCard.tsx`, `KpiGrid.tsx` | Auth, dashboard. |
| Layout | `components/layout/Shell.tsx`, `Sidebar.tsx`, `TopBar.tsx`, `MobileNav.tsx` | Shell. |
| UI | `components/ui/Button.tsx`, `Input.tsx`, `feedback/Loading.tsx`, `ErrorState.tsx` | UI. |
| Types | `types/domain.types.ts`, `api.types.ts` | DTOs. |
| Tests | `test/setup.ts`, `server.ts`, `fixtures.ts` | Tests. |

## Interfaces / Contracts

```typescript
// src/types/domain.types.ts
export type Role = 'ADMIN' | 'WAITER' | 'COOK' | 'CASHIER';
export interface RoleDto { id: string; name: Role; }
export interface RestaurantInfoDto { id: string; name: string; }
export interface RestaurantRoleDto { restaurantId: string; restaurantName: string; role: Role; }
export interface UserDto { id: string; username: string; firstName: string; lastName: string; primaryRole: RoleDto; restaurantRoles: RestaurantRoleDto[]; }
export interface LoginResponse { token: string; user: UserDto; currentRestaurant: RestaurantInfoDto; }
export interface DashboardReportDto { occupiedTables: number; activeOrders: number; closedOrdersToday: number; salesToday: number; totalTables: number; lowStockProducts: number; }

// src/stores/auth.store.ts — token, user, currentRestaurant; setAuth/logout.
// src/stores/tenant.store.ts — currentRestaurantId, currentRole, availableRoles; setTenant.
```

## Local Storage Schema

Keys are prefixed with `tacosoft:` and versioned (`v1`).

| Key | Value | Example |
|-----|-------|---------|
| `tacosoft:v1:token` | JWT string | `eyJhbG...` |
| `tacosoft:v1:user` | `UserDto` JSON | `{ "id": "...", ... }` |
| `tacosoft:v1:currentRestaurant` | `RestaurantInfoDto` JSON | `{ "id": "...", "name": "..." }` |
| `tacosoft:v1:restaurantRoles` | `RestaurantRoleDto[]` JSON | `[{ "restaurantId": "...", "role": "ADMIN" }]` |

Schema changes bump the version prefix and trigger logout of stale sessions.

## JWT Expiry Handling

- Decode `exp` claim on login and store expiry timestamp in the auth store.
- `setInterval` checks expiry every 60 seconds; on expiry, call `logout()` and redirect to `/login`.
- In-flight requests at expiry time will receive 401 and are handled by the response interceptor (clear storage + redirect) as a safety net.
- No silent refresh is implemented because the backend does not expose a refresh-token endpoint.

## Tenant Selector Visibility

- The tenant selector renders only when `restaurantRoles.length > 1`.
- For single-restaurant users, the header shows the restaurant name without a selector.
- Selecting a restaurant updates `currentRestaurantId` and `currentRole` in the tenant store by looking up the matching `restaurantRoles` entry; the Axios interceptor reads `currentRestaurantId` for `x-restaurant-id`.

## MSW Test Fixtures

`src/test/server.ts` sets up MSW with handlers for:
- `POST /auth/login` returning a `LoginResponse` fixture.
- `GET /reports/dashboard` returning a `DashboardReportDto` fixture.
- `POST /auth/login` with invalid credentials returning 401.

Fixtures live in `src/test/fixtures.ts` and mirror backend DTO shapes.

## Security Considerations

- **JWT storage**: `localStorage` default; use `sessionStorage` for shared kiosks.
- **XSS/CSRF**: React escapes output; JWT in header is cookie-free.
- **CORS**: Add production origin to `app.cors.allowed-origins`.
- **Tenant/role guards**: Every auth request carries `x-restaurant-id` from `tenantStore`; UI hides routes by role; backend is authoritative.

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Unit | Stores, interceptors, formatters | Vitest; mock `localStorage`. |
| Hook | `useLogin`, `useDashboardReport` | `renderHook` + MSW. |
| Component | `LoginForm`, `KpiCard`, `Sidebar` | RTL + user-event. |
| Integration | Login page, Dashboard, Shell | RTL + MSW + providers. |
| Coverage | Slice 1 files | 70% line threshold. |

Strict TDD: RED/GREEN/REFACTOR per first-slice unit. MSW mocks login and dashboard endpoints.

## WebSocket Architecture (Later Slices)

`useWebSocket` connects to `${API_BASE_URL}/ws` with SockJS/STOMP when a token exists, subscribes to `/topic/restaurant/{id}/orders` and `/topic/restaurant/{id}/tables`, and invalidates TanStack Query keys (`orders`, `tables`) on message. REST stays authoritative.

## PR Slicing Plan

| # | Branch | Focus | Depends |
|---|--------|-------|---------|
| 1 | `frontend/scaffold` | Vite + TS + Tailwind + ESLint/Prettier + Vitest + folder structure | — |
| 2 | `frontend/auth` | Login, auth store, protected routes, Axios interceptors, logout | #1 |
| 3 | `frontend/shell` | Responsive layout, sidebar, topbar, mobile nav, tenant selector, role guards | #2 |
| 4 | `frontend/dashboard` | Dashboard page, KPI cards, report API hook, tests | #3 |
| 5 | `frontend/tables` | Table grid, status transitions, WebSocket table topic | #4 |
| 6 | `frontend/menu` | Menu CRUD (sections, categories, products, options) | #4 |
| 7 | `frontend/orders` | Orders, kitchen view, WebSocket order topic | #5, #6 |
| 8 | `frontend/billing` | Invoice list, payment flow | #7 |
| 9 | `frontend/cash` | Cash registers, X/Z reports | #4 |
| 10 | `frontend/reports` | Sales, product, financial, footfall, staff planning reports | #4 |
| 11 | `frontend/suppliers` | Supplier CRUD | #4 |
| 12 | `frontend/pwa` | Vite PWA plugin, manifest, service worker scaffolding | #11 |

Chain strategy: `stacked-to-main`. Split any slice over 400 lines into read/list and write/mutate sub-PRs.

## Migration / Rollout

No backend migration. Revert frontend PRs. Version `localStorage` keys on schema changes.

## Open Questions

- [ ] Shared devices: switch default to `sessionStorage`?
- [ ] Production frontend origin for CORS?
- [ ] Product images: CDN or local uploads?
