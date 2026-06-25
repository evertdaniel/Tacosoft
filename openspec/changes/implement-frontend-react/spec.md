# Spec: Implement React Frontend for Tacosoft

## Scope

This change adds a new `frontend/` React application for Tacosoft. The first slice (`frontend/scaffold-auth-shell-dashboard`) establishes the project, auth, shell, and dashboard. Later slices add domain CRUD, WebSocket, and PWA scaffolding.

---

## First Slice — Detailed Specs

### 1. Project Scaffold

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-SCA-001 | The `frontend/` directory MUST contain a Vite + React + TypeScript project with Tailwind CSS, ESLint, Prettier, Vitest, React Testing Library, and MSW. | **GIVEN** no frontend exists; **WHEN** the scaffold is generated; **THEN** `vite.config.ts`, `tailwind.config.js`, `package.json`, and `tsconfig.json` exist. |
| FE-SCA-002 | `package.json` scripts MUST include `dev`, `build`, `test`, `test:coverage`, `lint`, and `format`. | **GIVEN** the scaffold; **WHEN** running each script; **THEN** the corresponding command executes successfully. |
| FE-SCA-003 | `frontend/.gitignore` MUST ignore `node_modules/`, `dist/`, `.env.local`, and coverage output. | **GIVEN** a fresh clone; **WHEN** `npm install` and `npm run build` run; **THEN** ignored directories are not tracked. |

### 2. Axios API Client

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-API-001 | The Axios instance MUST read the base URL from `VITE_API_BASE_URL` with default `http://localhost:8080`. | **GIVEN** `.env.local` sets `VITE_API_BASE_URL`; **WHEN** a request is made; **THEN** the request targets that base URL. |
| FE-API-002 | A request interceptor MUST attach `Authorization: Bearer <token>` and `x-restaurant-id: <restaurantId>` when both values are present. | **GIVEN** a logged-in user with a selected restaurant; **WHEN** calling an authenticated endpoint; **THEN** both headers are sent. |
| FE-API-003 | A response interceptor on 401/403 MUST clear auth storage and redirect to `/login`. | **GIVEN** an authenticated session; **WHEN** the server responds with 401; **THEN** `localStorage` auth keys are removed and the browser navigates to `/login`. |

### 3. Auth Domain

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-AUTH-001 | The login form MUST POST `/auth/login` with `{ username, password }`. | **GIVEN** valid credentials; **WHEN** submitting the login form; **THEN** the endpoint is called and a success path is taken. |
| FE-AUTH-002 | On successful login, the system MUST persist `token`, `user`, `currentRestaurant`, and `restaurantRoles` to `localStorage`; MUST persist `token`, `user`, and `currentRestaurant` to the Zustand auth store; and MUST initialize the tenant store with `restaurantRoles`. | **GIVEN** a valid login response; **WHEN** login succeeds; **THEN** all four values are in `localStorage`, the auth store holds `token`, `user`, and `currentRestaurant`, and the tenant store holds `restaurantRoles`. |
| FE-AUTH-003 | On login failure, the system MUST display an error message. | **GIVEN** invalid credentials; **WHEN** submitting the form; **THEN** an error message is shown and no token is stored. |
| FE-AUTH-004 | The `/login` route MUST render `LoginPage`. | **GIVEN** an unauthenticated user navigates to `/login`; **WHEN** the route matches; **THEN** the login page is rendered. |
| FE-AUTH-005 | A protected route wrapper MUST redirect unauthenticated users to `/login`. | **GIVEN** no token in storage; **WHEN** accessing `/dashboard`; **THEN** the user is redirected to `/login`. |
| FE-AUTH-006 | Logout MUST clear storage and redirect to `/login`. | **GIVEN** an authenticated user; **WHEN** logout is triggered; **THEN** auth data is removed and the user lands on `/login`. |
| FE-AUTH-007 | The system MUST logout the user when the JWT `exp` claim passes. | **GIVEN** a token near expiry; **WHEN** the expiry time passes; **THEN** the user is redirected to `/login` without a backend call. |

### 4. Tenant/Role Switching

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-TEN-001 | The Zustand tenant store MUST hold `currentRestaurantId` and `availableRoles`. | **GIVEN** a user with multiple restaurant roles; **WHEN** login completes; **THEN** the tenant store reflects the current restaurant and all available roles. |
| FE-TEN-002 | The shell header MUST display the current restaurant name and role. | **GIVEN** a selected restaurant and role; **WHEN** the shell renders; **THEN** the header shows the restaurant name and role label. |
| FE-TEN-003 | A restaurant selector MUST be visible only when `restaurantRoles.length > 1` and MUST update the tenant store on selection. | **GIVEN** a user in two restaurants; **WHEN** switching restaurants; **THEN** `currentRestaurantId`, role, and `x-restaurant-id` header update. |

