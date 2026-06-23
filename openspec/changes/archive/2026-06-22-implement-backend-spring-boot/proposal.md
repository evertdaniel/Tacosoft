# Proposal: Implement Spring Boot 3 Backend for Restaurant Management System

## Intent

Build a complete Spring Boot 3 backend that implements the full SDD specification document (`docs/SDD-sistema-restaurante.md`). The frontend already exists (React 18 + MUI v5); this backend provides the formal API layer with multi-tenancy, RBAC, financial invariants, and real-time WebSocket events that the current placeholder backend lacks.

## Scope

### In Scope

All functional modules from the SDD spec:
- **Auth:** JWT authentication, user registration, role management (SPEC-AUTH-001)
- **Orders:** Order creation, detail status updates, kitchen workflow (SPEC-ORDER-001, SPEC-ORDER-002)
- **Billing:** Bills, invoices with global folio sequence, payments (SPEC-BILL-001)
- **Cash:** Cash register operations, X/Z reports, transactions (SPEC-CASH-001)
- **Menu:** Sections, categories, products, options, Excel import (SPEC-MENU-001)
- **Tables:** CRUD, status changes, drag-and-drop positions (SPEC-TABLE-001)
- **Reports:** Dashboard, sales, products, finances, footfall, staff planning (SPEC-REPORT-001)
- **Suppliers:** Basic CRUD for supplier management
- **Infrastructure:** Multi-tenancy (`restaurant_id`), RBAC, WebSocket events, Flyway migrations

### Out of Scope

- Frontend changes (React app remains unchanged)
- Non-SQL databases or alternative persistence
- Microservices architecture (monolithic Spring Boot app)
- Authentication providers (OAuth, SAML) — only JWT
- Real-time analytics or streaming platforms beyond WebSocket

## Capabilities

### New Capabilities

- `auth`: User authentication and authorization with JWT + RBAC
- `order-management`: Order lifecycle and kitchen workflow
- `billing`: Financial operations (invoices, bills, payments) with folio sequence
- `cash-register`: Cash session management and transaction recording
- `menu-catalog`: Menu hierarchy (sections → categories → products → options)
- `table-management`: Restaurant table operations and layout
- `reporting`: Business intelligence and operational reports
- `supplier-management`: Supplier CRUD operations

### Modified Capabilities

None — this is a greenfield backend implementation.

## Approach

**Layered architecture (Controller → Service → Repository)** with feature-based package structure. Each domain module (auth, order, billing, cash, menu, table, report, supplier) gets its own package with controller, service, repository, DTO, and entity sub-packages.

Key implementation patterns:
- **Multi-tenancy:** `TenantFilter` extracts `x-restaurant-id` header; every repository query filters by `restaurant_id`
- **Financial invariants:** Service layer with `@Transactional`; pessimistic locking for folio sequence (INV-02); idempotent payments via unique `reference_id` (INV-03)
- **WebSocket:** STOMP over SockJS; restaurant-scoped topics (`/topic/restaurant/{id}/orders`)
- **DTO mapping:** MapStruct at service boundaries; JPA entities never exposed
- **Migrations:** Flyway with numbered versions; schema validation only (`hibernate.ddl-auto: validate`)

**Judgment double** (ADR-005) applies to any code touching `billing`, `cash`, `folio_sequence`, or `transaction` tables.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `backend/src/main/java/com/restaurant/app/` | New | Complete Spring Boot application |
| `backend/src/main/resources/db/migration/` | New | Flyway migrations for all tables |
| `backend/src/test/java/` | New | Unit, integration, API, and security tests |
| `docs/SDD-sistema-restaurante.md` | Reference | Source of truth for all specs |
| `frontend/` | None | No changes; existing React app consumes new API |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Financial invariant violations (folio gaps, double payment) | Medium | Judgment double review (ADR-005); dedicated invariant tests with concurrent scenarios |
| Tenant data leakage (missing `restaurant_id` filter) | Medium | Repository specs enforce tenant parameter; security tests verify cross-tenant isolation |
| WebSocket connection handling under load | Low | Use Spring's built-in STOMP broker; test with concurrent connections |
| Flyway migration conflicts during development | Medium | Immutable migrations rule; new changes always add new migration files |
| Performance bottleneck on folio sequence lock | Low | Short transaction window; evaluate batch allocation if needed |

## Rollback Plan

If critical defects are found post-deployment:
1. **Database:** Flyway migrations are designed to be forward-only. Revert requires manual SQL rollback and version correction — document rollback scripts for each migration.
2. **Code:** Deploy previous Spring Boot JAR version; revert via CI/CD.
3. **Configuration:** Restore previous `application.yml` from config backup.

For financial bugs (incorrect totals, payment errors), **halt the module** (disable endpoints via feature flag) before rollback; preserve affected transaction records for audit.

## Dependencies

- **External:** Java 21, MySQL 8, Maven/Gradle build tool
- **Libraries:** Spring Boot 3.3.x, Spring Data JPA, Spring Security, Flyway, MySQL Connector, JJWT, MapStruct, Testcontainers
- **Docs:** `docs/SDD-sistema-restaurante.md` must remain source of truth

## Success Criteria

- [ ] All SPEC-* requirements satisfied with corresponding test coverage
- [ ] All invariants (INV-01 through INV-06) have dedicated tests that pass
- [ ] OpenAPI documentation matches all REST endpoints
- [ ] WebSocket events correctly broadcast to same-tenant clients
- [ ] Financial modules (`billing`, `cash`) pass judgment double review
- [ ] Frontend can authenticate and perform core workflows (order → pay → close register)
