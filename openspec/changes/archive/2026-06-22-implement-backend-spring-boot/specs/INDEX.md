# Spec Phase Summary — Implement Spring Boot 3 Backend

## Status: COMPLETE

Date: 2026-06-21

## Specs Created

8 formal specifications extracted from `docs/SDD-sistema-restaurante.md`:

1. **SPEC-AUTH-001** — Login with JWT authentication
2. **SPEC-ORDER-001** — Create order with details
3. **SPEC-ORDER-002** — Update order detail status (kitchen workflow)
4. **SPEC-BILL-001** — Create/pay bill (💰 judgment double)
5. **SPEC-CASH-001** — Open/close cash register (💰 judgment double)
6. **SPEC-MENU-001** — Menu management
7. **SPEC-TABLE-001** — Table management
8. **SPEC-REPORT-001** — Dashboard and business reports

## Spec Structure

Each spec follows the formal format:
- ID, Title, Actor
- Preconditions
- Flow (step-by-step)
- Postconditions
- Invariants (INV-XXX)
- Acceptance criteria (10-13 per spec)
- Edge cases
- Dependencies
- Implementation notes

## Invariants Documented

**28 invariants** across all domains:

- **Multi-tenancy:** INV-06, INV-REPORT-002
- **Financial:** INV-02 (folio), INV-03 (idempotency), INV-04 (totals), INV-05 (closed register)
- **Orders:** INV-01 (order num), INV-ORDER-001 through INV-ORDER-005
- **Cash:** INV-CASH-001 through INV-CASH-003
- **Billing:** INV-BILL-001 through INV-BILL-003
- **Auth:** INV-AUTH-001, INV-AUTH-002
- **Menu:** INV-MENU-001 through INV-MENU-005
- **Tables:** INV-TABLE-001 through INV-TABLE-004
- **Reports:** INV-REPORT-001 through INV-REPORT-004

## Judgment Double (ADR-005)

Modules requiring double adversarial review:
- `billing` (invoices, folio_sequence)
- `cash` (cash_register, transaction)
- Any code touching financial state or money

## Acceptance Criteria

Total: ~100 acceptance criteria across all specs

Each spec includes:
- Happy path validation
- Error cases (400, 403, 404, 409)
- RBAC authorization checks
- Tenant isolation verification
- Business rule validation
- WebSocket event emission (where applicable)

## Next Phase

**Design** — Create architecture document with:
- Layered architecture (Controller → Service → Repository)
- Multi-tenancy implementation (TenantFilter)
- Financial invariants implementation (pessimistic locking, idempotency)
- WebSocket configuration (STOMP over SockJS)
- DTO mapping strategy (MapStruct)
- Migration strategy (Flyway)

## Artifact Locations

- Proposal: `openspec/changes/implement-backend-spring-boot/proposal.md`
- Specs: `openspec/changes/implement-backend-spring-boot/specs/*.md`
- State: `openspec/changes/implement-backend-spring-boot/state.yaml`

## Risks Documented in Specs

1. **Folio sequence concurrency** — Mitigated with pessimistic locking (INV-02)
2. **Double payment** — Mitigated with unique reference_id (INV-03)
3. **Tenant data leakage** — Mitigated with repository tenant filter enforcement (INV-06)
4. **Cash register consistency** — One open per restaurant (INV-CASH-001)
5. **Order total consistency** — Recalculated on every detail mutation (INV-04)

## Dependencies on Proposal

All specs derive from the proposal scope:
- Spring Boot 3.x + Java 21
- MySQL 8 with Flyway migrations
- JWT + RBAC
- Multi-tenancy per restaurant_id
- STOMP WebSocket
- MapStruct for DTOs

## Traceability

| Spec | Backend Package | Tables | Judgment Double |
|------|-----------------|--------|:---------------:|
| AUTH-001 | auth | app_user, role, user_restaurant_role | |
| ORDER-001 | order | order, order_detail | |
| ORDER-002 | order | order_detail | |
| BILL-001 | billing | invoice, bill, folio_sequence, transaction | ✅ |
| CASH-001 | cash | cash_register, transaction | ✅ |
| MENU-001 | menu | section, category, product, product_option | |
| TABLE-001 | table | restaurant_table | |
| REPORT-001 | report | (read aggregate) | |
