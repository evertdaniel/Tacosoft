# Verification Report

**Change**: `implement-backend-spring-boot`  
**Version**: N/A  
**Mode**: Strict TDD  
**Verified**: 2026-06-21  
**Verifier**: sdd-verify executor

---

## Completeness

| Metric | Value |
|--------|-------|
| Tasks total | 59 |
| Tasks complete | 59 |
| Tasks incomplete | 0 |

All tasks in `tasks.md` are marked complete. No unchecked implementation tasks remain.

---

## Build & Tests Execution

**Build**: ✅ Passed

```text
mvn -f backend/pom.xml compile
[INFO] BUILD SUCCESS
```

**Unit Tests**: ✅ 39 passed / ❌ 0 failed / ⚠️ 0 skipped

```text
mvn -f backend/pom.xml test -Dtest='!*IntegrationTest,!*InvariantTest'
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Integration / Invariant Tests**: ❌ Could not execute

```text
mvn -f backend/pom.xml verify -DskipITs=false -Dit.test=RepositoryIntegrationTest
Tests run: 3, Failures: 0, Errors: 3, Skipped: 0
BUILD FAILURE
```

Root cause:
- Docker daemon is installed but the current user lacks permission to connect (`permission denied while trying to connect to the docker API`).
- Testcontainers cannot start MySQL, so tests fall back to the embedded H2 database.
- Flyway migration `V8__init_reports_views.sql` uses MySQL-specific syntax (`HOUR(o.created_at) AS hour` where `hour` is a reserved keyword in H2, plus MySQL backticks around table names) and fails on H2.
- This blocks all 33 integration/invariant tests tagged `@Tag("integration")` from running in this environment.

**Coverage**: 11% line coverage (target: >80%) → ⚠️ Below threshold

```text
JaCoCo report: Total 12,497 of 14,191 missed → 11% line coverage
```

**Spotless**: ✅ Passed

```text
mvn -f backend/pom.xml spotless:check
BUILD SUCCESS
```

---

## TDD Compliance

| Check | Result | Details |
|-------|--------|---------|
| TDD Evidence reported | ❌ | No `apply-progress` artifact found in `openspec/changes/implement-backend-spring-boot/` |
| All tasks have tests | ⚠️ | Unit tests exist for core services; integration tests exist but are environment-blocked |
| RED confirmed (tests exist) | ✅ | Test files exist for all major domains |
| GREEN confirmed (tests pass) | ⚠️ | 39/39 unit tests pass; 33 integration tests could not be verified |
| Triangulation adequate | ⚠️ | Unit tests cover happy path + main errors; several integration tests contain placeholder/empty test bodies |
| Safety Net for modified files | ➖ | Not reported by apply phase |

**TDD Compliance**: 2/6 checks passed. Under Strict TDD, the missing `apply-progress` artifact is a protocol violation.

---

## Test Layer Distribution

| Layer | Tests | Files | Tools |
|-------|-------|-------|-------|
| Unit | 39 | 4 | JUnit 5 + Mockito |
| Integration | 33 (blocked) | 7 | Spring Boot Test + Testcontainers (MySQL) |
| E2E | 0 | 0 | Not installed |
| **Total** | **72 written / 39 executed** | **11** | |

---

## Assertion Quality

| File | Line | Assertion / Test | Issue | Severity |
|------|------|------------------|-------|----------|
| `ControllerIntegrationTest.java` | 217 | `rbacEnforcement_UserWithoutAdminRole_Returns403` | Empty placeholder body — no assertions | WARNING |
| `SecurityPenetrationTest.java` | 135 | `rbacEnforcement_UnauthorizedRole_Rejected` | Empty placeholder body — no assertions | WARNING |
| `SecurityPenetrationTest.java` | 232 | `csrfProtection_RequiredForStateChangingRequests` | Empty body with only `System.out.println` | WARNING |
| `SecurityPenetrationTest.java` | 267 | `bruteForcePrevention_AccountLockout` | Empty body with only `System.out.println` | WARNING |
| `TenantIsolationTest.java` | 173 | `operationsWithoutTenantContext_ThrowException` | Expects `IllegalStateException` but implementation allows `null` tenant through; test may not match runtime behavior | WARNING |

**Assertion quality**: 0 CRITICAL, 5 WARNING

---

## Spec Compliance Matrix

| Requirement | Scenario | Test | Result |
|-------------|----------|------|--------|
| **SPEC-AUTH-001** | | | |
| AUT-001 | Valid credentials → 200 + JWT | `AuthServiceTest.login_ValidCredentials_ReturnsLoginResponse` | ✅ COMPLIANT |
| AUT-002 | Invalid username → 401 | `AuthServiceTest.login_InvalidUsername_ThrowsUnauthorizedException` | ✅ COMPLIANT |
| AUT-003 | Invalid password → 401 | `AuthServiceTest.login_InvalidPassword_ThrowsUnauthorizedException` | ✅ COMPLIANT |
| AUT-004 | Inactive user → 401 | `AuthServiceTest.login_InactiveUser_ThrowsUnauthorizedException` | ✅ COMPLIANT |
| AUT-005 | No restaurants → `currentRestaurant: null` | `AuthServiceTest.login_UserWithNoRestaurantRoles_ReturnsNullCurrentRestaurant` | ✅ COMPLIANT |
| AUT-006 | Token claims (`sub`, `role`, `restaurantRoles`, `exp`, `iat`) | `JwtServiceTest.tokenContainsRequiredClaims` | ✅ COMPLIANT |
| AUT-007 | Token expires after 120 min | `JwtServiceTest.tokenExpiration_IsFuture` | ⚠️ PARTIAL (only checks future, not exact duration) |
| AUT-008 | Invalid signature → 401 | `JwtServiceTest.extractClaims_TamperedToken_ReturnsNull` | ✅ COMPLIANT |
| AUT-009 | Malformed payload → 400 | No covering test found | ❌ UNTESTED |
| **SPEC-ORDER-001** | | | |
| ORD-001 | Valid order → 201 Created | `OrderServiceTest.createOrder_ValidInPlaceOrder_CreatesOrderSuccessfully` | ⚠️ PARTIAL (service OK; controller returns 200 not 201) |
| ORD-002 | Product inactive → 400 | No direct test | ❌ UNTESTED |
| ORD-003 | Product out of stock → 409 | No direct test | ❌ UNTESTED |
| ORD-004 | Table not found → 404 | `OrderServiceTest.createOrder_TableNotAvailable_ThrowsConflictException` | ⚠️ PARTIAL (returns 409, not 404) |
| ORD-005 | Table occupied → 409 | `OrderServiceTest.createOrder_TableNotAvailable_ThrowsConflictException` | ✅ COMPLIANT |
| ORD-006 | IN_PLACE without table → 400 | `OrderServiceTest.createOrder_InPlaceOrderWithoutTable_ThrowsConflictException` | ⚠️ PARTIAL (throws `ConflictException` → 409, not 400) |
| ORD-007 | Quantity ≤ 0 → 400 | No covering test; not enforced in code | ❌ UNTESTED |
| ORD-008 | Product not in tenant → 403 | Covered indirectly via repository tenant filtering | ⚠️ PARTIAL |
| ORD-009 | User without WAITER/ADMIN → 403 | No `@PreAuthorize` on `OrderController`; security config lacks role enforcement | ❌ UNTESTED |
| ORD-010 | WebSocket `order:created` emitted | `OrderServiceTest.createOrder_ValidInPlaceOrder_CreatesOrderSuccessfully` | ⚠️ PARTIAL (broadcasts DTO, not typed `order:created` event) |
| ORD-011 | Table becomes OCCUPIED | `OrderServiceTest.createOrder_ValidInPlaceOrder_CreatesOrderSuccessfully` | ✅ COMPLIANT |
| ORD-012 | Sequential `order.num` | `OrderServiceTest.createOrder_GeneratesSequentialOrderNumbers` | ✅ COMPLIANT |
| **SPEC-ORDER-002** | | | |
| ORD-ST-001 | Valid detail transition → 200 | `OrderDetailServiceTest.updateStatus_ValidTransition_UpdatesStatus` | ✅ COMPLIANT |
| ORD-ST-002 | Invalid transition → 409 | `OrderDetailServiceTest.updateStatus_InvalidTransition_ThrowsConflictException` | ✅ COMPLIANT |
| ORD-ST-003 | User without COOK/ADMIN → 403 | No `@PreAuthorize` on controller | ❌ UNTESTED |
| ORD-ST-006 | All details DELIVERED → order DELIVERED | No test; `OrderDetailService` recalculates total but does not update `Order.status` per spec algorithm | ❌ UNTESTED |
| ORD-ST-007 | One detail IN_PROGRESS → order IN_PROGRESS | No test; `Order.status` is not updated on detail changes | ❌ UNTESTED |
| ORD-ST-008 | WebSocket `order-detail:updated` | `OrderDetailService.broadcastOrderDetailChange` sends detail DTO | ⚠️ PARTIAL (no event-type wrapper) |
| **SPEC-BILL-001** | | | |
| BILL-001 | Create invoice → 201 with unique folio | No unit test; integration test blocked | ❌ UNTESTED |
| BILL-002 | 100 concurrent invoices → contiguous folios | `InvoiceFinancialInvariantTest.invoiceFolioConcurrency_1000Threads...` | ❌ FAILING (cannot run) |
| BILL-003 | Full payment → invoice paid + transaction | `TransactionInvariantTest.payInvoice_DuplicateCalls_Idempotent...` | ❌ FAILING (cannot run) |
| BILL-004 | Duplicate payment idempotent | `TransactionInvariantTest.payInvoice_DuplicateCalls_Idempotent...` | ❌ FAILING (cannot run) |
| BILL-005 | No open register → 409 | `CashRegisterInvariantTest.transactionOnClosedRegister_ThrowsConflict` | ❌ FAILING (cannot run) |
| BILL-007 | `invoice.total = subtotal + tax` | `InvoiceService.createInvoice` hardcodes 16% from total; not derived from order details | ⚠️ PARTIAL |
| BILL-008 | `order.isPaid` when all invoices paid | Not implemented in `InvoiceService.payInvoice` | ❌ UNTESTED |
| BILL-010 | User without CASHIER/ADMIN → 403 | No `@PreAuthorize` on `InvoiceController` | ❌ UNTESTED |
| **SPEC-CASH-001** | | | |
| CASH-001 | Open register → 201 OPEN | No unit test; integration test blocked | ❌ UNTESTED |
| CASH-002 | Second open register (same restaurant) → 409 | `CashRegisterService.openRegister` checks per-user, not per-restaurant; violates INV-CASH-001 | ❌ FAILING |
| CASH-003 | Close register → 200 + Z-report | `CashRegisterInvariantTest.zReport_BalanceCalculation_Accurate_INV05` | ❌ FAILING (cannot run) |
| CASH-005 | Closed register rejects transactions | `CashRegisterInvariantTest.transactionOnClosedRegister_ThrowsConflict` | ❌ FAILING (cannot run) |
| CASH-006 | Different user closes → 403 | `CashRegisterService.closeRegister` rejects non-owner, but ADMIN override not implemented | ⚠️ PARTIAL |
| **SPEC-TABLE-001** | | | |
| TABLE-001 | Create table → 201 (ADMIN only) | No `@PreAuthorize` on `TableController` | ❌ UNTESTED |
| TABLE-002 | Seats ≤ 0 → 400 | `CreateTableRequest` validation exists but no test verified | ⚠️ PARTIAL |
| TABLE-004 | Valid status transition + WebSocket | `TableService.updateTableStatus` validates transitions | ⚠️ PARTIAL (transition set incomplete: OCCUPIED→CLEANING missing) |
| TABLE-007 | Delete table with active orders → 409 | `TableService.deleteTable` only checks `status = OCCUPIED`, not active orders | ⚠️ PARTIAL |
| **SPEC-MENU-001** | | | |
| MENU-001 | Create section → 201 | No unit test; integration test blocked | ❌ UNTESTED |
| MENU-008 | Non-ADMIN → 403 | No `@PreAuthorize` on menu controllers | ❌ UNTESTED |
| MENU-010 | Public menu filters `isPublic=true` + `isActive=true` | No dedicated endpoint `/menu` found | ❌ UNTESTED |
| **SPEC-REPORT-001** | | | |
| REP-001 | Dashboard metrics | No test; report endpoints depend on views blocked by V8 migration | ❌ UNTESTED |
| REP-004 | Finances net cash flow | No test | ❌ UNTESTED |
| REP-007 | Range > 1 year → 400 | No validation found in `ReportController` | ❌ UNTESTED |

**Compliance summary**: Many core financial and RBAC scenarios are untested or only partially covered because integration tests cannot run and method-level security is not enforced.

---

## Correctness (Static Evidence)

| Requirement | Status | Notes |
|------------|--------|-------|
| JWT generation / validation | ✅ Implemented | `JwtService` with HS256, required claims, expiration |
| Tenant context thread-local | ✅ Implemented | `TenantContext` with set/get/clear |
| Tenant filter | ⚠️ Partial | `TenantFilter` validates roles when header present, but allows requests without `x-restaurant-id` to proceed |
| Method-level RBAC | ❌ Not implemented | Controllers lack `@PreAuthorize`; `UserDetailsAdapter` only exposes `ROLE_` prefix, not per-restaurant authorities |
| Order creation + table occupancy | ✅ Implemented | `OrderService.createOrder` validates table and sets OCCUPIED |
| Order total invariant (INV-04) | ✅ Implemented | Recalculated in `OrderService` and `OrderDetailService` |
| Order status derivation (SPEC-ORDER-002) | ❌ Not implemented | `Order.status` is not updated when detail status changes |
| Sequential order numbers (INV-01) | ✅ Implemented | `OrderRepository.findMaxNumByRestaurantId` |
| Folio pessimistic lock (INV-02) | ✅ Implemented | `FolioSequenceRepository.lockByRestaurantId` with `@Lock(PESSIMISTIC_WRITE)` |
| Idempotent payment (INV-03) | ⚠️ Partial | `InvoiceService.payInvoice` checks `existsByReferenceId`, but has a redundant second check and does not update `order.is_paid` |
| Cash register balance (INV-05) | ✅ Implemented | `CashRegisterService.closeRegister` calculates expected amount |
| One open register per restaurant (INV-CASH-001) | ❌ Violated | `openRegister` checks per-user, not per-restaurant |
| Invoice tax calculation | ⚠️ Partial | Hardcoded 16% split from `order.total`; not based on product tax rates or order details |
| RFC 7807 error responses | ✅ Implemented | `GlobalExceptionHandler` returns `ProblemDetail` |
| WebSocket broadcasting | ⚠️ Partial | Topics exist, but event payloads lack `order:created` / `table:updated` event-type wrappers |

---

## Coherence (Design)

| Decision | Followed? | Notes |
|----------|-----------|-------|
| Feature-based package structure | ✅ Yes | `auth`, `order`, `billing`, `cash`, `menu`, `table`, `report`, `supplier` packages |
| Controller → Service → Repository | ✅ Yes | Layered architecture respected |
| MapStruct DTO mapping | ✅ Yes | Mappers present for all domains |
| Flyway migrations | ✅ Yes | 16 migrations present |
| `hibernate.ddl-auto: validate` | ✅ Yes | `application.yml` configured correctly |
| RFC 7807 ProblemDetail | ✅ Yes | `GlobalExceptionHandler` implemented |
| Pessimistic lock for folio | ✅ Yes | `FolioSequenceRepository.lockByRestaurantId` |
| STOMP over SockJS | ⚠️ Partial | `WebSocketConfig` enables STOMP, but no SockJS fallback endpoint registered |
| Method-level RBAC with `@PreAuthorize` | ❌ No | Design §5.2 specifies `@PreAuthorize`; not used anywhere |
| Controller returns 201 for creations | ❌ No | `OrderController.createOrder` and `InvoiceController.createInvoice` return 200 |
| Tenant header mandatory | ❌ No | `TenantFilter` permits missing header; services fail later with `IllegalStateException` or NPE |

---

## Changed File Coverage

Coverage tool (JaCoCo) is available. Based on the latest report:

| File | Line % | Rating |
|------|--------|--------|
| Service classes (tested units) | ~60-80% | ⚠️ Acceptable |
| Controller classes | ~0-20% | ⚠️ Low |
| Repository classes | ~0-10% | ⚠️ Low |
| Financial services (`InvoiceService`, `CashRegisterService`) | ~30-50% | ⚠️ Low |
| Overall project | **11%** | ⚠️ Low |

**Average changed file coverage**: ~11%  
**Coverage threshold**: 80% → **Below target**

---

## Quality Metrics

**Linter / Formatter**: ✅ Spotless `check` passes; no formatting errors.

**Type Checker**: ➖ Not applicable (Java compilation succeeds).

**Integration test environment**: ❌ Blocked by Docker permissions and H2/MySQL migration incompatibility.

---

## Issues Found

### CRITICAL

1. **Strict TDD protocol violation — no `apply-progress` artifact**. The apply phase did not produce a TDD Cycle Evidence table. Under Strict TDD Mode, this is a mandatory artifact and its absence blocks full TDD compliance.
2. **Integration / invariant tests cannot run in the verification environment**. Docker is not accessible (permission denied) and the fallback H2 database cannot execute MySQL-specific migration `V8__init_reports_views.sql`. All 33 `@Tag("integration")` tests (including the 1000-thread folio concurrency test, idempotent payment test, cash register invariant test, and tenant isolation test) are therefore unverified.
3. **No method-level RBAC enforcement**. Controllers do not use `@PreAuthorize`, so acceptance criteria requiring role-based rejection (e.g., ORD-009, ORD-ST-003, BILL-010, MENU-008, TABLE-001) cannot be satisfied at runtime.
4. **Cash register invariant INV-CASH-001 is violated**. `CashRegisterService.openRegister` prevents a user from opening a second register, but it does **not** enforce one open register per restaurant. A different user in the same restaurant can open another register.
5. **Order status is not derived from detail statuses**. `OrderDetailService.updateOrderDetailStatus` recalculates the order total but never updates `Order.status` per the algorithm in SPEC-ORDER-002, violating INV-ORDER-004 and ORD-ST-006/007.
6. **Controllers return 200 instead of 201 for creation endpoints**. `OrderController.createOrder` and `InvoiceController.createInvoice` use `ResponseEntity.ok(...)`, not `ResponseEntity.status(HttpStatus.CREATED)`, contradicting SPEC-ORDER-001 ORD-001 and SPEC-BILL-001 BILL-001.
7. **JaCoCo line coverage is 11%**, well below the design target of >80% and the `jacoco-maven-plugin` configured minimum of 10% is also barely met. Many controller, repository, and financial code paths are not exercised by the passing unit tests.

### WARNING

8. **Tenant header is not mandatory**. `TenantFilter` allows requests without `x-restaurant-id` to continue; downstream services fail with `IllegalStateException` or `NullPointerException`, not a clear 400/403.
9. **Invoice tax calculation is oversimplified**. `InvoiceService.createInvoice` assumes a flat 16% tax by dividing `order.total` by 1.16. It does not compute subtotal/tax from individual order details or product tax rates as implied by SPEC-BILL-001.
10. **`InvoiceService.payInvoice` does not update `order.is_paid`** when all invoices are paid, failing BILL-008.
11. **`InvoiceService.payInvoice` has redundant duplicate checks** and never validates that payment amount matches invoice total, so partial/overpayments are silently accepted.
12. **Table status transition set is incomplete**. `TableService` does not allow `OCCUPIED → CLEANING`, which SPEC-TABLE-001 lists as valid.
13. **Table deletion checks only `status = OCCUPIED`**, not whether the table has active (unclosed) orders, so TABLE-007 is only partially enforced.
14. **Several tests are empty placeholders** (`rbacEnforcement_UserWithoutAdminRole_Returns403`, `rbacEnforcement_UnauthorizedRole_Rejected`, `csrfProtection_RequiredForStateChangingRequests`, `bruteForcePrevention_AccountLockout`). They do not exercise production code.
15. **WebSocket payloads are raw DTOs**, not typed events (`order:created`, `order-detail:updated`, `table:updated`) as specified.
16. **No public menu endpoint** (`GET /menu`) filtering `isPublic=true` and `isActive=true` was found.
17. **V8 migration uses MySQL-only syntax** (`HOUR(...) AS hour`, backtick identifiers) and will fail on any non-MySQL database, blocking test execution and portability.

### SUGGESTION

18. Add `@PreAuthorize("hasAnyAuthority('WAITER','ADMIN')")` (or equivalent) to controllers and align authorities with per-restaurant roles.
19. Return `ResponseEntity.status(HttpStatus.CREATED)` from creation endpoints.
20. Make `x-restaurant-id` mandatory in `TenantFilter` for non-public endpoints and return 400 when missing.
21. Implement `Order.status` derivation in `OrderDetailService` per SPEC-ORDER-002 algorithm.
22. Enforce `INV-CASH-001` at the restaurant level in `CashRegisterService.openRegister`.
23. Refactor V8 migration to be database-agnostic or split MySQL-specific views from H2-compatible test setup.
24. Replace placeholder security tests with real assertions.
25. Raise coverage by adding controller / repository tests once integration environment is available.

---

## Verdict

### FAIL

The implementation compiles and 39 unit tests pass, but the verification cannot confirm compliance with critical spec scenarios because:

- All 33 integration / invariant tests are blocked by environment and migration issues.
- Strict TDD evidence (`apply-progress`) is missing.
- Several core invariants and acceptance criteria are not implemented or are implemented incorrectly (RBAC, INV-CASH-001, order status derivation, 201 status codes, invoice tax calculation).
- Code coverage is 11%, far below the 80% target.

The change must **not** be considered verified or archive-ready until the CRITICAL issues are resolved and the integration test suite can be executed successfully.
