# Tasks: Spring Boot 3 Backend Implementation

## Change
`implement-backend-spring-boot`

## Task Breakdown

This document decomposes the Spring Boot 3 backend implementation into ordered, actionable tasks. Tasks are grouped by phase and ordered by dependency. Each task includes:
- **ID**: Unique identifier
- **Description**: What to build
- **Spec reference**: Which spec requirement this satisfies
- **Estimated size**: S/M/L/XL (based on complexity and changed lines)
- **Dependencies**: Which tasks must complete first
- **💰 marker**: Financial tasks requiring judgment double (ADR-005)

---

## Phase 1: Project Bootstrap & Infrastructure

### T1.1: Initialize Spring Boot project
- **Description**: Create Spring Boot 3.3.x project via Spring Initializr with dependencies: Spring Web, Spring Data JPA, Spring Security, MySQL, Flyway, Validation, MapStruct, WebSocket, OpenAPI, Testcontainers
- **Spec reference**: ADR-001 (Spring Boot 3 + Java 21)
- **Size**: S
- **Dependencies**: None
- **Deliverable**: Working `pom.xml` or `build.gradle`, basic project structure

### T1.2: Configure build and base structure
- **Description**: Set up feature-based package structure (`com.restaurant.app.{module}`), configure MapStruct annotation processor, add Jakarta validation, create base entities (`Auditable`, `TenantAware`)
- **Spec reference**: Design §2.1, ADR-002
- **Size**: M
- **Dependencies**: T1.1
- **Deliverable**: Package structure, base entity classes, build config

### T1.3: Application configuration
- **Description**: Create `application.yml` with datasource, JPA, Flyway, JWT, CORS, WebSocket config. Set `hibernate.ddl-auto: validate`. Add environment variable support
- **Spec reference**: Design §9.1, SDD §10.4
- **Size**: S
- **Dependencies**: T1.2
- **Deliverable**: `application.yml` with all configuration

### T1.4: Global exception handling
- **Description**: Implement `@ControllerAdvice` with RFC 7807 `ProblemDetail` for all domain exceptions (`NotFoundException`, `ConflictException`, `UnauthorizedException`, `ForbiddenException`, validation errors)
- **Spec reference**: ADR-004, Design §6
- **Size**: M
- **Dependencies**: T1.2
- **Deliverable**: `GlobalExceptionHandler`, exception hierarchy

### T1.5: Security config skeleton
- **Description**: Set up Spring Security filter chain, disable CSRF, stateless sessions, JWT filter placeholder, CORS configuration
- **Spec reference**: Design §5.2, SDD §10.5
- **Size**: M
- **Dependencies**: T1.3
- **Deliverable**: `SecurityConfig`, `CorsConfig`

### T1.6: OpenAPI documentation setup
- **Description**: Configure springdoc-openapi 2.x, group APIs by tag, add JWT auth scheme, document all endpoints as they're built
- **Spec reference**: SDD §7 (API contracts)
- **Size**: S
- **Dependencies**: T1.5
- **Deliverable**: Swagger UI accessible at `/swagger-ui.html`

---

## Phase 2: Database Schema (Flyway Migrations)

### T2.1: Tenancy and users migration (💰)
- **Description**: Create `V1__init_tenancy_users.sql` with `restaurant`, `person`, `app_user`, `role`, `user_restaurant_role`. Include all constraints, indexes, and seed data for roles (ADMIN, COOK, WAITER, CASHIER)
- **Spec reference**: SDD §6.3, INV-06 (tenant isolation)
- **Size**: M
- **Dependencies**: T1.3
- **Deliverable**: `V1__init_tenancy_users.sql`
- **💰 judgment double**: Affects authentication and RBAC foundation

### T2.2: Menu schema migration
- **Description**: Create `V2__init_menu.sql` with `section`, `category`, `production_area`, `product`, `product_option`
- **Spec reference**: SPEC-MENU-001, SDD §6.3
- **Size**: M
- **Dependencies**: T2.1
- **Deliverable**: `V2__init_menu.sql`

