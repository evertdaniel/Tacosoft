# Tacosoft - Sistema de Gestión para Restaurantes

![CI](https://github.com/evertdaniel/Tacosoft/actions/workflows/ci.yml/badge.svg)

API backend para la administración integral de restaurantes, construida con **Spring Boot 3.3**, **Java 21** y **Maven**. El sistema permite gestionar autenticación, usuarios, menús, mesas, órdenes, facturación, caja, reportes y proveedores, con soporte multi-tenant, control de acceso basado en roles (RBAC) y seguridad mediante JWT.

---

## Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Características Principales](#características-principales)
3. [Stack Tecnológico](#stack-tecnológico)
4. [Arquitectura](#arquitectura)
5. [Requisitos Previos](#requisitos-previos)
6. [Configuración del Entorno](#configuración-del-entorno)
7. [Cómo Ejecutar](#cómo-ejecutar)
8. [Base de Datos y Migraciones](#base-de-datos-y-migraciones)
9. [Seguridad](#seguridad)
10. [Testing](#testing)
11. [Cobertura de Código](#cobertura-de-código)
12. [Formato de Código](#formato-de-código)
13. [Documentación de API](#documentación-de-api)
14. [WebSocket](#websocket)
15. [Estructura del Proyecto](#estructura-del-proyecto)
16. [Flujo de Trabajo y Contribución](#flujo-de-trabajo-y-contribución)
17. [Decisiones Técnicas Destacadas](#decisiones-técnicas-destacadas)
18. [Limitaciones Conocidas](#limitaciones-conocidas)
19. [Licencia](#licencia)

---

## Descripción General

Tacosoft es una API REST diseñada para operar la parte administrativa y operativa de uno o varios restaurantes. Cada restaurante funciona como un tenant aislado: los usuarios, productos, órdenes, facturas y demás entidades están siempre asociados a un `restaurant_id` y el acceso se controla mediante un header `x-restaurant-id`.

El sistema fue desarrollado siguiendo el enfoque **Spec-Driven Development (SDD)**: los requisitos se documentaron primero en especificaciones bajo `openspec/changes/`, y la implementación se validó mediante tests unitarios e integración antes de ser mergeada.

---

## Características Principales

### Autenticación y Autorización
- Login con JWT (HS256, expiración de 120 minutos).
- Roles: `ADMIN`, `WAITER`, `COOK`, `CASHIER`.
- RBAC a nivel de métodos de controlador con `@PreAuthorize`.
- Control de acceso multi-tenant mediante `TenantFilter` y `TenantContext`.

### Gestión de Restaurante
- CRUD de usuarios, personas y roles por restaurante.
- Asignación de múltiples roles a un mismo usuario en distintos restaurantes.

### Menú
- Secciones, categorías, productos, opciones de producto y áreas de producción.
- Estados de producto: `AVAILABLE`, `OUT_OF_STOCK`, `INACTIVE`.
- Control de stock, impuestos y costos unitarios.

### Mesas
- CRUD de mesas con número, capacidad y estado.
- Transiciones de estado: `AVAILABLE`, `OCCUPIED`, `CLEANING`, `RESERVED`.

### Órdenes
- Creación de órdenes para consumo en mesa, para llevar o delivery.
- Numeración secuencial de órdenes por restaurante.
- Detalles de orden con productos, opciones, cantidades y precios.
- Derivación automática del estado de la orden a partir de los detalles.
- Marcado de orden como pagada cuando todas sus facturas están pagadas.

### Facturación y Pagos
- Facturas con folio secuencial por restaurante (con bloqueo pesimista).
- Pagos idempotentes mediante `reference_id`.
- Cálculo de subtotal, impuesto y total.

### Caja
- Apertura y cierre de cajas registradoras.
- Control de una sola caja abierta por restaurante.
- Reportes X y Z.

### Reportes
- Dashboard con métricas clave.
- Reportes de ventas, productos, flujo de caja, afluencia de clientes y planificación de personal.

### Proveedores
- CRUD de proveedores con contactos por restaurante.

---

## Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| Framework | Spring Boot 3.3.0 |
| Lenguaje | Java 21 |
| Build | Maven 3.9+ |
| Base de datos | MySQL 8 |
| Migraciones | Flyway |
| Tests unitarios | JUnit 5, Mockito, AssertJ |
| Tests de integración | Spring Boot Test, MockMvc, H2, Testcontainers |
| Seguridad | Spring Security, JWT (jjwt) |
| Mapeo DTO | MapStruct |
| Documentación API | OpenAPI / SpringDoc |
| WebSocket | STOMP sobre SockJS |
| Cobertura | JaCoCo |
| Formato | Spotless (google-java-format) |

---

## Arquitectura

El proyecto sigue una arquitectura en capas con paquetes organizados por **dominio funcional**:

```
backend/src/main/java/com/restaurant/app/
├── auth/          # Autenticación, JWT, usuarios, roles
├── billing/       # Facturación, folios, pagos
├── cash/          # Cajas registradoras, transacciones
├── common/        # Excepciones, utilidades, handler global
├── config/        # Configuración de seguridad, CORS, JPA, OpenAPI, WebSocket
├── menu/          # Secciones, categorías, productos, opciones, áreas
├── order/         # Órdenes, detalles, estados
├── report/        # Reportes y vistas
├── security/      # Filtros, contexto de tenant, adaptadores de seguridad
├── supplier/      # Proveedores
├── table/         # Mesas
└── user/          # Usuarios del sistema y personas
```

Cada dominio contiene típicamente:
- `controller/` — Endpoints REST.
- `service/` — Lógica de negocio.
- `repository/` — Acceso a datos con Spring Data JPA.
- `model/` — Entidades JPA.
- `dto/` — Objetos de transferencia de datos.
- `mapper/` — MapStruct mappers.

### Patrones aplicados
- **Controller → Service → Repository**: separación clara de responsabilidades.
- **TenantAware**: entidades base que incluyen `restaurant_id` para aislamiento multi-tenant.
- **Auditable**: campos `created_at` y `updated_at` gestionados automáticamente.
- **RFC 7807 ProblemDetail**: respuestas de error estandarizadas.

---

## Requisitos Previos

Para ejecutar el proyecto necesitás:

- **Java 21** instalado.
- **Maven 3.9+** o usar el wrapper `./mvnw`.
- **MySQL 8** para el perfil de producción/desarrollo.
- **Docker** (opcional, para ejecutar tests con Testcontainers).

Verificá las versiones:

```bash
java -version
./mvnw -version
```

---

## Configuración del Entorno

### Variables de entorno

El archivo principal de configuración es `backend/src/main/resources/application.yml`. Para conectarse a MySQL, podés sobrescribir las variables:

```bash
export DB_URL=jdbc:mysql://localhost:3306/tacosoft
export DB_USER=tacosoft_user
export DB_PASS=tacosoft_pass
```

### Perfiles

- `default` / `prod`: ejecuta con MySQL.
- `test`: ejecuta tests con base de datos H2 en memoria y aplica todas las migraciones Flyway.

---

## Cómo Ejecutar

### Compilar

```bash
cd backend
./mvnw clean compile
```

### Ejecutar tests

```bash
./mvnw test
```

### Ejecutar tests de integración

```bash
./mvnw verify -DskipITs=false
```

### Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080` por defecto.

---

## Base de Datos y Migraciones

Las migraciones de Flyway se encuentran en:

```
backend/src/main/resources/db/migration/
```

Actualmente hay **17 migraciones** que crean el esquema completo:

1. `V1__init_tenancy_users.sql` — Usuarios, roles, restaurantes.
2. `V2__init_menu.sql` — Menú inicial.
3. `V3__init_tables_clients.sql` — Mesas y clientes.
4. `V4__init_orders.sql` — Órdenes y detalles.
5. `V5__init_billing.sql` — Facturación.
6. `V6__init_cash_registers.sql` — Cajas y transacciones.
7. `V7__init_suppliers.sql` — Proveedores.
8. `V8__init_reports_views.sql` — Vistas de reportes (compatible con H2 y MySQL).
9. `V9__add_category_description.sql`
10. `V10__add_order_detail_notes.sql`
11. `V11__add_missing_restaurant_ids.sql`
12. `V12__add_product_option_description.sql`
13. `V13__rename_product_option_price.sql`
14. `V14__add_restaurant_table_num.sql`
15. `V15__add_section_description.sql`
16. `V16__add_app_user_restaurant_id.sql`
17. `V17__align_product_schema.sql`

Al iniciar la aplicación, Flyway valida y aplica automáticamente las migraciones pendientes.

---

## Seguridad

### Autenticación JWT

El endpoint `POST /auth/login` devuelve un token JWT con los claims:

- `sub`: username
- `role`: rol primario
- `restaurantRoles`: roles por restaurante
- `iat`, `exp`: timestamps de emisión y expiración

Los endpoints protegidos requieren el header:

```
Authorization: Bearer <token>
```

### Multi-tenant

Además del token, la mayoría de los endpoints requieren:

```
x-restaurant-id: <restaurant_id>
```

Si falta este header en rutas no públicas, el sistema responde con `400 Bad Request`.

### RBAC

Los controladores usan anotaciones como:

```java
@PreAuthorize("@tenantSecurityExpression.hasAnyRole('ADMIN', 'WAITER')")
```

Algunos endpoints públicos (como login) no requieren autorización.

---

## Testing

El proyecto cuenta con una suite de tests extensa:

| Tipo | Cantidad | Detalle |
|------|----------|---------|
| Unitarios | 265 | Servicios, mappers, DTOs, utilidades |
| Integración / Invariantes | 48 | Controladores, repositorios, invariantes de negocio |
| E2E | 0 | No configurados |

### Comandos útiles

```bash
# Tests unitarios
./mvnw test

# Tests unitarios + integración
./mvnw verify -DskipITs=false

# Solo tests de integración
./mvnw failsafe:integration-test

# Con Spotless
./mvnw verify spotless:check
```

### Tests con Testcontainers

Algunos tests de invariantes financieras (`InvoiceFinancialInvariantTest`) requieren Docker para levantar MySQL. Si Docker no está disponible, esos tests se saltan automáticamente gracias a la condición `@EnabledIfDockerAvailable`.

---

## Cobertura de Código

La cobertura se mide con **JaCoCo**. El umbral mínimo configurado es del **80% de líneas cubiertas** a nivel de bundle.

```bash
./mvnw clean verify
```

El reporte se genera en:

```
backend/target/site/jacoco/index.html
```

---

## Formato de Código

El proyecto usa **Spotless** con `google-java-format`.

```bash
# Verificar formato
./mvnw spotless:check

# Aplicar formato automáticamente
./mvnw spotless:apply
```

---

## Documentación de API

Una vez ejecutada la aplicación, la documentación OpenAPI está disponible en:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## WebSocket

La aplicación expone un broker STOMP en `/ws` usando SockJS como fallback. Esto permite notificaciones en tiempo real sobre cambios de órdenes, detalles de orden y estados de mesa.

Configuración relevante en `WebSocketConfig.java`.

---

## Estructura del Proyecto

```
Tacosoft/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/restaurant/app/    # Código fuente
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/           # Migraciones Flyway
│       └── test/
│           ├── java/com/restaurant/app/    # Tests
│           └── resources/
│               └── application-test.yml    # Perfil de tests H2
├── openspec/
│   ├── changes/archive/                    # Cambios SDD archivados
│   ├── config.yaml                         # Configuración de OpenSpec
│   └── specs/                              # Especificaciones del sistema
└── README.md
```

---

## Flujo de Trabajo y Contribución

Este proyecto usa **GitHub Flow** con pull requests pequeñas y enfocadas:

1. Crear una rama desde `main`.
2. Implementar el cambio con tests.
3. Asegurar que `./mvnw clean verify spotless:check` pase.
4. Crear un PR descriptivo.
5. Mergear solo después de revisión y build exitosa.

Convención de commits: **Conventional Commits**.

Ejemplos:

```
feat(order): add sequential order numbering
fix(auth): correct JWT claim validation
test(billing): add folio concurrency test
docs(readme): update setup instructions
```

---

## Decisiones Técnicas Destacadas

1. **H2 para tests, MySQL para producción**: los tests de integración corren en H2 con modo MySQL, permitiendo ejecutar la CI sin Docker salvo para tests específicos de Testcontainers.
2. **Flyway para migraciones**: el esquema evoluciona mediante migraciones versionadas y se valida contra Hibernate `ddl-auto: validate`.
3. **MapStruct para DTOs**: separa explícitamente las entidades JPA de los contratos de API.
4. **Multi-tenant por header**: simple, stateless y compatible con JWT.
5. **Tests como documentación viva**: cada requisito del spec tiene al menos un test asociado.
6. **SDD (Spec-Driven Development)**: los cambios importantes se planifican primero en `openspec/` antes de implementarse.

---

## Limitaciones Conocidas

- El test `InvoiceFinancialInvariantTest` requiere Docker para validar concurrencia de folios; sin Docker se salta.
- Algunas transiciones de estado de mesa (`OCCUPIED → CLEANING`) y validaciones secundarias pueden requerir refinamiento futuro.
- El cálculo de impuestos en facturas usa una tasa fija del 16%; no soporta múltiples tasas por producto en esta versión.
- Los mensajes WebSocket envían DTOs cruzados; un wrapper tipado con nombre de evento es trabajo futuro.

---

## Licencia

[Especificar licencia aquí]

---

## Contacto

Proyecto: [github.com/evertdaniel/Tacosoft](https://github.com/evertdaniel/Tacosoft)
