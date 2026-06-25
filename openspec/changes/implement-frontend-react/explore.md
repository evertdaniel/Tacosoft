# Exploration: Implement React Frontend for Tacosoft

## 1. Current State

Tacosoft is a Spring Boot 3.3 monolith (Java 21, Maven) exposing a stateless REST API and a WebSocket/STOMP broker. There is **no existing frontend** in the repository. Authentication is JWT-based (HS256, 120 min expiry) with RBAC roles `ADMIN`, `WAITER`, `COOK`, `CASHIER`. Every authenticated request (except login and docs) must carry both:

- `Authorization: Bearer <jwt>`
- `x-restaurant-id: <restaurant-id>`

CORS is already configured via `CorsConfig.java` using `app.cors.allowed-origins`. WebSocket is configured at `/ws` with SockJS fallback, broker prefix `/topic`, and application prefix `/app`.

## 2. Backend Capability Inventory

### 2.1 Auth & Users

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Auth | `POST /auth/login` | `LoginRequest`, `LoginResponse` |
| Users | `GET /users`, `GET /users/{id}`, `POST /users`, `PUT /users/{id}`, `DELETE /users/{id}`, `POST /users/{id}/roles`, `DELETE /users/{id}/roles/{restaurantId}/{roleId}` | `UserDto`, `CreateUserRequest`, `UpdateUserRequest`, `AssignRoleRequest` |

`LoginResponse` returns `{ token, user: UserDto, currentRestaurant: RestaurantInfoDto }`. `UserDto` carries `restaurantRoles: RestaurantRoleDto[]` for tenant/role switching.

### 2.2 Menu

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Sections | `GET /sections`, `GET /sections/active`, `GET /sections/{id}`, `POST /sections`, `PUT /sections/{id}`, `DELETE /sections/{id}` | `SectionDto`, `CreateSectionRequest`, `UpdateSectionRequest` |
| Categories | `GET /categories`, `GET /categories/active`, `GET /categories/{id}`, `POST /categories`, `PUT /categories/{id}`, `DELETE /categories/{id}` | `CategoryDto`, `CreateCategoryRequest`, `UpdateCategoryRequest` |
| Products | `GET /products`, `GET /products/active`, `GET /products/available`, `GET /products/category/{categoryId}`, `GET /products/{id}`, `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}` | `ProductDto`, `CreateProductRequest`, `UpdateProductRequest` |
| Product Options | `GET /product-options`, `GET /product-options/product/{productId}`, `GET /product-options/product/{productId}/available`, `GET /product-options/{id}`, `POST /product-options`, `PUT /product-options/{id}`, `DELETE /product-options/{id}` | `ProductOptionDto`, `CreateProductOptionRequest`, `UpdateProductOptionRequest` |
| Production Areas | `GET /production-areas`, `GET /production-areas/{id}`, `POST /production-areas`, `PUT /production-areas/{id}`, `DELETE /production-areas/{id}` | `ProductionAreaDto`, `CreateProductionAreaRequest`, `UpdateProductionAreaRequest` |

### 2.3 Tables

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Tables | `GET /tables`, `GET /tables/active`, `GET /tables/available`, `GET /tables/{id}`, `POST /tables`, `PUT /tables/{id}`, `PUT /tables/{id}/status`, `DELETE /tables/{id}` | `TableDto`, `CreateTableRequest`, `UpdateTableRequest`, `UpdateTableStatusRequest` |

### 2.4 Orders

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Orders | `GET /orders`, `GET /orders/active`, `GET /orders/status/{status}`, `GET /orders/{id}`, `POST /orders`, `PUT /orders/{id}`, `DELETE /orders/{id}` | `OrderDto`, `CreateOrderRequest` |
| Order Details | `GET /order-details/{id}`, `GET /order-details/order/{orderId}`, `PUT /order-details/{id}/status` | `OrderDetailDto`, `UpdateOrderDetailStatusRequest` |

### 2.5 Billing

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Invoices | `GET /invoices`, `GET /invoices/unpaid`, `GET /invoices/{id}`, `POST /invoices`, `POST /invoices/{id}/pay` | `InvoiceDto`, `CreateInvoiceRequest`, `PaymentRequest` |