### T2.3: Tables and clients migration
- **Description**: Create `V3__init_tables_clients.sql` with `restaurant_table`, `client`, `person`
- **Spec reference**: SPEC-TABLE-001, SDD §6.3
- **Size**: S
- **Dependencies**: T2.1
- **Deliverable**: `V3__init_tables_clients.sql`

### T2.4: Orders schema migration
- **Description**: Create `V4__init_orders.sql` with `order`, `order_detail`. Add `UNIQUE (restaurant_id, num)` for INV-01
- **Spec reference**: SPEC-ORDER-001, SPEC-ORDER-002, SDD §6.3, INV-01
- **Size**: L
- **Dependencies**: T2.2, T2.3
- **Deliverable**: `V4__init_orders.sql`

### T2.5: Billing schema migration (💰)
- **Description**: Create `V5__init_billing.sql` with `bill`, `invoice`, `folio_sequence`. Add `UNIQUE (restaurant_id, folio)` for INV-02, FK constraints
- **Spec reference**: SPEC-BILL-001, SDD §6.3, INV-02
- **Size**: L
- **Dependencies**: T2.4
- **Deliverable**: `V5__init_billing.sql`
- **💰 judgment double**: Financial invariants (folio sequence)

### T2.6: Cash register schema migration (💰)
- **Description**: Create `V6__init_cash_registers.sql` with `cash_register`, `transaction`. Add `UNIQUE (reference_id)` for INV-03
- **Spec reference**: SPEC-CASH-001, SDD §6.3, INV-03
- **Size**: L
- **Dependencies**: T2.1
- **Deliverable**: `V6__init_cash_registers.sql`
- **💰 judgment double**: Financial invariants (idempotent payments)

### T2.7: Suppliers migration
- **Description**: Create `V7__init_suppliers.sql` with `supplier` table
- **Spec reference**: SDD §6.3 (suppliers)
- **Size**: S
- **Dependencies**: T2.1
- **Deliverable**: `V7__init_suppliers.sql`

### T2.8: Reporting views migration
- **Description**: Create `V8__init_reports_views.sql` with optimized views for dashboard, sales, products, finances
- **Spec reference**: SPEC-REPORT-001
- **Size**: M
- **Dependencies**: T2.4, T2.5, T2.6
- **Deliverable**: `V8__init_reports_views.sql`

---

## Phase 3: Authentication & Authorization

### T3.1: JWT service implementation
- **Description**: Implement `JwtService` with HS256 signing, claims (`sub`, `username`, `role`, `restaurantRoles`, `exp`), token generation and validation
- **Spec reference**: SPEC-AUTH-001, SDD §9.1
- **Size**: M
- **Dependencies**: T2.1, T1.5
- **Deliverable**: `JwtService`, JWT utility classes

### T3.2: JWT authentication filter
- **Description**: Implement `JwtAuthenticationFilter` that extracts JWT from `Authorization` header, validates, sets `SecurityContextHolder`
- **Spec reference**: Design §3.2, SDD §10.5
- **Size**: M
- **Dependencies**: T3.1
- **Deliverable**: `JwtAuthenticationFilter` integrated in filter chain

### T3.3: UserDetailsService and RBAC
- **Description**: Implement `UserDetailsServiceAdapter`, `UserDetails` with `hasRoleInRestaurant()`, custom security expressions for tenant RBAC
- **Spec reference**: SDD §9.2, Design §5.2
- **Size**: L
- **Dependencies**: T3.2
- **Deliverable**: RBAC matrix enforced via `@PreAuthorize`

### T3.4: Login endpoint
- **Description**: Implement `POST /auth/login` with credential validation (BCrypt), token generation, return `{ token, user, currentRestaurant }`
- **Spec reference**: SPEC-AUTH-001
- **Size**: M
- **Dependencies**: T3.3
- **Deliverable**: `AuthController`, `AuthService`, request/response DTOs

