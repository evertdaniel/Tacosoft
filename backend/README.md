# Tacosoft Backend - Spring Boot 3 Restaurant Management System

A comprehensive, production-ready restaurant management backend built with **Spring Boot 3.3.x**, **Java 21**, and **MySQL 8**. Implements multi-tenant architecture, financial invariants, and real-time order processing via WebSocket.

## 🎯 Project Overview

Tacosoft is a complete restaurant POS (Point of Sale) backend handling:
- **Multi-tenant restaurant management** with RBAC (Role-Based Access Control)
- **Menu management** (sections, categories, products, options)
- **Order processing** with real-time WebSocket updates
- **Billing & invoicing** with sequential folio generation
- **Cash register management** with X/Z reports
- **Advanced reporting** (sales, products, finances, footfall)
- **Supplier management**

### Key Business Invariants (INV-01 to INV-06)
- **INV-01**: Unique order numbers per restaurant (`UNIQUE (restaurant_id, num)`)
- **INV-02**: Contiguous invoice folios without gaps (pessimistic locking)
- **INV-03**: Idempotent payments via `UNIQUE (reference_id)`
- **INV-04**: Order totals always match sum of details
- **INV-05**: No transactions on closed cash registers
- **INV-06**: Strict tenant isolation (cross-restaurant data protection)

---

## 🛠 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 3.3.x |
| **Language** | Java | 21 (Virtual Threads enabled) |
| **Database** | MySQL | 8.0+ |
| **Migration** | Flyway | - |
| **Security** | Spring Security + JWT | - |
| **API Documentation** | SpringDoc OpenAPI | 2.x |
| **WebSocket** | STOMP over SockJS | - |
| **Testing** | JUnit 5 + Testcontainers | - |
| **Build Tool** | Maven | 3.9+ |

---

## 📋 Prerequisites

Before running this application, ensure you have:

