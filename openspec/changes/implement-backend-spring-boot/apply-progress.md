# Apply Progress: implement-backend-spring-boot

## Split Decision
PR #6 (`pr/6-coverage-controllers-read-admin`) was **518 changed lines**, exceeding the 400-line review budget. The user explicitly chose **chained/stacked PRs** with the **`stacked-to-main`** strategy. The work has been split into two focused stacked PRs:

- **PR #6a**: `pr/6a-coverage-auth-table` — auth + table controller coverage + required production fixes.
- **PR #6b**: `pr/6b-coverage-menu-report` — menu (section/category/product/product-option) + report controller coverage.

## Branch Topology
```
main
  └── pr/5-integration-fixtures
        └── pr/6a-coverage-auth-table
              └── pr/6b-coverage-menu-report
                    └── pr/7a-coverage-order
                          └── pr/7b-coverage-cash-invoice
                                └── pr/8-coverage-services-repositories (PR #8a)
                                      └── pr/8b-coverage-billing-cash-services
                                            └── pr/8c-coverage-repositories
                                                  └── pr/9a-coverage-report-service
                                                        └── pr/9b-coverage-menu-services
                                                              └── pr/9c-coverage-product-service
                                                                    └── pr/9d-i-coverage-product-option-service
```

## PR #6a: `pr/6a-coverage-auth-table`
- **Base**: `pr/5-integration-fixtures`
- **Changed lines**: 297 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/pom.xml` | Modified | Raise JaCoCo line threshold to `0.80`. |
  | `backend/src/main/resources/db/migration/V8__init_reports_views.sql` | Modified | H2-compatible `EXTRACT(HOUR FROM ...)` in `v_footfall`. |
  | `backend/src/main/java/com/restaurant/app/config/CorsConfig.java` | Modified | Use `allowedOriginPatterns` to allow credentials with wildcard. |
  | `backend/src/test/java/com/restaurant/app/auth/controller/AuthControllerTest.java` | Created | MockMvc coverage for `AuthController` login success/validation. |
  | `backend/src/test/java/com/restaurant/app/table/controller/TableControllerTest.java` | Created | MockMvc coverage for `TableController` CRUD + status + RBAC. |

### Test Results (PR #6a)
- `mvn -f backend/pom.xml clean test`: **PASS** (61 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.29 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #6a)
| Scope | Coverage |
|-------|----------|
| `auth.controller` | 100% lines (5/5) |
| `table.controller` | 91% lines (21/23) |

## PR #6b: `pr/6b-coverage-menu-report`
- **Base**: `pr/6a-coverage-auth-table`
- **Changed lines**: 373 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/menu/controller/MenuControllersTest.java` | Created | MockMvc read coverage for section/category/product/product-option controllers. |
  | `backend/src/test/java/com/restaurant/app/report/controller/ReportControllerTest.java` | Created | MockMvc read coverage for `ReportController` dashboard and sales endpoints. |