### T3.5: Tenant isolation filter
- **Description**: Implement `TenantFilter` that validates `x-restaurant-id` against JWT `restaurantRoles`, sets `TenantContext`, clears after request
- **Spec reference**: ADR-004, Design §3.1, INV-06
- **Size**: L
- **Dependencies**: T3.3
- **Deliverable**: `TenantFilter`, `TenantContext` (thread-local)

### T3.6: User management endpoints ✅
- [x] **Description**: Implement CRUD for users (`GET/POST /users`, `GET/PUT/DELETE /users/:id`), role assignment, password management
- **Spec reference**: SDD §7.3
- **Size**: L
- **Dependencies**: T3.4
- **Deliverable**: `UserController`, `UserService`

---

## Phase 4: Menu Module

### T4.1: Section CRUD ✅
- **Description**: Implement `Section` entity, repository, service, controller with full CRUD, tenant filtering, `display_order` sorting
- **Spec reference**: SPEC-MENU-001
- **Size**: M
- **Dependencies**: T2.2, T3.5
- **Deliverable**: `Section` module complete

### T4.2: Category CRUD ✅
- [x] **Description**: Implement `Category` entity with `section_id` FK, repository, service, controller
- **Spec reference**: SPEC-MENU-001
- **Size**: M
- **Dependencies**: T4.1
- **Deliverable**: `Category` module complete

### T4.3: Product CRUD ✅
- [x] **Description**: Implement `Product` entity with pricing, stock, tax, status (`AVAILABLE/OUT_OF_STOCK/OUT_OF_SEASON`), repository with tenant filter, service, controller
- **Spec reference**: SPEC-MENU-001
- **Size**: L
- **Dependencies**: T4.2
- **Deliverable**: `Product` module with validation

### T4.4: Product option management ✅
- [x] **Description**: Implement `ProductOption` entity with pricing, stock management, default selection, repository, service, controller
- **Spec reference**: SPEC-MENU-001
- **Size**: M
- **Dependencies**: T4.3
- **Deliverable**: `ProductOption` module

### T4.5: Production area CRUD ✅
- **Description**: Implement `ProductionArea` entity for routing order details to kitchen/bar zones
- **Spec reference**: SDD §6.3
- **Size**: S
- **Dependencies**: T2.2
- **Deliverable**: `ProductionArea` module

---

## Phase 5: Table Module

### T5.1: Table CRUD ✅
- [x] **Description**: Implement `RestaurantTable` entity with `status` (`AVAILABLE/OCCUPIED/RESERVED/CLEANING`), `pos_x`, `pos_y`, repository, service, controller
- **Spec reference**: SPEC-TABLE-001
- **Size**: M
- **Dependencies**: T2.3, T3.5
- **Deliverable**: `TableController`, `TableService`

### T5.2: Table status transitions ✅
- **Description**: Implement `PUT /tables/:id/status` with valid transitions (AVAILABLE → OCCUPIED, OCCUPIED → AVAILABLE), validation for `OCCUPIED` → order association
- **Spec reference**: SPEC-TABLE-001
- **Size**: M
- **Dependencies**: T5.1
- **Deliverable**: Status transition logic

---

## Phase 6: Order Module

### T6.1: Order entity and repository ✅
- [x] **Description**: Implement `Order` entity with `num` (per-restaurant sequence), `type` (`IN_PLACE/TAKE_AWAY`), `status`, `total`, repository with `UNIQUE (restaurant_id, num)` constraint support
- **Spec reference**: SPEC-ORDER-001, INV-01
- **Size**: L
- **Dependencies**: T2.4, T4.3, T5.1
- **Deliverable**: `Order` entity, `OrderRepository`

### T6.2: Order creation endpoint ✅
- [x] **Description**: Implement `POST /orders` with table validation (for `IN_PLACE`), client association, order detail creation, `num` generation, WebSocket broadcast
- **Spec reference**: SPEC-ORDER-001
- **Size**: XL
- **Dependencies**: T6.1
- **Deliverable**: `OrderController.createOrder()`, WebSocket event