### 5. Shell Layout

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-SHL-001 | The shell MUST show a sidebar on desktop and a top/bottom navigation bar on mobile. | **GIVEN** a 1440 px viewport; **WHEN** rendering; **THEN** a sidebar is visible. **GIVEN** a 375 px viewport; **THEN** mobile navigation is visible. |
| FE-SHL-002 | Navigation items MUST be visible based on the user's current role (`ADMIN`, `WAITER`, `COOK`, `CASHIER`). | **GIVEN** a `COOK`; **WHEN** the shell renders; **THEN** kitchen-related items are visible and billing items are hidden. |
| FE-SHL-003 | The main content area MUST render the matched route. | **GIVEN** a logged-in user on `/dashboard`; **WHEN** the shell renders; **THEN** the dashboard page appears in the main area. |

### 6. Dashboard

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-DSH-001 | The dashboard page MUST fetch `/reports/dashboard` via TanStack Query. | **GIVEN** an authenticated user; **WHEN** the dashboard loads; **THEN** a GET request to `/reports/dashboard` is made with auth and tenant headers. |
| FE-DSH-002 | The dashboard MUST render KPI cards for occupied tables, active orders, sales today, and low stock count. | **GIVEN** a dashboard response; **WHEN** data is loaded; **THEN** four KPI cards display the correct values. |
| FE-DSH-003 | The dashboard MUST show a loading skeleton while fetching and an error state on failure. | **GIVEN** a pending request; **THEN** skeletons appear. **GIVEN** a 500 response; **THEN** an error message is shown. |

### 7. Tests — First Slice

| ID | Requirement | Scenarios |
|----|-------------|-----------|
| FE-TST-001 | Each hook, store, component, and page in the first slice MUST have at least one test. | **GIVEN** the first slice code; **WHEN** tests run; **THEN** coverage includes auth hook/store, login page, dashboard page, API client, and shell components. |
| FE-TST-002 | MSW MUST mock `POST /auth/login` and `GET /reports/dashboard`. | **GIVEN** tests for login and dashboard; **WHEN** they run; **THEN** no real backend calls are made. |
| FE-TST-003 | First slice test coverage MUST be at least 70%. | **GIVEN** all tests pass; **WHEN** coverage is computed; **THEN** the threshold is met. |

---

## Subsequent Slices — High-Level Specs

### Tables Slice

- **FE-TBL-001**: The system MUST display a grid of tables with status (`FREE`, `OCCUPIED`, `RESERVED`).
- **FE-TBL-002**: Staff MUST be able to change a table's status.
- **Key files**: `features/tables/pages/TablesPage.tsx`, `features/tables/components/TableCard.tsx`, `api/tables.api.ts`.
- **Dependencies**: scaffold, auth, shell.

| Scenario | Flow |
|----------|------|
| List tables | **GIVEN** tables exist; **WHEN** opening `/tables`; **THEN** the grid renders with correct statuses. |
| Update status | **GIVEN** an occupied table; **WHEN** staff selects "Free"; **THEN** `PUT /tables/{id}/status` is called and the grid refreshes. |

### Menu Slice

- **FE-MNU-001**: The system MUST provide list views for sections, categories, products, and product options.
- **FE-MNU-002**: Admins MUST be able to create and edit menu items.
- **Key files**: `features/menu/pages/*Page.tsx`, `features/menu/components/*Form.tsx`.
- **Dependencies**: scaffold, auth, shell.

| Scenario | Flow |
|----------|------|
| List products | **GIVEN** products exist; **WHEN** opening `/menu/products`; **THEN** the list renders. |
| Create category | **GIVEN** admin role; **WHEN** submitting the category form; **THEN** `POST /categories` is called. |

### Orders Slice

- **FE-ORD-001**: Waiters MUST create orders linked to a table or take-away.
- **FE-ORD-002**: The system MUST show order lists and detail status.
- **FE-ORD-003**: Cooks MUST see a kitchen view filtered by production area.
- **Key files**: `features/orders/pages/OrdersPage.tsx`, `features/orders/components/OrderCard.tsx`, `features/orders/pages/KitchenPage.tsx`.
- **Dependencies**: scaffold, auth, shell, tables, menu, WebSocket.