### 2.6 Cash Registers

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Cash Registers | `GET /cash-registers`, `GET /cash-registers/active`, `GET /cash-registers/x-report`, `POST /cash-registers/open`, `PUT /cash-registers/{id}/close` | `CashRegisterDto`, `OpenCashRegisterRequest`, `CloseCashRegisterRequest`, `XReportDto`, `ZReportDto` |

### 2.7 Reports

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Reports | `GET /reports/dashboard`, `GET /reports/sales`, `GET /reports/products`, `GET /reports/finances`, `GET /reports/footfall`, `GET /reports/staff-planning` | `DashboardReportDto`, `SalesSummaryDto`, `ProductReportDto`, `FinancialReportDto`, `FootfallReportDto`, `StaffPlanningReportDto` |

### 2.8 Suppliers

| Resource | Endpoints | DTOs |
|----------|-----------|------|
| Suppliers | `GET /suppliers`, `GET /suppliers/active`, `GET /suppliers/search`, `GET /suppliers/{id}`, `POST /suppliers`, `PUT /suppliers/{id}`, `DELETE /suppliers/{id}` | `SupplierDto`, `CreateSupplierRequest`, `UpdateSupplierRequest` |

### 2.9 WebSocket/STOMP Topics

Broker prefix `/topic`. Current server broadcasts:

- `/topic/restaurant/{restaurantId}/orders` — `OrderDto` on creation, `OrderDetailDto` on detail change.
- `/topic/restaurant/{restaurantId}/orders/{orderId}` — `"ORDER_UPDATED"` string on order-level change.
- `/topic/restaurant/{restaurantId}/tables` — `TableDto` on table status change.

No client-to-server `/app` destinations are currently consumed by the backend. The WebSocket is used purely for server pushes.

## 3. Frontend Technology Options

### 3.1 Build Tool

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Vite** | Fast HMR, modern ESBuild/Rollup, PWA plugin, smaller config, better DX | Slightly newer ecosystem, some older plugins need updates | Low |
| Create React App | Familiar, well documented | Deprecated by React team, slow builds, ejection risk, poor PWA story | Low now, High later |

### 3.2 State Management

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Zustand** | Tiny, no boilerplate, TypeScript-friendly, excellent for client UI state | Less built-in devtools than Redux | Low |
| Redux Toolkit | Mature, powerful devtools, predictable for large teams | Verbose, overkill for many CRUD apps | Medium |
| React Query / TanStack Query | Caching, background sync, deduping server state | Complements, not replaces, UI state; needs Zustand/Context anyway | Medium |

### 3.3 Routing

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **React Router v6** | De facto standard, data APIs, nested routes, role-based lazy loading | Frequent major API changes historically | Low |

### 3.4 UI Library / Component System

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Tailwind CSS + Headless UI** | Utility-first, responsive, customizable, small bundle, accessible primitives | Markup can get verbose; needs design discipline | Low-Medium |
| Material UI | Complete component set, mature theming | Heavy bundle, opinionated look, licensing considerations | Low |
| Ant Design | Rich enterprise components | Heavy bundle, less PWA-friendly, opinionated | Medium |
| shadcn/ui | Copy-paste components, Tailwind-based, Radix primitives | Needs manual updates, newer ecosystem | Medium |

### 3.5 HTTP Client

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Axios** | Interceptors for JWT/tenant headers, request/response transforms, wide browser support | Extra dependency (small) | Low |
| fetch | Native, no dependency | Verbose interceptor pattern, no timeout/cancel by default | Low |

### 3.6 WebSocket Client

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **@stomp/stompjs + SockJS-client** | Matches Spring STOMP/SockJS config exactly, reconnect handling, Heartbeats | Extra deps, somewhat dated API | Medium |
| Native WebSocket | No dependency | Must re-implement STOMP framing and SockJS fallback | Medium-High |

### 3.7 Testing

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Vitest + React Testing Library** | Same config as Vite, fast, modern Jest-compatible API | Team may need to learn if coming from Jest | Low |
| Jest | Mature, widely known | Separate config from Vite, slower, more setup | Medium |

### 3.8 TypeScript

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Yes** | Type safety across DTOs, catches API drift, better IDE support, scales with domains | Slightly more setup, all examples must be typed | Low |
| No | Faster initial prototyping | Technical debt grows quickly in multi-domain app | Low now, High later |