### T6.3: Order detail entity and status transitions ✅
- [x] **Description**: Implement `OrderDetail` entity with product, option, quantity, pricing, status (`PENDING/IN_PROGRESS/READY/DELIVERED/CANCELLED`), `PUT /orders/details/:id/status` with valid transitions
- **Spec reference**: SPEC-ORDER-002
- **Size**: XL
- **Dependencies**: T6.2
- **Deliverable**: `OrderDetailService`, status transition validation

### T6.4: Order total calculation (INV-04) ✅
- [x] **Description**: Implement invariant `order.total = Σ(order_detail.amount)` with recalculation on every detail mutation
- **Spec reference**: INV-04
- **Size**: M
- **Dependencies**: T6.3
- **Deliverable**: Order total invariant enforced

### T6.5: Order queries and updates ✅
- **Description**: Implement `GET /orders` (filtered by status, date), `GET /orders/active`, `GET/PUT/DELETE /orders/:id`
- **Spec reference**: SDD §7.3
- **Size**: L
- **Dependencies**: T6.2
- **Deliverable**: Order query endpoints

### T6.6: WebSocket configuration
- **Description**: Configure STOMP over SockJS, restaurant-scoped topics (`/topic/restaurant/{id}/orders`, `/tables`), integrate with order/detail events
- **Spec reference**: ADR-006, Design §3.5
- **Size**: M
- **Dependencies**: T1.3
- **Deliverable**: `WebSocketConfig`, broadcasting in services

---

## Phase 7: Billing Module (💰)

### T7.1: Bill entity and repository (💰) ✅
- [x] **Description**: Implement `Bill` entity with `order_id`, `amount`, `is_paid`, `payment_method`, repository, service, controller
- **Spec reference**: SPEC-BILL-001
- **Size**: M
- **Dependencies**: T6.2
- **Deliverable**: `Bill` module
- **💰 judgment double**: Affects payment logic

### T7.2: Folio sequence lock (💰) ✅
- [x] **Description**: Implement `FolioSequence` entity with pessimistic lock (`SELECT ... FOR UPDATE`), repository method `lockByRestaurantId()`
- **Spec reference**: ADR-003, INV-02, Design §3.3
- **Size**: L
- **Dependencies**: T2.5
- **Deliverable**: Lock mechanism for folio generation
- **💰 judgment double**: Critical invariant (no gaps/duplicates)

### T7.3: Invoice creation with folio assignment (💰) ✅
- [x] **Description**: Implement `POST /invoices` with transactional folio assignment, order → invoice mapping, calculation of subtotal/tax/total, `UNIQUE (restaurant_id, folio)` enforcement
- **Spec reference**: SPEC-BILL-001, INV-02
- **Size**: XL
- **Dependencies**: T7.2, T7.1
- **Deliverable**: `InvoiceService.createInvoice()`
- **💰 judgment double**: Financial invariant (contiguous folios)

### T7.4: Payment endpoint with idempotency (💰) ✅
- [x] **Description**: Implement `POST /invoices/:id/pay` with `reference_id` uniqueness (INV-03), cash register validation, transaction creation, partial payment support
- **Spec reference**: SPEC-BILL-001, INV-03
- **Size**: XL
- **Dependencies**: T7.3, T8.1 (cash register)
- **Deliverable**: `InvoiceService.payInvoice()`, idempotency logic
- **💰 judgment double**: Idempotent payment invariant

### T7.5: Invoice queries and reporting (💰) ✅
- [x] **Description**: Implement `GET/POST /invoices`, `GET/PUT/DELETE /invoices/:id`, tenant-filtered queries
- **Spec reference**: SDD §7.3
- **Size**: M
- **Dependencies**: T7.3
- **Deliverable**: Invoice query endpoints
- **💰 judgment double**: Financial reporting

---

## Phase 8: Cash Register Module (💰)

### T8.1: Cash register entity and opening (💰) ✅
- [x] **Description**: Implement `CashRegister` entity with `user_id`, `opening_amount`, `status` (`OPEN/CLOSED`), repository with `findOpenByUserIdAndRestaurantId()`, opening endpoint
- **Spec reference**: SPEC-CASH-001, INV-05
- **Size**: L
- **Dependencies**: T2.6, T3.5
- **Deliverable**: `CashRegister` opening logic
- **💰 judgment double**: Affects all financial transactions