| Scenario | Flow |
|----------|------|
| Create order | **GIVEN** a table; **WHEN** adding products; **THEN** `POST /orders` creates the order. |
| Kitchen update | **GIVEN** a new order detail; **WHEN** a WebSocket event arrives; **THEN** the kitchen view refreshes. |

### Billing Slice

- **FE-BIL-001**: Cashiers MUST see a list of unpaid invoices.
- **FE-BIL-002**: Cashiers MUST record a payment for an invoice.
- **Key files**: `features/billing/pages/InvoicesPage.tsx`, `features/billing/components/PaymentModal.tsx`.
- **Dependencies**: scaffold, auth, shell, orders.

| Scenario | Flow |
|----------|------|
| List unpaid | **GIVEN** unpaid invoices; **THEN** `/invoices/unpaid` renders them. |
| Pay invoice | **GIVEN** an unpaid invoice; **WHEN** submitting payment; **THEN** `POST /invoices/{id}/pay` succeeds. |

### Cash Slice

- **FE-CAS-001**: Cashiers MUST open and close cash registers.
- **FE-CAS-002**: The system MUST display X and Z report summaries.
- **Key files**: `features/cash/pages/CashRegisterPage.tsx`, `features/cash/components/XReport.tsx`, `features/cash/components/ZReport.tsx`.
- **Dependencies**: scaffold, auth, shell.

| Scenario | Flow |
|----------|------|
| Open register | **GIVEN** no active register; **WHEN** opening with initial amount; **THEN** `POST /cash-registers/open` succeeds. |
| View X report | **GIVEN** an active register; **WHEN** requesting X report; **THEN** `GET /cash-registers/x-report` renders. |

### Reports Slice

- **FE-RPT-001**: Admins MUST view sales, product, financial, footfall, and staff planning reports.
- **FE-RPT-002**: Report pages MUST accept date range filters.
- **Key files**: `features/reports/pages/*ReportPage.tsx`, `features/reports/components/DateRangeFilter.tsx`.
- **Dependencies**: scaffold, auth, shell.

| Scenario | Flow |
|----------|------|
| Sales report | **GIVEN** a date range; **WHEN** submitting; **THEN** `GET /reports/sales` renders charts. |
| Empty range | **GIVEN** no data; **THEN** the report shows an empty state, not an error. |

### Suppliers Slice

- **FE-SUP-001**: Admins MUST list, search, create, edit, and deactivate suppliers.
- **Key files**: `features/suppliers/pages/SuppliersPage.tsx`, `features/suppliers/components/SupplierForm.tsx`.
- **Dependencies**: scaffold, auth, shell.

| Scenario | Flow |
|----------|------|
| Search suppliers | **GIVEN** a query; **WHEN** typing; **THEN** `GET /suppliers/search` returns matches. |
| Create supplier | **GIVEN** valid data; **WHEN** saving; **THEN** `POST /suppliers` succeeds. |

### WebSocket Slice

- **FE-WS-001**: The system MUST maintain a STOMP/SockJS connection to `/ws`.
- **FE-WS-002**: Incoming messages for orders and tables MUST invalidate the corresponding TanStack Query caches.
- **Key files**: `hooks/useWebSocket.ts`, `stores/websocket.store.ts`.
- **Dependencies**: scaffold, auth, shell, tenant store.

| Scenario | Flow |
|----------|------|
| Connect | **GIVEN** auth token; **WHEN** app loads; **THEN** the client connects and subscribes to `/topic/restaurant/{id}/orders`. |
| Cache refresh | **GIVEN** an active orders query; **WHEN** an order event arrives; **THEN** the orders cache refreshes. |

### PWA Slice

- **FE-PWA-001**: The build MUST generate a web app manifest and service worker via `vite-plugin-pwa`.
- **FE-PWA-002**: The app shell MUST remain usable when offline after the first load.
- **Key files**: `vite.config.ts` (PWA plugin), `public/manifest.json`, `public/icons/*`.
- **Dependencies**: all prior slices.

| Scenario | Flow |
|----------|------|
| Manifest | **GIVEN** a production build; **THEN** `manifest.webmanifest` is generated. |
| Offline shell | **GIVEN** a cached shell; **WHEN** the network is offline; **THEN** the app shell still renders. |

---

## REMOVED Requirements

None. This change is a pure frontend addition; no existing backend behavior is removed.

---

## RENAMED Requirements

None.