### 3.9 PWA Readiness

| Option | Pros | Cons | Effort |
|--------|------|------|--------|
| **Vite PWA plugin** | Auto service worker, manifest, offline strategies, works with Vite | Needs asset/icon config | Low |
| Manual | Full control | Easy to get wrong, more code to maintain | Medium |

## 4. Recommended Stack

- **Build tool**: Vite (React + TypeScript template)
- **Language**: TypeScript (strict mode recommended)
- **Routing**: React Router v6 with data APIs
- **Styling**: Tailwind CSS + Headless UI + Lucide React icons
- **HTTP client**: Axios with request/response interceptors
- **Server state**: TanStack Query (React Query) for caching and synchronization
- **Client/UI state**: Zustand for auth, tenant, theme, sidebar, websocket status
- **WebSocket**: `@stomp/stompjs` + `sockjs-client`
- **Testing**: Vitest + React Testing Library + MSW for API mocking
- **PWA**: `vite-plugin-pwa` deferred to later slice; keep app shell PWA-ready from day one
- **Linting/Formatting**: ESLint + Prettier (align with backend Spotless discipline)

**Rationale**: This stack matches the backend's multi-tenant, real-time, RBAC characteristics without over-engineering. TanStack Query eliminates most caching/synchronization boilerplate for REST endpoints; Zustand keeps global UI state minimal; Tailwind enables rapid responsive design; STOMP/SockJS integrates directly with Spring's existing broker.

## 5. High-Level Frontend Architecture

### 5.1 Folder Structure

```
frontend/
├── public/
│   └── manifest.json              # PWA manifest (deferred)
├── src/
│   ├── api/
│   │   ├── axios.ts               # Configured instance with interceptors
│   │   ├── auth.api.ts
│   │   ├── users.api.ts
│   │   ├── menu.api.ts
│   │   ├── tables.api.ts
│   │   ├── orders.api.ts
│   │   ├── billing.api.ts
│   │   ├── cash.api.ts
│   │   ├── reports.api.ts
│   │   └── suppliers.api.ts
│   ├── components/
│   │   ├── ui/                    # Tailwind/Headless primitives (Button, Input, Modal)
│   │   ├── layout/                # Shell, Sidebar, TopBar, MobileNav
│   │   └── feedback/              # Loading, ErrorBoundary, Toast
│   ├── features/
│   │   ├── auth/
│   │   ├── menu/
│   │   ├── tables/
│   │   ├── orders/
│   │   ├── billing/
│   │   ├── cash/
│   │   ├── reports/
│   │   └── suppliers/
│   │       └── (each: pages/, components/, hooks/, types/)
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useTenant.ts
│   │   ├── useWebSocket.ts
│   │   └── useMediaQuery.ts
│   ├── stores/
│   │   ├── auth.store.ts          # Zustand
│   │   ├── tenant.store.ts
│   │   └── ui.store.ts
│   ├── types/
│   │   ├── api.types.ts
│   │   └── domain.types.ts        # Mirrors backend DTOs
│   ├── router/
│   │   ├── index.tsx
│   │   └── guarded-routes.tsx     # Role-based route guards
│   ├── utils/
│   │   ├── formatters.ts          # Currency, dates
│   │   └── validators.ts
│   ├── App.tsx
│   └── main.tsx
├── index.html
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```

### 5.2 Module Boundaries

Each domain under `src/features/` owns its pages, components, hooks, and local types. Shared concerns live in `src/components/`, `src/hooks/`, `src/stores/`, and `src/api/`. API clients are split by domain but share the base Axios instance.

### 5.3 Component Pattern

Use **container/presentational** over atomic design for speed:

- **Pages/Containers** connect to TanStack Query / Zustand and pass data down.
- **Presentational components** in `features/*/components/` and `components/ui/` receive props and emit events.

Atomic design can be adopted later if the component library grows.

### 5.4 Auth Flow

1. User posts credentials to `POST /auth/login`.
2. On success, store `token` in `localStorage` (or `sessionStorage` for shared-device safety).
3. Decode token to extract `exp` and schedule refresh/logout.
4. Persist selected `restaurantId` from `currentRestaurant` or `restaurantRoles`.
5. Axios interceptor attaches `Authorization` and `x-restaurant-id` to every request.
6. On 401/403, clear storage and redirect to `/login`.