### Test Results (PR #6b)
- `mvn -f backend/pom.xml clean test`: **PASS** (66 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.32 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #6b)
| Scope | Coverage |
|-------|----------|
| `menu.controller` (combined) | ~48% lines (54/110) — read endpoints covered; write endpoints still uncovered by design. |
| `report.controller` | 39% lines (9/23) — dashboard + sales read endpoints covered. |

## PR #7a: `pr/7a-coverage-order`
- **Base**: `pr/6b-coverage-menu-report`
- **Changed lines**: 246 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/order/controller/OrderControllerTest.java` | Created | MockMvc coverage for `OrderController` create/read/update/delete and `OrderDetailController` status transitions. |

### Test Results (PR #7a)
- `mvn -f backend/pom.xml clean test`: **PASS** (71 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.34 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #7a)
| Scope | Coverage |
|-------|----------|
| `order.controller` | 85.7% lines (102/119) — create/read/update/delete + order-detail status transitions covered. |

## PR #7b: `pr/7b-coverage-cash-invoice`
- **Base**: `pr/7a-coverage-order`
- **Changed lines**: 337 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/controller/CashInvoiceControllerTest.java` | Created | MockMvc coverage for `CashRegisterController` open/close/X-report/list and `InvoiceController` create/pay/list. |

### Test Results (PR #7b)
- `mvn -f backend/pom.xml clean test`: **PASS** (75 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.36 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #7b)
| Scope | Coverage |
|-------|----------|
| `cash.controller` | 100% lines (18/18) — open/close/X-report/list covered. |
| `billing.controller` | 100% lines (13/13) — create/pay/list covered. |

## PR #8a: `pr/8-coverage-services-repositories`
- **Base**: `pr/7b-coverage-cash-invoice`
- **Changed lines**: 383 (under 400-line budget)
  - 208 modifications to existing service tests.
  - 175 new lines for `TenantSecurityExpressionTest`.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/order/service/OrderServiceTest.java` | Modified | Add coverage for `getAllOrders`, `getOrdersByStatus`, `updateOrder`, table-not-found error path. |
  | `backend/src/test/java/com/restaurant/app/order/service/OrderDetailServiceTest.java` | Modified | Add coverage for `getOrderDetailById`, cancellation stock restore, product-option price adjustment, option-unavailable error path. |
  | `backend/src/test/java/com/restaurant/app/security/TenantSecurityExpressionTest.java` | Created | Unit tests for `hasRole`, `hasAnyRole`, `hasAllRoles`, missing tenant/auth/principal edge cases. |

### Test Results (PR #8a)
- `mvn -f backend/pom.xml clean test`: **PASS** (93 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.41 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #8a)
| Scope | Coverage |
|-------|----------|
| `order.service.OrderService` | 73.0% → 98.2% lines (109/111) |
| `order.service.OrderDetailService` | 78.2% → 91.0% lines (142/156) |
| `security.TenantSecurityExpression` | 37.5% → 87.5% lines (21/24) |
| **Overall** | 32.6% → 41.5% lines |

## PR #8b: `pr/8b-coverage-billing-cash-services`
- **Base**: `pr/8-coverage-services-repositories`
- **Changed lines**: 353 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/billing/service/InvoiceServiceTest.java` | Modified | Add coverage for `createInvoice` folio/tax logic, `getInvoice`, `listInvoices`, `listUnpaidInvoices`, idempotent payment paths, missing cash-register error path. |
  | `backend/src/test/java/com/restaurant/app/cash/service/CashRegisterServiceTest.java` | Modified | Add coverage for `closeRegister` balanced/difference/error paths, `getXReport`, `getActiveRegister`, `listRegisters`. |

### Test Results (PR #8b)
- `mvn -f backend/pom.xml clean test`: **PASS** (111 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.41 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #8b)
| Scope | Coverage |
|-------|----------|
| `billing.service.InvoiceService` | 52.0% → 98.0% lines (100/102) |
| `cash.service.CashRegisterService` | 29.0% → 99.0% lines (99/100) |
| **Overall** | 41.5% → 41.7% lines (controllers already covered most of these services) |

## PR #8c: `pr/8c-coverage-repositories`
- **Base**: `pr/8b-coverage-billing-cash-services`
- **Changed lines**: 356 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/order/repository/OrderRepositoryTest.java` | Created | H2 Spring Boot tests for custom queries: `findActiveOrders`, `findByRestaurantIdAndStatusNotIn`, `findMaxNumByRestaurantId`, `getMaxOrderNum`, `findByRestaurantIdAndDateRange`, `findByNumAndRestaurantId`. |
  | `backend/src/test/java/com/restaurant/app/billing/repository/InvoiceRepositoryTest.java` | Created | H2 Spring Boot tests for custom queries: tenant-scoped lookups, folio ordering, unpaid count, `FolioSequence.lockByRestaurantId`. |

### Test Results (PR #8c)
- `mvn -f backend/pom.xml clean test`: **PASS** (125 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.42 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #8c)
| Scope | Coverage |
|-------|----------|
| `order.repository.OrderRepository` | custom query lines exercised (repository interfaces contribute few countable lines) |
| `billing.repository.InvoiceRepository` | custom query lines exercised |
| **Overall** | 41.7% → 41.8% lines |

## PR #9a: `pr/9a-coverage-report-service`
- **Base**: `pr/8c-coverage-repositories`
- **Changed lines**: 318 (under 400-line budget)
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/report/service/ReportServiceTest.java` | Created | Unit tests for `ReportService`: dashboard metrics, sales summary/period comparison, product margin/turnover, financial net cash flow, footfall peak hours, staff planning workload recommendations. |

### TDD Cycle Evidence (PR #9a)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| ReportService coverage | `ReportServiceTest.java` | Unit | ✅ 125/125 | ✅ Written | ✅ Passed | ✅ 11 cases | ✅ Spotless applied |

### Test Results (PR #9a)
- `mvn -f backend/pom.xml clean test -Dtest='!*IntegrationTest,!*InvariantTest'`: **PASS** (136 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **FAIL at `jacoco:check`** (expected — line coverage 0.53 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9a)
| Scope | Coverage |
|-------|----------|
| `report.service.ReportService` | 1.0% → 98.0% lines (294/300) |
| **Overall** | 41.8% → 53.0% lines |

## PR #9b: `pr/9b-coverage-menu-services`
- **Base**: `pr/9a-coverage-report-service`
- **Changed lines**: 443 (slightly over the 400-line soft budget; kept as one focused slice per user-suggested `#9b-i` sections/categories split)
- **Scope decision**: The original PR #9b scope (SectionService, CategoryService, ProductService, ProductOptionService) exceeded the 400-line budget. This slice covers **sections and categories only**; products and options move to PR #9c.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/menu/service/SectionServiceTest.java` | Created | Unit tests for `SectionService`: CRUD, restaurant scoping, tenant isolation, partial updates. |
  | `backend/src/test/java/com/restaurant/app/menu/service/CategoryServiceTest.java` | Created | Unit tests for `CategoryService`: CRUD, section relationship validation, tenant isolation, partial updates. |

### TDD Cycle Evidence (PR #9b)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| SectionService coverage | `SectionServiceTest.java` | Unit | N/A (new) | ✅ Written | ✅ Passed | ✅ 13 cases | ✅ Spotless applied |
| CategoryService coverage | `CategoryServiceTest.java` | Unit | N/A (new) | ✅ Written | ✅ Passed | ✅ 11 cases | ✅ Spotless applied |

### Test Results (PR #9b)
- `mvn -f backend/pom.xml clean test -Dtest=SectionServiceTest,CategoryServiceTest`: **PASS** (24 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **PASS** (160 surefire + 15 failsafe tests), **FAIL at `jacoco:check`** (expected — line coverage 0.54 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9b)
| Scope | Before | After |
|-------|--------|-------|
| `menu.service.SectionService` | 7.4% lines (4/54) | **100% lines (54/54)** |
| `menu.service.CategoryService` | 10.2% lines (5/49) | **100% lines (49/49)** |
| `menu.service.ProductService` | 5.5% lines (6/109) | 5.5% lines (6/109) — unchanged, covered in PR #9c |
| `menu.service.ProductOptionService` | 7.9% lines (5/63) | 7.9% lines (5/63) — unchanged, covered in PR #9c |
| **Overall project** | 53.0% lines | **54% lines** |

## PR #9c: `pr/9c-coverage-product-service`
- **Base**: `pr/9b-coverage-menu-services`
- **Changed lines**: 428 (slightly over the 400-line soft budget; kept as the minimum focused slice for `ProductService`)
- **Scope decision**: The original PR #9c scope (`ProductService`, `ProductOptionService`, `TableService`, `UserService`, `SupplierService`, and `UserDetailsServiceAdapter`) far exceeded the 400-line review budget. This slice covers **only `ProductService`**; the remaining services move to follow-up PRs.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/menu/service/ProductServiceTest.java` | Created | Unit tests for `ProductService`: CRUD, status, stock, price, tax, restaurant scoping, category/production-area validation, and stock-management edge cases. |

### TDD Cycle Evidence (PR #9c)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| ProductService coverage | `ProductServiceTest.java` | Unit | ✅ 160/160 | ✅ Written | ✅ Passed | ✅ 18 cases | ✅ Spotless applied |

### Test Results (PR #9c)
- `mvn -f backend/pom.xml clean test -Dtest=ProductServiceTest`: **PASS** (18 tests).
- `mvn -f backend/pom.xml clean verify -DskipITs=false`: **PASS** (160 surefire + 15 failsafe tests), **FAIL at `jacoco:check`** (expected — line coverage 0.55 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9c)
| Scope | Before | After |
|-------|--------|-------|
| `menu.service.ProductService` | 5.5% lines (6/109) | **100% lines (109/109)** |
| `menu.service.ProductOptionService` | 7.9% lines (5/63) | 7.9% lines (5/63) — unchanged, planned for next slice |
| `menu.service` (combined package) | 37.2% lines (118/317) | **69.7% lines (221/317)** |
| `table.service` | 10.4% lines (10/96) | 10.4% lines (10/96) — unchanged, planned for next slice |
| `user.service` | 7.4% lines (7/95) | 7.4% lines (7/95) — unchanged, planned for next slice |
| `supplier.service` | 6.6% lines (4/61) | 6.6% lines (4/61) — unchanged, planned for next slice |
| **Overall project** | 54.0% lines | **55.3% lines** |

## Removed File
- `backend/src/test/java/com/restaurant/app/controller/ReadAdminControllerTest.java` — replaced by the focused test classes above.

## Production Fixes Placement
- The two production fixes (`V8__init_reports_views.sql` H2 hour fix and `CorsConfig` `allowedOriginPatterns` fix) are kept **only in PR #6a**.
- They are **not duplicated** in downstream PRs because the chain is stacked on top of PR #6a.

## Issues / Notes
1. No remote is configured in this workspace (`git remote -v` returns nothing), so the new branches are currently local only. A remote URL is required before `git push` can succeed.
2. `mvn verify` fails on all branches only at the `jacoco:check` gate because the overall project line coverage is still below 0.80. This is expected and accepted; further coverage slices are required.
3. `mvn clean` must be used when verifying after migration resource changes, because stale `target/classes` copies can produce misleading H2 syntax errors.
4. PR #7 was originally planned as a single 516-line slice covering order + cash register + invoice. It was split into `#7a` (order/order-detail, 246 lines) and `#7b` (cash register/invoice, 337 lines) to respect the 400-line budget.
5. PR #8 was originally planned as a single slice covering services + repositories + `TenantSecurityExpression`. It exceeded the 400-line budget and was split into `#8a` (order services + tenant RBAC, 383 lines), `#8b` (billing/cash services, 353 lines), and `#8c` (order/invoice repositories, 356 lines).
6. Invoice and cash register services were already heavily covered by controller tests from PR #7b, so the service unit tests in PR #8b raised direct unit coverage but only marginally moved overall project coverage.
7. Repository interfaces and `ReportService` inner row-mapper classes contribute very few countable lines to JaCoCo, so repository/query tests verify correctness but do not significantly move overall coverage. Reaching 80% will require covering the remaining large uncovered blocks: `ProductOptionService`, `TableService`, `UserService`, `SupplierService`, mappers, DTOs/entities, and inner row-mapper classes in `ReportRepository`.
8. PR #9c was split further because the combined test code for `ProductService` + `ProductOptionService` alone would have been ~687 lines, well over the 400-line budget. `ProductService` was kept as the focused first slice (428 lines).
9. PR #9d was split into four slices because the combined test code for `ProductOptionService` + `TableService` + `UserService` + `UserDetailsServiceAdapter` + `SupplierService` would have been well over the 400-line budget: #9d-i `ProductOptionService` (269 lines), #9d-ii `TableService` (352 lines), #9d-iii `UserService` (359 lines), and #9d-iv `UserDetailsServiceAdapter` + `SupplierService` (354 lines). MapStruct mappers remain for a follow-up slice.

## PR #9d-i: `pr/9d-i-coverage-product-option-service`
- **Base**: `pr/9c-coverage-product-service`
- **Changed lines**: 269 (under 400-line budget)
- **Scope decision**: The original PR #9d scope (`ProductOptionService`, `TableService`, `UserService`, `SupplierService`, and mappers) far exceeded the 400-line review budget. This slice covers **`ProductOptionService`** only; `TableService`, `UserService` / `UserDetailsServiceAdapter`, `SupplierService`, and mappers move to follow-up PRs.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/menu/service/ProductOptionServiceTest.java` | Created | Unit tests for `ProductOptionService`: CRUD, product-scoped lookups, tenant isolation, partial updates, and product-not-found error path. |

### TDD Cycle Evidence (PR #9d-i)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| ProductOptionService coverage | `ProductOptionServiceTest.java` | Unit | ✅ 178/178 | ✅ Written | ✅ Passed | ✅ 12 cases | ✅ Spotless applied |

### Test Results (PR #9d-i)
- `mvn -f backend/pom.xml clean test -Dtest=ProductOptionServiceTest`: **PASS** (12 tests).
- `mvn -f backend/pom.xml clean test`: **PASS** (178 surefire tests).
- `mvn -f backend/pom.xml verify -DskipITs=false`: **PASS** (178 surefire + 15 failsafe tests), **FAIL at `jacoco:check`** (expected — line coverage 0.61 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9d-i)
| Scope | Before | After |
|-------|--------|-------|
| `menu.service.ProductOptionService` | 7.9% lines (5/63) | **100% lines (63/63)** |
| `menu.service.ProductOptionMapper` | 9.1% lines (1/11) | 9.1% lines (1/11) — unchanged, planned for mapper slice |
| `table.service` | 10.4% lines (10/96) | 10.4% lines (10/96) — unchanged, planned for next slice |
| `user.service` | 7.4% lines (7/95) | 7.4% lines (7/95) — unchanged, planned for next slice |
| `supplier.service` | 6.6% lines (4/61) | 6.6% lines (4/61) — unchanged, planned for next slice |
| **Overall project** | 58.7% lines | **61.4% lines** |

## PR #9d-ii: `pr/9d-ii-coverage-table-service`
- **Base**: `pr/9d-i-coverage-product-option-service`
- **Changed lines**: 352 (under 400-line budget)
- **Scope decision**: The original PR #9d scope (`ProductOptionService`, `TableService`, `UserService`, `SupplierService`, and mappers) far exceeded the 400-line review budget. After covering `ProductOptionService` in PR #9d-i, this slice covers **`TableService`**; `UserService` / `UserDetailsServiceAdapter`, `SupplierService`, and mappers move to follow-up PRs.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/table/service/TableServiceTest.java` | Created | Unit tests for `TableService`: CRUD, active/available queries, status transitions, WebSocket broadcasts, number conflicts, and occupied-table deletion guard. |

### TDD Cycle Evidence (PR #9d-ii)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| TableService coverage | `TableServiceTest.java` | Unit | ✅ 195/195 | ✅ Written | ✅ Passed | ✅ 17 cases | ✅ Spotless applied |

### Test Results (PR #9d-ii)
- `mvn -f backend/pom.xml clean test -Dtest=TableServiceTest`: **PASS** (17 tests).
- `mvn -f backend/pom.xml clean test`: **PASS** (195 surefire tests).
- `mvn -f backend/pom.xml verify -DskipITs=false`: **PASS** (195 surefire + 15 failsafe tests), **FAIL at `jacoco:check`** (expected — line coverage 0.64 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9d-ii)
| Scope | Before | After |
|-------|--------|-------|
| `table.service.TableService` | 10.4% lines (10/96) | **96.9% lines (93/96)** |
| `table.service.TableMapper` | 9.1% lines (1/11) | 9.1% lines (1/11) — unchanged, planned for mapper slice |
| `user.service` | 7.4% lines (7/95) | 7.4% lines (7/95) — unchanged, planned for next slice |
| `supplier.service` | 6.6% lines (4/61) | 6.6% lines (4/61) — unchanged, planned for next slice |
| **Overall project** | 61.4% lines | **63.9% lines** |

## PR #9d-iii: `pr/9d-iii-coverage-user-service`
- **Base**: `pr/9d-ii-coverage-table-service`
- **Changed lines**: 359 (under 400-line budget)
- **Scope decision**: The remaining PR #9d scope (`UserService`, `UserDetailsServiceAdapter`, `SupplierService`, and mappers) still exceeded the 400-line review budget. This slice covers **`UserService`** only; `UserDetailsServiceAdapter`, `SupplierService`, and mappers move to follow-up PRs.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/user/service/UserServiceTest.java` | Created | Unit tests for `UserService`: CRUD, password/role/active/person updates, username conflict, restaurant role assignment/removal, and tenant isolation. |

### TDD Cycle Evidence (PR #9d-iii)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| UserService coverage | `UserServiceTest.java` | Unit | ✅ 213/213 | ✅ Written | ✅ Passed | ✅ 18 cases | ✅ Spotless applied |

### Test Results (PR #9d-iii)
- `mvn -f backend/pom.xml clean test -Dtest=UserServiceTest`: **PASS** (18 tests).
- `mvn -f backend/pom.xml clean test`: **PASS** (213 surefire tests).
- `mvn -f backend/pom.xml verify -DskipITs=false`: **PASS** (213 surefire + 15 failsafe tests), **FAIL at `jacoco:check`** (expected — line coverage 0.68 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9d-iii)
| Scope | Before | After |
|-------|--------|-------|
| `user.service.UserService` | 7.4% lines (7/95) | **100% lines (95/95)** |
| `user.service` (package) | 7.4% lines (7/95) | **100% lines (95/95)** |
| `security.UserDetailsServiceAdapter` | unchanged | unchanged, planned for next slice |
| `supplier.service` | 6.6% lines (4/61) | 6.6% lines (4/61) — unchanged, planned for next slice |
| **Overall project** | 63.9% lines | **67.6% lines (3099/4585)** |

## PR #9d-iv: `pr/9d-iv-coverage-user-details-supplier`
- **Base**: `pr/9d-iii-coverage-user-service`
- **Changed lines**: 354 (under 400-line budget)
- **Scope decision**: The remaining PR #9d scope (`UserDetailsServiceAdapter`, `SupplierService`, and mappers) still fit under the 400-line budget when grouped together. This slice covers **`UserDetailsServiceAdapter`** and **`SupplierService`**; MapStruct mappers move to the next slice.
- **Files changed**:
  | File | Action | Why |
  |------|--------|-----|
  | `backend/src/test/java/com/restaurant/app/security/UserDetailsServiceAdapterTest.java` | Created | Unit tests for `UserDetailsServiceAdapter`: successful load, missing user, missing roles, plus `UserDetailsAdapter` authority and role-in-restaurant checks. |
  | `backend/src/test/java/com/restaurant/app/supplier/service/SupplierServiceTest.java` | Created | Unit tests for `SupplierService`: CRUD, active/search queries, partial updates, tenant isolation, and not-found paths. |

### TDD Cycle Evidence (PR #9d-iv)
| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| UserDetailsServiceAdapter coverage | `UserDetailsServiceAdapterTest.java` | Unit | ✅ 226/226 | ✅ Written | ✅ Passed | ✅ 3 cases | ✅ Spotless applied |
| SupplierService coverage | `SupplierServiceTest.java` | Unit | ✅ 226/226 | ✅ Written | ✅ Passed | ✅ 10 cases | ✅ Spotless applied |

### Test Results (PR #9d-iv)
- `mvn -f backend/pom.xml clean test -Dtest=UserDetailsServiceAdapterTest,SupplierServiceTest`: **PASS** (13 tests).
- `mvn -f backend/pom.xml clean test`: **PASS** (226 surefire tests).
- `mvn -f backend/pom.xml verify -DskipITs=false`: **PASS** (226 surefire + 15 failsafe tests), **FAIL at `jacoco:check`** (expected — line coverage 0.72 < 0.80).
- `mvn -f backend/pom.xml spotless:check`: **PASS**.

### Coverage Delta (PR #9d-iv)
| Scope | Before | After |
|-------|--------|-------|
| `security.UserDetailsServiceAdapter` | 0% lines (0/18) | **100% lines (18/18)** |
| `supplier.service.SupplierService` | 6.6% lines (4/61) | **100% lines (61/61)** |
| `supplier.service` (package) | 6.6% lines (4/61) | **100% lines (61/61)** |
| `auth.mapper` | 2% lines (2/79) | 2% lines (2/79) — unchanged, planned for mapper slice |
| `menu.mapper` | 10% lines (6/60) | 10% lines (6/60) — unchanged, planned for mapper slice |
| `user.mapper` | 4% lines (1/14) | 4% lines (1/14) — unchanged, planned for mapper slice |
| `supplier.mapper` | 7% lines (1/14) | 7% lines (1/14) — unchanged, planned for mapper slice |
| `table.mapper` | 12% lines (1/11) | 12% lines (1/11) — unchanged, planned for mapper slice |
| **Overall project** | 67.6% lines | **71.8% lines (3292/4585)** |

## Branch Topology
```
main
  └── pr/5-integration-fixtures
        └── pr/6a-coverage-auth-table
              └── pr/6b-coverage-menu-report
                    └── pr/7a-coverage-order
                          └── pr/7b-coverage-cash-invoice
                                └── pr/8-coverage-services-repositories (PR #8a)
                                      └── pr/8b-coverage-billing-cash-services
                                            └── pr/8c-coverage-repositories
                                                  └── pr/9a-coverage-report-service
                                                        └── pr/9b-coverage-menu-services
                                                              └── pr/9c-coverage-product-service
                                                                    └── pr/9d-i-coverage-product-option-service
                                                                          └── pr/9d-ii-coverage-table-service
                                                                                └── pr/9d-iii-coverage-user-service
                                                                                      └── pr/9d-iv-coverage-user-details-supplier
```

## Next Steps
1. Configure a Git remote and push `pr/6a-coverage-auth-table`, `pr/6b-coverage-menu-report`, `pr/7a-coverage-order`, `pr/7b-coverage-cash-invoice`, `pr/8-coverage-services-repositories`, `pr/8b-coverage-billing-cash-services`, `pr/8c-coverage-repositories`, `pr/9a-coverage-report-service`, `pr/9b-coverage-menu-services`, `pr/9c-coverage-product-service`, `pr/9d-i-coverage-product-option-service`, `pr/9d-ii-coverage-table-service`, `pr/9d-iii-coverage-user-service`, and `pr/9d-iv-coverage-user-details-supplier`.
2. Open stacked PRs:
   - PR #6a → `pr/5-integration-fixtures`
   - PR #6b → `pr/6a-coverage-auth-table`
   - PR #7a → `pr/6b-coverage-menu-report`
   - PR #7b → `pr/7a-coverage-order`
   - PR #8a → `pr/7b-coverage-cash-invoice`
   - PR #8b → `pr/8-coverage-services-repositories`
   - PR #8c → `pr/8b-coverage-billing-cash-services`
   - PR #9a → `pr/8c-coverage-repositories`
   - PR #9b → `pr/9a-coverage-report-service`
   - PR #9c → `pr/9b-coverage-menu-services`
   - PR #9d-i → `pr/9c-coverage-product-service`
   - PR #9d-ii → `pr/9d-i-coverage-product-option-service`
   - PR #9d-iii → `pr/9d-ii-coverage-table-service`
   - PR #9d-iv → `pr/9d-iii-coverage-user-service`
3. Merge in order: PR #6a → #6b → #7a → #7b → #8a → #8b → #8c → #9a → #9b → #9c → #9d-i → #9d-ii → #9d-iii → #9d-iv.
4. Continue with the next coverage slice(s) for MapStruct mappers, then proceed to DTOs/entities and remaining repository inner classes.
5. Hand off to `sdd-verify` once 80% line coverage is reached and all PRs are merged.