- **Java 21** installed ([Adoptium Temurin](https://adoptium.net/) recommended)
- **Maven 3.9+** for building the project
- **MySQL 8.0+** running locally or accessible via `DB_HOST`
- **Git** for cloning the repository

### Verify Prerequisites

```bash
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.9+
mysql --version  # Should show MySQL 8.0+
```

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/tacosoft.git
cd tacosoft/backend
```

### 2. Set Environment Variables

Create a `.env` file or export these environment variables:

```bash
# Database Configuration
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=tacosoft
export DB_USER=tacosoft_user
export DB_PASS=secure_password_here

# JWT Configuration
export JWT_SECRET=your_super_secret_jwt_key_at_least_32_chars

# Frontend Configuration
export FRONTEND_URL=http://localhost:3000

# Optional: Override default port (default: 8080)
export SERVER_PORT=8080
```

### 3. Create Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE tacosoft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tacosoft_user'@'localhost' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON tacosoft.* TO 'tacosoft_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 4. Run Database Migrations

Flyway migrations run automatically on startup. To verify migrations:

```bash
mvn flyway:info
```

### 5. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

Or use Maven directly:

```bash
mvn spring-boot:run
```

### 6. Verify Installation

The application should start on `http://localhost:8080`. Verify:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **API Docs**: http://localhost:8080/v3/api-docs

---

## 🧪 Testing

### Run All Tests

```bash
# Unit tests (fast)
mvn test

# Integration tests (requires MySQL)
mvn verify

# Specific test class
mvn test -Dtest=AuthServiceTest

# Financial invariant tests (critical)
mvn test -Dtest=*FinancialInvariantTest
```

### Test Coverage

```bash
# Generate coverage report (requires JaCoCo plugin)
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Critical Invariant Tests

These tests validate financial correctness and **must pass** before deployment:

- `InvoiceFinancialInvariantTest` → INV-02 (folio sequence)
- `TransactionInvariantTest` → INV-03 (idempotent payments)
- `CashRegisterInvariantTest` → INV-05 (closed register protection)
- `TenantIsolationTest` → INV-06 (cross-tenant protection)

---

## 📖 API Documentation

### Swagger UI

Interactive API documentation available at: **http://localhost:8080/swagger-ui.html**

### Authentication

All endpoints (except `/auth/login`) require JWT authentication:

```bash
# Login to get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Response: { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", ... }

# Use token in subsequent requests
curl -X GET http://localhost:8080/orders \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "x-restaurant-id: <restaurant_uuid>"
```

### Key Endpoints

| Module | Endpoint | Description |
|--------|----------|-------------|
| **Auth** | `POST /auth/login` | Authenticate and get JWT |
| **Orders** | `POST /orders` | Create order (WebSocket broadcast) |
| **Orders** | `GET /orders/active` | List active orders |
| **Invoices** | `POST /invoices` | Create invoice (folio assignment) |
| **Invoices** | `POST /invoices/{id}/pay` | Pay invoice (idempotent) |
| **Cash Registers** | `PUT /cash-registers/{id}/close` | Close register with Z-report |
| **Reports** | `GET /reports/dashboard` | Dashboard statistics |

---

## 🔒 Architecture Overview

### Multi-Tenancy (ADR-004)

Tacosoft uses **shared database, shared schema** with tenant isolation via:

1. **Tenant Filter**: Validates `x-restaurant-id` header against JWT claims
2. **Thread-Local Context**: `TenantContext` stores current restaurant ID
3. **Repository Filters**: All queries auto-filter by `restaurant_id`
4. **Security Layer**: Spring Security enforces RBAC matrix

### Security Model (ADR-001)

- **Authentication**: JWT tokens with HS256 signing
- **Authorization**: RBAC with restaurant-scoped roles
- **Roles**: `ADMIN`, `COOK`, `WAITER`, `CASHIER`
- **JWT Claims**: `sub`, `username`, `role`, `restaurantRoles`, `exp`

### Financial Invariants (ADR-003, ADR-005)

Critical business rules enforced at database and service layers:

- **Folio Sequence**: Pessimistic lock (`SELECT FOR UPDATE`) prevents gaps
- **Idempotent Payments**: Unique constraint on `reference_id`
- **Order Totals**: Recalculated on every detail mutation
- **Closed Registers**: Service layer validation before transactions

### Real-Time Updates (ADR-006)

- **WebSocket**: STOMP over SockJS
- **Topics**: Restaurant-scoped (`/topic/restaurant/{id}/orders`, `/tables`)
- **Broadcasts**: Order creation, status changes, table updates

---

## 🗄 Database Schema

### Migration Strategy (Flyway)

- **Location**: `src/main/resources/db/migration/`
- **Naming**: `V{version}__{description}.sql`
- **Rule**: Migrations are **immutable** — never modify committed migrations
- **Versioning**: Incremental versions (V1, V2, V3, ...)

### Key Tables

| Table | Description | Key Constraints |
|-------|-------------|-----------------|
| `restaurant` | Tenant organizations | Primary key for all FKs |
| `app_user` | User accounts | RBAC integration |
| `order` | Orders | `UNIQUE (restaurant_id, num)` |
| `invoice` | Invoices | `UNIQUE (restaurant_id, folio)` |
| `transaction` | Cash register transactions | `UNIQUE (reference_id)` |
| `cash_register` | Cash registers | Status validation |

### Seed Data

Roles are seeded in `V1__init_tenancy_users.sql`:
- `ADMIN` (full access)
- `COOK` (kitchen operations)
- `WAITER` (order/table management)
- `CASHIER` (billing and cash registers)

---

## 🚢 Deployment Checklist

### Pre-Deployment

- [ ] All tests pass (`mvn verify`)
- [ ] Financial invariant tests pass (`mvn test -Dtest=*FinancialInvariantTest`)
- [ ] Database migrations verified (`mvn flyway:info`)
- [ ] Environment variables configured
- [ ] JWT_SECRET is strong (32+ chars)
- [ ] MySQL backup performed

### Production Configuration

Update `application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASS}
  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'update' in production
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  port: ${SERVER_PORT:8080}
  error:
    include-message: always
    include-binding-errors: always

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours

cors:
  allowed-origins: ${FRONTEND_URL}
```

### Build for Production

```bash
# Build optimized JAR
mvn clean package -Pprod

# Run with production profile
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment

```dockerfile
# Dockerfile example
FROM eclipse-temurin:21-jre-alpine
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t tacosoft-backend .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e JWT_SECRET=your_prod_secret \
  tacosoft-backend
```

---

## 🛠 Development Guidelines

### Conventional Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

```bash
git commit -m "feat(invoices): add partial payment support"
git commit -m "fix(cash): prevent transactions on closed register (INV-05)"
git commit -m "test(order): add order total invariant test (INV-04)"
git commit -m "docs(readme): update deployment instructions"
```

### Financial Changes (ADR-005)

For any change affecting billing, payments, or cash registers:

1. **Code Review**: Required from senior developer
2. **Judgment Double**: Two independent reviews for financial logic
3. **Invariant Tests**: Must pass all `*FinancialInvariantTest` tests
4. **Migration Review**: Database changes reviewed by DBA

### Code Style

- **Java 21 Features**: Use records, pattern matching, virtual threads where applicable
- **Validation**: Use Jakarta `@Valid` and custom validators
- **Error Handling**: RFC 7807 `ProblemDetail` for all exceptions
- **Logging**: Structured logging with appropriate levels

---

## 📚 Additional Documentation

### Architecture Decision Records (ADRs)

- **ADR-001**: Spring Boot 3 + Java 21 Technology Stack
- **ADR-002**: Domain-Driven Design & Feature-Based Structure
- **ADR-003**: Financial Invariants via Pessimistic Locking
- **ADR-004**: Multi-Tenancy via Tenant Filter
- **ADR-005**: Judgment Day Process for Financial Changes
- **ADR-006**: Real-Time Updates via WebSocket

### Specification References

- **SPEC-AUTH-001**: Authentication & Authorization
- **SPEC-MENU-001**: Menu Management
- **SPEC-ORDER-001**: Order Creation & Management
- **SPEC-ORDER-002**: Order Detail Status Transitions
- **SPEC-BILL-001**: Billing & Invoicing
- **SPEC-CASH-001**: Cash Register Management
- **SPEC-REPORT-001**: Reporting & Analytics

---

## 🐛 Troubleshooting

### Common Issues

**1. Flyway Migration Fails**

```bash
# Check migration status
mvn flyway:info

# Repair if checksum mismatch
mvn flyway:repair

# Manual baseline (use with caution)
mvn flyway:baseline
```

**2. JWT Token Invalid**

- Verify `JWT_SECRET` matches between generation and validation
- Check token expiration (default: 24 hours)
- Ensure `Authorization` header format: `Bearer <token>`

**3. Tenant Isolation Errors**

- Verify `x-restaurant-id` header is set
- Check JWT contains `restaurantRoles` claim
- Ensure user has role in target restaurant

**4. Cash Register Closed (INV-05)**

```bash
# Check open registers
GET /cash-registers/active

# Open new register
POST /cash-registers
{
  "opening_amount": 100.00
}
```

---

## 📞 Support & Contributing

### Getting Help

- **Documentation**: Check Swagger UI first (`/swagger-ui.html`)
- **Logs**: Enable debug logging: `logging.level.com.restaurant.app=DEBUG`
- **Tests**: Run invariant tests to verify business rules

### Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feat/your-feature`
3. Write tests for your changes
4. Ensure all tests pass: `mvn verify`
5. Commit with conventional commits
6. Submit pull request

For financial changes, follow **ADR-005 Judgment Day** process.

---

## 📄 License

[Specify your license here - e.g., MIT, Apache 2.0, etc.]

---

## 🎓 Acknowledgments

Built with:
- [Spring Boot](https://spring.io/projects/spring-boot) - Core framework
- [MySQL](https://www.mysql.com/) - Database
- [Flyway](https://flywaydb.org/) - Database migrations
- [Testcontainers](https://www.testcontainers.org/) - Integration testing
- [SpringDoc OpenAPI](https://springdoc.org/) - API documentation

---

**Status**: ✅ Production Ready | **Version**: 1.0.0 | **Last Updated**: 2025-06-21