### 5.5 Tenant Header Handling

- Zustand `tenantStore` holds `currentRestaurantId` and available `restaurantRoles`.
- A tenant selector in the top bar lets users switch restaurants.
- Axios interceptor reads `currentRestaurantId` and injects `x-restaurant-id`.
- Route guards hide menu items the user's current role cannot access, mirroring backend `@PreAuthorize`.

### 5.6 WebSocket Integration Pattern

- Single `useWebSocket` hook in `src/hooks/useWebSocket.ts`.
- Connects to `http://localhost:8080/ws` using SockJS, subscribes to topics under `/topic/restaurant/{restaurantId}/...`.
- On message, publishes typed events to a lightweight event bus or Zustand store.
- TanStack Query cache for orders/tables is invalidated/refreshed when relevant events arrive, keeping REST data authoritative.
- Heartbeat and reconnect handled by STOMP client.

## 6. First-Slice Proposal

The smallest autonomous PR that delivers value and establishes the architecture:

1. **Project scaffold** — Vite + React + TypeScript + Tailwind + ESLint/Prettier + Vitest.
2. **Auth** — Login page, Axios interceptors, JWT storage, auth store, protected route wrapper.
3. **Shell layout** — Responsive sidebar/topbar, tenant selector, role-based navigation skeleton.
4. **Dashboard skeleton** — Fetches `/reports/dashboard`, renders KPI cards (occupied tables, active orders, sales today, low stock).

This slice gives every user a working login and a dashboard; subsequent slices add one domain at a time.

## 7. Risks and Open Decisions

| Risk / Decision | Impact | Notes |
|-----------------|--------|-------|
| WebSocket payload typing | Medium | Backend currently sends raw DTOs or strings; frontend should treat payloads defensively and always refresh from REST. |
| Multi-tenant role switching | Medium | Need UI for switching restaurants/roles; `restaurantRoles` already returned by login. |
| Shared-device JWT storage | Medium | `localStorage` vs `sessionStorage` vs memory+refresh affects security and UX; decide before auth slice. |
| Offline/PWA scope | Medium | PWA is future; defer service worker but keep app shell responsive. |
| Image handling | Low | Products have `imageUrl`; need asset hosting strategy (CDN vs local uploads). |
| Currency/locale | Low | Backend uses `MXN` default; frontend formatter should match locale. |
| CORS origin in production | Low | `app.cors.allowed-origins` must include production frontend URL. |
| Test strategy for frontend | Low | Backend has strict TDD; frontend should follow Vitest + RTL with MSW. |

## 8. Chained PR Strategy

Target under 400 changed lines per PR (excluding generated/boilerplate). Proposed slice sequence:

| # | PR | Focus | Estimated Lines (non-boilerplate) |
|---|-----|-------|-----------------------------------|
| 1 | `frontend/scaffold` | Vite + TS + Tailwind + Vitest + folder structure | 150–250 |
| 2 | `frontend/auth` | Login, Axios interceptors, auth store, protected routes | 250–350 |
| 3 | `frontend/shell` | Layout, sidebar, topbar, tenant selector, responsive nav | 250–350 |
| 4 | `frontend/dashboard` | Dashboard page, report API, KPI cards | 200–300 |
| 5 | `frontend/tables` | Table grid, status transitions, WebSocket table topic | 250–350 |
| 6 | `frontend/menu` | Section/category/product CRUD list views | 300–400 |
| 7 | `frontend/orders` | Order creation, order list, detail status, WebSocket order topic | 300–400 |
| 8 | `frontend/billing` | Invoice list, payment flow | 250–350 |
| 9 | `frontend/cash` | Open/close register, X/Z reports | 200–300 |
| 10 | `frontend/reports` | Sales, product, financial, footfall, staff planning charts | 300–400 |
| 11 | `frontend/suppliers` | Supplier CRUD | 200–300 |
| 12 | `frontend/pwa` | Vite PWA plugin, manifest, offline basics | 150–250 |

Each PR builds on the previous. Boilerplate (`package-lock.json`, generated icons, initial `vite.config.ts`) should be committed in PR #1 so reviewers can focus on logic in later PRs. If a domain slice exceeds 400 lines, split into read/list and write/mutate sub-PRs.