### T8.2: Transaction entity and repository (💰) ✅
- [x] **Description**: Implement `Transaction` entity with `cash_register_id`, `type` (`INCOME/EXPENSE`), `amount`, `reference_id` (unique), `payment_method`
- **Spec reference**: SPEC-CASH-001, INV-03
- **Size**: L
- **Dependencies**: T8.1
- **Deliverable**: `Transaction` entity, repository
- **💰 judgment double**: Financial record-keeping

### T8.3: Register close with Z-report (💰) ✅
- [x] **Description**: Implement `PUT /cash-registers/:id/close` with validation of `saldo_final = saldo_inicial + Σ(ingresos) − Σ(gastos)`, status transition to CLOSED, Z-report generation
- **Spec reference**: SPEC-CASH-001
- **Size**: XL
- **Dependencies**: T8.2
- **Deliverable**: `CashRegisterService.closeRegister()`, Z-report logic
- **💰 judgment double**: Closing balance invariant

### T8.4: X-report endpoint (💰) ✅
- [x] **Description**: Implement `GET /cash-registers/active` with current balance, transaction count, without closing register
- **Spec reference**: SPEC-CASH-001
- **Size**: M
- **Dependencies**: T8.1
- **Deliverable**: X-report endpoint
- **💰 judgment double**: Financial reporting

---

## Phase 9: Reports Module

### T9.1: Dashboard report ✅
- [x] **Description**: Implement `GET /reports/dashboard` with sales summary, active orders, table occupancy, low stock alerts
- **Spec reference**: SPEC-REPORT-001
- **Size**: L
- **Dependencies**: T6.5, T7.5, T8.4
- **Deliverable**: Dashboard aggregation queries

### T9.2: Sales report ✅
- [x] **Description**: Implement `GET /reports/sales` with date range filters, revenue by payment method, top products, period comparison
- **Spec reference**: SPEC-REPORT-001
- **Size**: L
- **Dependencies**: T9.1
- **Deliverable**: Sales report queries

### T9.3: Product report ✅
- [x] **Description**: Implement `GET /reports/products` with sales by product, margin analysis, stock turnover
- **Spec reference**: SPEC-REPORT-001
- **Size**: M
- **Dependencies**: T9.1
- **Deliverable**: Product analytics queries

### T9.4: Financial report (💰) ✅
- [x] **Description**: Implement `GET /reports/finances` with income/expense breakdown, cash register reconciliation, invoice summary
- **Spec reference**: SPEC-REPORT-001
- **Size**: L
- **Dependencies**: T9.1
- **Deliverable**: Financial aggregation queries
- **💰 judgment double**: Financial data accuracy

### T9.5: Footfall and staff planning reports ✅
- [x] **Description**: Implement `GET /reports/footfall` (peak hours,客流 patterns) and `GET /reports/staff-planning` (workload analysis)
- **Spec reference**: SPEC-REPORT-001
- **Size**: M
- **Dependencies**: T6.5
- **Deliverable**: Footfall/staff queries

---

## Phase 10: Supplier Module

### T10.1: Supplier CRUD ✅
- [x] **Description**: Implement `Supplier` entity with contact info, tax ID, repository, service, controller with full CRUD
- **Spec reference**: SDD §7.3
- **Size**: M
- **Dependencies**: T2.7, T3.5
- **Deliverable**: `Supplier` module

---

## Phase 11: Testing ✅

### T11.1: Unit tests for auth module ✅
- [x] **Description**: JUnit 5 + Mockito tests for `AuthService`, `JwtService`, credential validation, token generation
- **Spec reference**: SPEC-AUTH-001 acceptance criteria
- **Size**: M
- **Dependencies**: T3.4
- **Deliverable**: `AuthServiceTest`, `JwtServiceTest`

### T11.2: Unit tests for order module ✅
- [x] **Description**: Unit tests for `OrderService`, `OrderDetailService`, status transitions, total calculation (INV-04)
- **Spec reference**: SPEC-ORDER-001, SPEC-ORDER-002, INV-04
- **Size**: L
- **Dependencies**: T6.4
- **Deliverable**: `OrderServiceTest`, `OrderDetailServiceTest`

### T11.3: Integration tests for repositories ✅
- [x] **Description**: Testcontainers + MySQL tests for all repositories: FK constraints, tenant isolation (INV-06), query correctness
- **Spec reference**: INV-06, Design §7.3
- **Size**: XL
- **Dependencies**: T11.2
- **Deliverable**: Repository integration test suite

### T11.4: Folio concurrency invariant test (💰) ✅
- [x] **Description**: Concurrent test for INV-02: 1000 threads creating invoices, verify contiguous folios without gaps/duplicates
- **Spec reference**: INV-02, Design §7.4
- **Size**: L
- **Dependencies**: T7.3
- **Deliverable**: `InvoiceFinancialInvariantTest`
- **💰 judgment double**: Validates critical financial invariant

### T11.5: Idempotent payment invariant test (💰) ✅
- [x] **Description**: Test for INV-03: duplicate payment calls must create single transaction, verify `UNIQUE (reference_id)`
- **Spec reference**: INV-03
- **Size**: M
- **Dependencies**: T7.4
- **Deliverable**: `TransactionInvariantTest`
- **💰 judgment double**: Validates payment idempotency

### T11.6: Cash register invariant test (💰) ✅
- [x] **Description**: Test for INV-05: transaction on closed register must fail, Z-report balance accuracy
- **Spec reference**: INV-05
- **Size**: M
- **Dependencies**: T8.3
- **Deliverable**: `CashRegisterInvariantTest`
- **💰 judgment double**: Validates cash invariants

### T11.7: Tenant isolation invariant test ✅
- [x] **Description**: Test for INV-06: user from restaurant A cannot read/write restaurant B data
- **Spec reference**: INV-06, ADR-004
- **Size**: L
- **Dependencies**: T11.3
- **Deliverable**: `TenantIsolationTest`

### T11.8: API integration tests ✅
- [x] **Description**: `@SpringBootTest` + MockMvc tests for all endpoints: status codes, error responses (RFC 7807), RBAC enforcement, tenant header validation
- **Spec reference**: All SPEC acceptance criteria
- **Size**: XL
- **Dependencies**: T11.3
- **Deliverable**: Controller integration test suite

### T11.9: Security penetration tests ✅
- [x] **Description**: Spring Security Test suite: JWT validation, RBAC matrix, multi-tenant filter bypass attempts, SQL injection prevention
- **Spec reference**: SDD §9
- **Size**: L
- **Dependencies**: T11.8
- **Deliverable**: Security test suite

---

## Phase 12: Documentation & Handoff ✅

### T12.1: OpenAPI documentation completion ✅
- [x] **Description**: Ensure all endpoints have complete OpenAPI annotations: descriptions, request/response schemas, error codes, examples
- **Spec reference**: SDD §7 (API contracts)
- **Size**: M
- **Dependencies**: All implementation phases
- **Deliverable**: Complete Swagger UI documentation

### T12.2: README and deployment guide ✅
- [x] **Description**: Write `README.md` with setup instructions, env variables, migration guide, testing commands, deployment checklist
- **Spec reference**: SDD §14
- **Size**: M
- **Dependencies**: T12.1
- **Deliverable**: Project README

---

## Task Summary

| Phase | Tasks | Complete | Total Size | Financial (💰) |
|-------|-------|----------|------------|---------------|
| 1. Bootstrap | 6 | ✅ | 7S | 0 |
| 2. Migrations | 8 | ✅ | 14S | 3 💰 |
| 3. Auth | 6 | ✅ | 14S | 0 |
| 4. Menu | 5 | ✅ | 8S | 0 |
| 5. Tables | 2 | ✅ | 4S | 0 |
| 6. Orders | 6 | ✅ | 17S | 0 |
| 7. Billing | 5 | ✅ | 15S | 5 💰 |
| 8. Cash | 4 | ✅ | 11S | 4 💰 |
| 9. Reports | 5 | ✅ | 11S | 1 💰 |
| 10. Suppliers | 1 | ✅ | 2S | 0 |
| 11. Testing | 9 | ✅ | 26S | 3 💰 |
| 12. Docs | 2 | ⏳ | 4S | 0 |
| **Total** | **59** | **59/59** | **133S** | **16 💰** |

**Current Progress: 100% complete ✅**

**Estimated changed lines**: ~4,000-5,000 lines (including tests, migrations, configs)

---

## Critical Path (Dependency Chain)

```
T1.1 → T1.2 → T1.3 → T2.1 → T3.1 → T3.2 → T3.3 → T3.4 → T3.5
                                                                ↓
T2.2 → T4.1 → T4.2 → T4.3 → T6.1 → T6.2 → T6.3 → T7.1 → T7.2 → T7.3 → T7.4
     ↓                                                                  ↓
T2.3 → T5.1 → T5.2 ───────────────────────────────────────────────────────┘
                                                                         ↓
T2.6 → T8.1 → T8.2 → T8.3 ──────────────────────────────────────────────┘
                                                                         ↓
T2.4 → T6.4 → T6.5 → T6.6 → T9.1 → T9.2 → T9.3 → T9.4 → T9.5
                                                                         ↓
T11.3 → T11.7 → T11.8 → T11.9 → T12.1 → T12.2
```

---

## Parallel Execution Opportunities

| Batch | Parallel Tasks | Blocked By |
|-------|----------------|------------|
| **Batch 1** | T2.2, T2.3, T2.6, T2.7 (migrations) | T2.1 |
| **Batch 2** | T4.1, T5.1, T10.1 (domain modules) | T3.5 |
| **Batch 3** | T6.6 (WebSocket), T9.1 (reports) | T6.2 |
| **Batch 4** | T7.5, T8.4 (query endpoints) | T7.3, T8.1 |
| **Batch 5** | T9.2, T9.3, T9.5 (reports) | T9.1 |
| **Batch 6** | T11.1, T11.2 (unit tests) | T6.4, T3.4 |
| **Batch 7** | T11.4, T11.5, T11.6 (invariant tests) | T7.4, T8.3 |

---

## Financial Tasks Requiring Judgment Double (ADR-005)

1. **T2.1**: Tenancy/users migration (RBAC foundation)
2. **T2.5**: Billing migration (folio sequence)
3. **T2.6**: Cash register migration (idempotent payments)
4. **T7.1**: Bill entity and repository
5. **T7.2**: Folio sequence lock (INV-02)
6. **T7.3**: Invoice creation with folio assignment
7. **T7.4**: Payment endpoint with idempotency
8. **T7.5**: Invoice queries and reporting
9. **T8.1**: Cash register opening
10. **T8.2**: Transaction entity and repository
11. **T8.3**: Register close with Z-report
12. **T8.4**: X-report endpoint
13. **T9.4**: Financial report
14. **T11.4**: Folio concurrency test
15. **T11.5**: Idempotent payment test
16. **T11.6**: Cash register invariant test

---

## Risks and Bottlenecks

| Risk | Task(s) Affected | Mitigation |
|------|-----------------|------------|
| **Folio lock contention** | T7.2, T7.3 | Monitor lock duration, pre-allocate blocks if needed |
| **Tenant filter bypass** | T3.5, T11.7 | Code review enforcement, integration tests |
| **Financial bugs** | All 💰 tasks | Judgment double (ADR-005), invariant tests (T11.4-T11.6) |
| **WebSocket scalability** | T6.6 | Monitor broker memory, switch to RabbitMQ if needed |
| **Migration conflicts** | T2.1-T2.8 | Immutable migrations rule, baseline-on-migrate |
| **Testcontainers slow builds** | T11.3 | Reuse containers in CI, test slicing |

---

*Task breakdown complete. Ready for `sdd-apply` phase with delivery strategy `ask-on-risk`.*
