# Spec-Driven Development — Sistema de Restaurante (Full‑Stack)

> **Backend:** Spring Boot 3 (Java 21) · **Persistencia:** MySQL 8 · **Frontend:** React 18 + MUI v5 + Redux Toolkit
>
> **Estado:** Draft v1 · **Última actualización:** 2025-06-21 · **Metodología:** Spec-Driven Development (SDD)

---

## Tabla de Contenido

1. [Visión General](#1-visión-general)
2. [Glosario y Dominio](#2-glosario-y-dominio)
3. [Arquitectura del Sistema](#3-arquitectura-del-sistema)
4. [Architecture Decision Records (ADR)](#4-architecture-decision-records-adr)
5. [Especificaciones Funcionales (Specs)](#5-especificaciones-funcionales-specs)
6. [Modelo de Datos — MySQL](#6-modelo-de-datos--mysql)
7. [Contratos de API (REST)](#7-contratos-de-api-rest)
8. [Eventos WebSocket](#8-eventos-websocket)
9. [Seguridad y RBAC](#9-seguridad-y-rbac)
10. [Backend — Spring Boot 3](#10-backend--spring-boot-3)
11. [Frontend — React 18 + MUI](#11-frontend--react-18--mui)
12. [Estrategia de Testing](#12-estrategia-de-testing)
13. [Migraciones y Versionado de Esquema](#13-migraciones-y-versionado-de-esquema)
14. [Despliegue y Operación](#14-despliegue-y-operación)
15. [Convenciones de Desarrollo (SDD)](#15-convenciones-de-desarrollo-sdd)
16. [Trazabilidad Spec → Implementación](#16-trazabilidad-spec--implementación)

---

## 1. Visión General

### 1.1 Propósito

El **Sistema de Restaurante** es una solución de gestión de pedidos que cubre el ciclo completo de operación de un restaurante: toma de pedidos, cocina en tiempo real, mesas, facturación, caja, clientes, proveedores y reportes.

Este documento es la **fuente de verdad** (single source of truth) bajo SDD: ninguna línea de código de producción se escribe sin una spec correspondiente, y toda spec que toca dinero o estado financiero requiere **juicio doble** (doble revisión adversarial).

### 1.2 Alcance

| Capa | Tecnología | Estado |
|------|------------|--------|
| Frontend (SPA) | React 18.2 + MUI v5 + Redux Toolkit + Zustand + TanStack Query | Existente — se mantiene |
| Backend (API) | Spring Boot 3.x + Java 21 | A construir según esta spec |
| Persistencia | MySQL 8 + Flyway | A construir según esta spec |
| Tiempo real | Socket.IO ⇄ Spring (STOMP/WebSocket) | A construir |

### 1.3 Principios Rectores

1. **Spec primero, código después.** El código deriva de una spec aprobada.
2. **Contratos explícitos.** El API es un contrato versionado; el frontend y backend lo respetan.
3. **Multi-tenancy estricto.** Toda entidad de negocio pertenece a un `restaurant_id`.
4. **Juicio doble para dinero.** Bills, Invoices, CashRegister, Transactions exigen doble revisión.
5. **Invariantes documentadas.** Cada invariante del dominio se nombra, se versiona y se prueba.

---

## 2. Glosario y Dominio

| Término | Definición |
|---------|------------|
| **Restaurant** | Unidad de tenancy. Todo recurso de negocio le pertenece. |
| **Order (Pedido)** | Comanda de un cliente; agrupa `OrderDetail`. Puede asociarse a una mesa y/o cliente. |
| **OrderDetail** | Línea de pedido: producto, cantidad, opción, estado de cocina. |
| **Bill (Cuenta)** | Importe a cobrar dentro de un pedido; soporta cobros parciales. |
| **Invoice (Factura)** | Documento fiscal/de cobro generado a partir de un pedido. |
| **CashRegister (Caja)** | Sesión de caja con apertura/cierre y arqueo (X/Z). |
| **Transaction** | Movimiento financiero (ingreso/gasto) asociado a una caja. |
| **ProductionArea** | Zona de preparación (cocina, bar) a la que se enruta un `OrderDetail`. |
| **Folio** | Secuencia numérica de comprobantes. **Invariante: global por restaurante.** |
| **Juicio doble** | Revisión adversarial obligatoria para PRs que tocan dinero. |

### 2.1 Roles

`ADMIN`, `COOK` (cocinero), `WAITER` (mesero), `CASHIER` (cajero).

---

## 3. Arquitectura del Sistema

### 3.1 Vista de Componentes

```
┌──────────────────────────────────────────────────────────────┐
│                        CLIENTE (Navegador)                     │
│   React 18 SPA · MUI v5 · Redux Toolkit · Zustand · TanStack   │
│        Axios (REST)            Socket.IO (tiempo real)         │
└───────────────┬────────────────────────────┬─────────────────┘
                │ HTTPS / JWT                 │ WSS
                ▼                             ▼
┌──────────────────────────────────────────────────────────────┐
│                  BACKEND — Spring Boot 3 (Java 21)            │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐   │
│  │ Controllers  │  │  WebSocket   │  │  Security (JWT)    │   │
│  │  (REST API)  │  │ (STOMP/SockJS│  │  + RBAC + Tenant   │   │
│  └──────┬───────┘  └──────┬───────┘  └─────────┬──────────┘   │
│         ▼                 ▼                     ▼              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                   Service Layer (@Transactional)        │  │
│  │   OrderService · BillService · CashRegisterService ...  │  │
│  └──────────────────────────┬─────────────────────────────┘  │
│                             ▼                                 │
│  ┌────────────────────────────────────────────────────────┐  │
│  │      Repository Layer — Spring Data JPA (Hibernate)     │  │
│  └──────────────────────────┬─────────────────────────────┘  │
└─────────────────────────────┼────────────────────────────────┘
                              ▼
                  ┌────────────────────────┐
                  │      MySQL 8           │
                  │  (Flyway migrations)   │
                  └────────────────────────┘
```

### 3.2 Estilo Arquitectónico

- **Backend:** arquitectura por capas (Controller → Service → Repository) con organización **feature-based** por paquete de dominio. Servicios transaccionales; DTOs en los bordes; entidades JPA nunca salen del backend.
- **Frontend:** SPA feature-based con estado híbrido (Redux global, Zustand local de módulo, TanStack Query para servidor/caché). Se mantiene **sin cambios** respecto al sistema actual.
- **Comunicación:** REST sincrónica + WebSocket para eventos de pedidos/mesas.

### 3.3 Flujo de Lectura/Escritura

```
Lectura :  Componente → useQuery → Service(axios) → REST → Controller → Service → Repository → MySQL
                ▲                                                                         │
                └──────────────── TanStack Query Cache ◄──────────────────────────────────┘

Escritura: Componente → useMutation → Service → REST → Controller → Service(@Transactional) → MySQL
                                                          │
                                                          └─► WebSocket broadcast → clientes
```

---

## 4. Architecture Decision Records (ADR)

### ADR-001 — Backend en Spring Boot 3 + Java 21

- **Contexto:** El frontend ya consume una API REST externa no documentada. Se requiere un backend formal.
- **Decisión:** Spring Boot 3.x sobre Java 21 (LTS), Spring Data JPA + Hibernate, Spring Security con JWT.
- **Consecuencias:** Ecosistema maduro, soporte transaccional robusto para dinero, curva de adopción conocida. Requiere JVM en producción.
- **Estado:** Aceptado.

### ADR-002 — MySQL 8 como motor de persistencia

- **Contexto:** Necesidad de almacenamiento relacional con integridad referencial para datos financieros.
- **Decisión:** MySQL 8 (InnoDB, `utf8mb4`), migraciones gestionadas por **Flyway** (numeradas, aplicadas en orden).
- **Consecuencias:** Transacciones ACID, FK estrictas. Se exige `utf8mb4_0900_ai_ci` para soporte completo de Unicode.
- **Estado:** Aceptado.

### ADR-003 — Folio global por restaurante

- **Contexto:** Los comprobantes necesitan numeración única y auditable.
- **Decisión:** La secuencia de folio es **global por `restaurant_id`**, no por usuario ni por caja. Implementada con tabla `folio_sequence` y bloqueo pesimista (`SELECT ... FOR UPDATE`) dentro de la transacción de creación del comprobante.
- **Consecuencias:** Numeración contigua y auditable. Punto de contención bajo alta concurrencia → mitigado con transacción corta.
- **Estado:** Aceptado. *Override requiere aprobación explícita.*

### ADR-004 — Multi-tenancy por columna `restaurant_id` + header

- **Contexto:** Un usuario puede pertenecer a varios restaurantes (`restaurantRoles`).
- **Decisión:** Discriminador por columna `restaurant_id` en cada tabla de negocio. El cliente envía `x-restaurant-id`; un filtro de seguridad valida que el usuario tenga rol en ese restaurante e inyecta el tenant en el contexto de la petición.
- **Consecuencias:** Aislamiento a nivel de query (todas las queries filtran por tenant). Riesgo: olvidar el filtro → fuga de datos. Mitigado con specs de repositorio que exigen el parámetro tenant.
- **Estado:** Aceptado.

### ADR-005 — Estado financiero exige juicio doble

- **Contexto:** Bills, Invoices, CashRegister y Transactions afectan dinero real.
- **Decisión:** Todo PR que toque estos paquetes requiere doble revisión adversarial antes de merge.
- **Consecuencias:** Mayor latencia de entrega en dinero; menor riesgo de error financiero. No negociable.
- **Estado:** Aceptado.

---

## 5. Especificaciones Funcionales (Specs)

Formato de cada spec: **ID · Título · Actor · Precondiciones · Flujo · Postcondiciones · Invariantes · Criterios de aceptación.**

### SPEC-AUTH-001 — Inicio de sesión

- **Actor:** Cualquier usuario.
- **Precondiciones:** Usuario activo existente.
- **Flujo:**
  1. `POST /auth/login` con `{ username, password }`.
  2. Backend valida credenciales (BCrypt).
  3. Emite JWT firmado con claims `sub`, `role`, `restaurantRoles`, `exp`.
  4. Responde `{ token, user, currentRestaurant }`.
- **Postcondiciones:** Cliente almacena token; Redux pasa a `authenticated`.
- **Invariantes:** Un token expirado nunca autoriza. Credenciales inválidas → 401 sin filtrar si el usuario existe.
- **Criterios de aceptación:**
  - [ ] Credenciales válidas → 200 + token válido.
  - [ ] Credenciales inválidas → 401.
  - [ ] Usuario inactivo → 401.
  - [ ] Sin restaurante asignado → `currentRestaurant: null`.

### SPEC-ORDER-001 — Crear pedido

- **Actor:** `WAITER`, `ADMIN`.
- **Precondiciones:** Sesión válida; tenant válido; productos existentes y activos.
- **Flujo:**
  1. `POST /orders` con `CreateOrderDto`.
  2. Backend valida mesa (si `IN_PLACE`), cliente (opcional), y cada detalle.
  3. Crea `Order` con `num` correlativo por restaurante, estado `PENDING`.
  4. Persiste detalles, enruta cada uno a su `ProductionArea`.
  5. Emite evento `order:created` por WebSocket.
- **Postcondiciones:** Pedido persistido; cocina notificada; mesa pasa a `OCCUPIED` si aplica.
- **Invariantes:**
  - `total = Σ(detail.amount)` siempre consistente.
  - Un pedido `IN_PLACE` con mesa ocupa exactamente una mesa.
- **Criterios de aceptación:**
  - [ ] Pedido válido → 201 + `Order` completo.
  - [ ] Producto inactivo en detalle → 400.
  - [ ] Mesa inexistente con tipo `IN_PLACE` → 400.
  - [ ] Evento `order:created` recibido por los clientes del mismo restaurante.

### SPEC-ORDER-002 — Cambiar estado de detalle (cocina)

- **Actor:** `COOK`, `ADMIN`.
- **Flujo:** `PUT /orders/details/:id/status` con `{ status }`. Transiciones válidas: `PENDING → IN_PROGRESS → READY → DELIVERED`; `CANCELLED` desde cualquiera salvo `DELIVERED`.
- **Invariantes:** No se permite retroceder de `DELIVERED`. El estado del `Order` se recalcula a partir de sus detalles.
- **Criterios de aceptación:**
  - [ ] Transición válida → 200 + evento `order-detail:updated`.
  - [ ] Transición inválida → 409.

### SPEC-BILL-001 — Crear y cobrar cuenta *(💰 juicio doble)*

- **Actor:** `CASHIER`, `ADMIN`.
- **Precondiciones:** Pedido abierto; caja del usuario abierta.
- **Flujo:**
  1. `POST /invoices` o creación de `Bill` desde el pedido.
  2. Se asigna **folio global** del restaurante (ADR-003) bajo transacción.
  3. `POST /invoices/:id/pay` registra el cobro con `paymentMethod`.
  4. Se crea `Transaction` de ingreso ligada a la caja activa.
- **Postcondiciones:** `Bill.isPaid = true` cuando el cobro cubre el importe; pedido se cierra si todas las cuentas están pagadas.
- **Invariantes:**
  - `Σ(bills.amount) ≤ order.total`.
  - No se cobra una cuenta dos veces (idempotencia por `bill_id`).
  - Folio nunca se reutiliza ni se salta sin registro.
- **Criterios de aceptación:**
  - [ ] Cobro completo → `isPaid=true` + transacción de ingreso.
  - [ ] Cobro parcial → `PARTIAL_PAY`.
  - [ ] Reintento del mismo pago → no duplica transacción.
  - [ ] Sin caja abierta → 409.

### SPEC-CASH-001 — Apertura y cierre de caja (X/Z) *(💰 juicio doble)*

- **Actor:** `CASHIER`, `ADMIN`.
- **Flujo:**
  1. `POST /cash-registers` abre caja con monto inicial.
  2. Durante el turno se acumulan `Transaction`.
  3. `GET` reporte **X** (corte parcial, no cierra).
  4. `PUT /cash-registers/:id/close` genera reporte **Z** y cierra.
- **Invariantes (a confirmar por negocio):**
  - **Caja abierta:** por defecto **una caja abierta por restaurante**. *Si el modelo evoluciona a "una caja abierta por `user_id`", debe versionarse esta invariante explícitamente (ver §15.4).*
  - `saldo_final = saldo_inicial + Σ(ingresos) − Σ(gastos)`.
- **Criterios de aceptación:**
  - [ ] Abrir con caja ya abierta (según invariante vigente) → 409.
  - [ ] Reporte Z cuadra contra transacciones.
  - [ ] Caja cerrada no acepta nuevas transacciones.

### SPEC-MENU-001 — Gestión de menú (secciones/categorías/productos)

- **Actor:** `ADMIN`.
- **Flujo:** CRUD sobre `/sections`, `/categories`, `/products`; importación por Excel vía `POST /products/excel`.
- **Invariantes:** Un producto pertenece a exactamente una categoría; una categoría a una sección. Borrado lógico (`isActive=false`) preferido sobre borrado físico cuando hay pedidos históricos.

### SPEC-TABLE-001 — Gestión de mesas

- **Actor:** `ADMIN`, `WAITER`.
- **Flujo:** CRUD `/tables`; `PUT /tables/:id/status`. Mapa arrastrable persiste `x`, `y`.
- **Invariantes:** Una mesa `OCCUPIED` tiene a lo sumo un pedido activo asociado.

### SPEC-REPORT-001 — Dashboard y reportes

- **Actor:** `ADMIN`.
- **Flujo:** `GET /reports/{dashboard,sales,products,finances,footfall,staff-planning}` con filtros de fecha. Generación de PDF en frontend.
- **Invariantes:** Los importes de reportes financieros provienen exclusivamente de `Transaction` e `Invoice` confirmados.

---

## 6. Modelo de Datos — MySQL

### 6.1 Convenciones

- Motor **InnoDB**, charset `utf8mb4`, collation `utf8mb4_0900_ai_ci`.
- PK `BINARY(16)` para UUID (o `CHAR(36)` si se prioriza legibilidad). Aquí se usa `CHAR(36)`.
- Toda tabla de negocio incluye `restaurant_id` + FK e índice.
- Timestamps `created_at`, `updated_at` (`TIMESTAMP`, default `CURRENT_TIMESTAMP`).
- Importes monetarios en `DECIMAL(12,2)` — **nunca** `FLOAT/DOUBLE`.

### 6.2 Diagrama de Relaciones

```
restaurant 1───* user_restaurant_role *───1 user 1───1 person
restaurant 1───* table
restaurant 1───* section 1───* category 1───* product 1───* product_option
restaurant 1───* order 1───* order_detail *───1 product
order 1───* bill
order 1───* invoice 1───* invoice_detail
order *───1 table (opcional)
order *───1 client *───1 person
restaurant 1───* production_area
restaurant 1───* cash_register 1───* transaction
restaurant 1───1 folio_sequence
```

### 6.3 DDL Principal (extracto)

```sql
-- ============ TENANCY & USUARIOS ============
CREATE TABLE restaurant (
  id          CHAR(36)      NOT NULL,
  name        VARCHAR(150)  NOT NULL,
  address     VARCHAR(255),
  phone       VARCHAR(40),
  email       VARCHAR(120),
  tax_id      VARCHAR(40),
  currency    VARCHAR(8)    NOT NULL DEFAULT 'MXN',
  logo        VARCHAR(255),
  is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE person (
  id          CHAR(36)      NOT NULL,
  first_name  VARCHAR(80)   NOT NULL,
  last_name   VARCHAR(80)   NOT NULL,
  email       VARCHAR(120),
  phone       VARCHAR(40),
  address     VARCHAR(255),
  document_id VARCHAR(40),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE app_user (
  id          CHAR(36)      NOT NULL,
  username    VARCHAR(60)   NOT NULL,
  password    VARCHAR(100)  NOT NULL,   -- BCrypt
  person_id   CHAR(36)      NOT NULL,
  is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
  online      BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES person(id)
) ENGINE=InnoDB;

CREATE TABLE role (
  id          INT           NOT NULL AUTO_INCREMENT,
  name        VARCHAR(20)   NOT NULL,   -- ADMIN, COOK, WAITER, CASHIER
  description VARCHAR(120),
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_name (name)
) ENGINE=InnoDB;

CREATE TABLE user_restaurant_role (
  id            CHAR(36)    NOT NULL,
  user_id       CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)    NOT NULL,
  role_id       INT         NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_urr (user_id, restaurant_id, role_id),
  CONSTRAINT fk_urr_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_urr_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_urr_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB;

-- ============ MENÚ ============
CREATE TABLE section (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)    NOT NULL,
  name          VARCHAR(120) NOT NULL,
  display_order INT         NOT NULL DEFAULT 0,
  is_public     BOOLEAN     NOT NULL DEFAULT TRUE,
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  KEY idx_section_rest (restaurant_id),
  CONSTRAINT fk_section_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB;

CREATE TABLE category (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)    NOT NULL,
  section_id    CHAR(36)    NOT NULL,
  name          VARCHAR(120) NOT NULL,
  is_public     BOOLEAN     NOT NULL DEFAULT TRUE,
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  KEY idx_category_section (section_id),
  CONSTRAINT fk_category_section FOREIGN KEY (section_id) REFERENCES section(id)
) ENGINE=InnoDB;

CREATE TABLE production_area (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)    NOT NULL,
  name          VARCHAR(120) NOT NULL,
  description   VARCHAR(255),
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  CONSTRAINT fk_parea_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB;

CREATE TABLE product (
  id                CHAR(36)      NOT NULL,
  restaurant_id     CHAR(36)      NOT NULL,
  category_id       CHAR(36)      NOT NULL,
  production_area_id CHAR(36),
  name              VARCHAR(150)  NOT NULL,
  description       VARCHAR(500),
  price             DECIMAL(12,2) NOT NULL,
  unit_cost         DECIMAL(12,2) DEFAULT 0,
  iva               DECIMAL(5,2)  DEFAULT 0,
  quantity          INT           DEFAULT 0,
  status            VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE|OUT_OF_STOCK|OUT_OF_SEASON
  is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
  is_public         BOOLEAN       NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  KEY idx_product_category (category_id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_product_parea   FOREIGN KEY (production_area_id) REFERENCES production_area(id)
) ENGINE=InnoDB;

CREATE TABLE product_option (
  id           CHAR(36)      NOT NULL,
  product_id   CHAR(36)      NOT NULL,
  name         VARCHAR(120)  NOT NULL,
  price        DECIMAL(12,2) NOT NULL,
  cost         DECIMAL(12,2) DEFAULT 0,
  quantity     INT           DEFAULT 0,
  manage_stock BOOLEAN       NOT NULL DEFAULT FALSE,
  is_default   BOOLEAN       NOT NULL DEFAULT FALSE,
  is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
  is_available BOOLEAN       NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  CONSTRAINT fk_option_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

-- ============ MESAS ============
CREATE TABLE restaurant_table (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)    NOT NULL,
  name          VARCHAR(60) NOT NULL,
  seats         INT         NOT NULL DEFAULT 4,
  pos_x         INT         NOT NULL DEFAULT 0,
  pos_y         INT         NOT NULL DEFAULT 0,
  status        VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE|OCCUPIED|RESERVED|CLEANING
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  KEY idx_table_rest (restaurant_id),
  CONSTRAINT fk_table_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB;

-- ============ CLIENTES ============
CREATE TABLE client (
  id            CHAR(36)      NOT NULL,
  restaurant_id CHAR(36)      NOT NULL,
  person_id     CHAR(36)      NOT NULL,
  type          VARCHAR(30)   NOT NULL DEFAULT 'REGULAR',
  credit_limit  DECIMAL(12,2) NOT NULL DEFAULT 0,
  is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_client_person FOREIGN KEY (person_id) REFERENCES person(id),
  CONSTRAINT fk_client_rest   FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB;

-- ============ PEDIDOS ============
CREATE TABLE `order` (
  id            CHAR(36)      NOT NULL,
  restaurant_id CHAR(36)      NOT NULL,
  num           INT           NOT NULL,           -- correlativo por restaurante
  type          VARCHAR(20)   NOT NULL,           -- IN_PLACE | TAKE_AWAY
  status        VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  status_pay    VARCHAR(20)   NOT NULL DEFAULT 'NO_PAY',
  people        INT           NOT NULL DEFAULT 1,
  total         DECIMAL(12,2) NOT NULL DEFAULT 0,
  notes         VARCHAR(500),
  table_id      CHAR(36),
  client_id     CHAR(36),
  user_id       CHAR(36)      NOT NULL,           -- creador
  is_paid       BOOLEAN       NOT NULL DEFAULT FALSE,
  is_closed     BOOLEAN       NOT NULL DEFAULT FALSE,
  delivery_time TIMESTAMP     NULL,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_num (restaurant_id, num),
  KEY idx_order_rest_status (restaurant_id, status),
  CONSTRAINT fk_order_rest   FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_order_table  FOREIGN KEY (table_id) REFERENCES restaurant_table(id),
  CONSTRAINT fk_order_client FOREIGN KEY (client_id) REFERENCES client(id),
  CONSTRAINT fk_order_user   FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;

CREATE TABLE order_detail (
  id              CHAR(36)      NOT NULL,
  order_id        CHAR(36)      NOT NULL,
  product_id      CHAR(36)      NOT NULL,
  product_option_id CHAR(36),
  quantity        INT           NOT NULL,
  qty_delivered   INT           NOT NULL DEFAULT 0,
  ready_quantity  INT           NOT NULL DEFAULT 0,
  qty_paid        INT           NOT NULL DEFAULT 0,
  price           DECIMAL(12,2) NOT NULL,
  amount          DECIMAL(12,2) NOT NULL,
  description     VARCHAR(500),
  status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  type_order_detail VARCHAR(20) NOT NULL,
  is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
  created_by      CHAR(36),
  updated_by      CHAR(36),
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_detail_order (order_id),
  CONSTRAINT fk_detail_order   FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
  CONSTRAINT fk_detail_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

-- ============ FINANZAS (💰 juicio doble) ============
CREATE TABLE bill (
  id             CHAR(36)      NOT NULL,
  order_id       CHAR(36)      NOT NULL,
  amount         DECIMAL(12,2) NOT NULL,
  is_paid        BOOLEAN       NOT NULL DEFAULT FALSE,
  payment_method VARCHAR(20),   -- CASH | CREDIT_CARD | TRANSFER
  created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_bill_order FOREIGN KEY (order_id) REFERENCES `order`(id)
) ENGINE=InnoDB;

CREATE TABLE invoice (
  id             CHAR(36)      NOT NULL,
  restaurant_id  CHAR(36)      NOT NULL,
  order_id       CHAR(36)      NOT NULL,
  folio          BIGINT        NOT NULL,
  subtotal       DECIMAL(12,2) NOT NULL,
  tax            DECIMAL(12,2) NOT NULL,
  total          DECIMAL(12,2) NOT NULL,
  is_paid        BOOLEAN       NOT NULL DEFAULT FALSE,
  payment_method VARCHAR(20),
  created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_folio (restaurant_id, folio),
  CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES `order`(id)
) ENGINE=InnoDB;

CREATE TABLE folio_sequence (
  restaurant_id CHAR(36) NOT NULL,
  next_folio    BIGINT   NOT NULL DEFAULT 1,
  PRIMARY KEY (restaurant_id),
  CONSTRAINT fk_folio_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB;

CREATE TABLE cash_register (
  id             CHAR(36)      NOT NULL,
  restaurant_id  CHAR(36)      NOT NULL,
  user_id        CHAR(36)      NOT NULL,   -- usuario que abre
  opening_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  closing_amount DECIMAL(12,2),
  status         VARCHAR(20)   NOT NULL DEFAULT 'OPEN', -- OPEN | CLOSED
  opened_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  closed_at      TIMESTAMP     NULL,
  PRIMARY KEY (id),
  KEY idx_cr_rest_status (restaurant_id, status),
  CONSTRAINT fk_cr_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;

CREATE TABLE `transaction` (
  id              CHAR(36)      NOT NULL,
  cash_register_id CHAR(36)     NOT NULL,
  type            VARCHAR(20)   NOT NULL,  -- INCOME | EXPENSE
  amount          DECIMAL(12,2) NOT NULL,
  description     VARCHAR(255),
  payment_method  VARCHAR(20),
  reference_id    CHAR(36),                -- bill/invoice asociado (idempotencia)
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tx_cr (cash_register_id),
  UNIQUE KEY uk_tx_reference (reference_id),  -- evita doble cobro
  CONSTRAINT fk_tx_cr FOREIGN KEY (cash_register_id) REFERENCES cash_register(id)
) ENGINE=InnoDB;
```

### 6.4 Invariantes de Integridad (a nivel de datos)

| ID | Invariante | Mecanismo |
|----|-----------|-----------|
| INV-01 | `order.num` único por restaurante | `UNIQUE (restaurant_id, num)` |
| INV-02 | Folio único por restaurante | `UNIQUE (restaurant_id, folio)` + `folio_sequence` con lock |
| INV-03 | No doble cobro | `UNIQUE (reference_id)` en `transaction` |
| INV-04 | `order.total = Σ(order_detail.amount)` | Recalculado en `OrderService` dentro de la transacción |
| INV-05 | Caja cerrada no admite transacciones | Validación en `CashRegisterService` |
| INV-06 | Toda query de negocio filtra por tenant | Specs de repositorio + filtro de seguridad |

---

## 7. Contratos de API (REST)

### 7.1 Convenciones

| Atributo | Valor |
|----------|-------|
| Base URL | `VITE_API_URL` |
| Auth | `Authorization: Bearer <JWT>` |
| Tenant | `x-restaurant-id: <uuid>` |
| Formato | JSON (`Content-Type: application/json`) |
| Errores | RFC 7807 (`application/problem+json`) recomendado |

### 7.2 Códigos de Estado

| Código | Uso |
|--------|-----|
| 200 / 201 | Éxito / creado |
| 400 | Validación |
| 401 | No autenticado |
| 403 | Sin permiso (rol) |
| 404 | No encontrado |
| 409 | Conflicto de invariante (transición/estado) |
| 422 | Entidad procesable pero inválida (reglas de negocio) |
| 500 | Error de servidor |

### 7.3 Endpoints (resumen por módulo)

**Auth** — `POST /auth/login` · `POST /auth/register` · `GET /auth/renew` · `POST /auth/forgot-password` · `POST /auth/reset-password/:token`

**Users** — `GET|POST /users` · `GET|PUT|DELETE /users/:id` · `POST /users/invite` · `PUT /users/:id/role` · `PUT /users/:id/password`

**Clients** — `GET|POST /clients` · `GET|PUT|DELETE /clients/:id`

**Menu** — `GET /menu` · `GET /menu/:restaurantId` · `GET|POST /sections` · `PUT|DELETE /sections/:id` · `GET|POST /categories` · `PUT|DELETE /categories/:id` · `GET|POST /products` · `PUT|DELETE /products/:id` · `POST /products/excel`

**Orders** — `GET /orders` · `GET /orders/active` · `GET /orders/:id` · `POST /orders` · `PUT|DELETE /orders/:id` · `POST /orders/:id/details` · `PUT|DELETE /orders/details/:id` · `PUT /orders/details/:id/status`

**Invoices/Bills** *(💰)* — `GET|POST /invoices` · `GET|PUT|DELETE /invoices/:id` · `POST /invoices/:id/pay`

**Tables** — `GET|POST /tables` · `GET|PUT|DELETE /tables/:id` · `PUT /tables/:id/status`

**Cash/Finanzas** *(💰)* — `GET /cash-registers` · `GET /cash-registers/active` · `POST /cash-registers` · `PUT /cash-registers/:id/close` · `GET|POST /transactions` · `GET /accounts` · `GET /payment-methods`

**Reports** — `GET /reports/{dashboard,sales,products,finances,footfall,staff-planning}`

**Restaurant** — `GET|POST /restaurants` · `GET|PUT /restaurants/:id` · `GET|POST /production-areas`

**Suppliers** — `GET|POST /suppliers` · `GET|PUT|DELETE /suppliers/:id`

### 7.4 Ejemplos de Contrato

```jsonc
// POST /auth/login  → 200
// Request
{ "username": "mesero01", "password": "••••••" }
// Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { "id": "…", "username": "mesero01", "role": { "id": 3, "name": "WAITER" }, "restaurantRoles": [ … ] },
  "currentRestaurant": { "id": "…", "name": "El Buen Taco" }
}
```

```jsonc
// POST /orders  → 201
// Request (CreateOrderDto)
{
  "tableId": "…",
  "clientId": null,
  "type": "IN_PLACE",
  "people": 2,
  "notes": "Sin cebolla",
  "details": [
    { "productId": "…", "quantity": 3, "productOptionId": "…", "description": "Bien dorado" }
  ]
}
// Response: Order completo con id, num, status=PENDING, total calculado
```

```jsonc
// POST /invoices/:id/pay  → 200   (💰 idempotente por reference_id)
{ "paymentMethod": "CASH", "amount": 250.00 }
```

```jsonc
// Error 409 (RFC 7807)
{
  "type": "https://errors.restaurant.app/order-detail-invalid-transition",
  "title": "Transición de estado inválida",
  "status": 409,
  "detail": "No se puede pasar de DELIVERED a IN_PROGRESS",
  "instance": "/orders/details/abc-123/status"
}
```

---

## 8. Eventos WebSocket

Implementación backend: Spring WebSocket + STOMP sobre SockJS, o adaptador compatible Socket.IO. Canales por restaurante (`/topic/restaurant/{restaurantId}/orders`).

### 8.1 Cliente → Servidor

| Evento | Payload | Descripción |
|--------|---------|-------------|
| `order:create` | `{ orderId }` | Notificar nuevo pedido |
| `order:update` | `{ orderId }` | Notificar actualización |
| `order:delete` | `{ orderId }` | Notificar eliminación |
| `table:update` | `{ tableId }` | Cambio de mesa |

### 8.2 Servidor → Cliente

| Evento | Payload | Descripción |
|--------|---------|-------------|
| `order:created` | `Order` | Nuevo pedido |
| `order:updated` | `Order` | Pedido actualizado |
| `order:deleted` | `{ orderId }` | Pedido eliminado |
| `table:updated` | `Table` | Mesa actualizada |
| `order-detail:updated` | `OrderDetail` | Detalle actualizado |

**Invariante de difusión:** un evento solo llega a clientes autenticados del mismo `restaurant_id`.

---

## 9. Seguridad y RBAC

### 9.1 Autenticación

- JWT firmado (HS256/RS256). Claims: `sub` (userId), `role`, `restaurantRoles`, `exp`, `iat`.
- BCrypt para contraseñas (cost ≥ 10).
- Renovación vía `GET /auth/renew`.

### 9.2 Autorización (matriz de permisos)

| Recurso / Acción | ADMIN | COOK | WAITER | CASHIER |
|------------------|:-----:|:----:|:------:|:-------:|
| Crear pedido | ✅ | ❌ | ✅ | ❌ |
| Cambiar estado detalle (cocina) | ✅ | ✅ | ❌ | ❌ |
| Crear/cobrar bill *(💰)* | ✅ | ❌ | ❌ | ✅ |
| Abrir/cerrar caja *(💰)* | ✅ | ❌ | ❌ | ✅ |
| Gestionar menú | ✅ | ❌ | ❌ | ❌ |
| Gestionar usuarios | ✅ | ❌ | ❌ | ❌ |
| Ver reportes | ✅ | ❌ | ❌ | parcial |

> **Nota de evolución (mini‑franquicia):** si el negocio decide que cada `WAITER` opere como estación autónoma de cobro, esta matriz debe modificarse para otorgar a `WAITER` los permisos `bills:write` y `financial:close_register`. Ese cambio **rompe invariantes** y requiere ADR + juicio doble (ver §15.4).

### 9.3 Defensa multi-tenant

- Filtro `TenantFilter` valida `x-restaurant-id` contra `restaurantRoles` del token.
- Toda query de repositorio recibe `restaurant_id` como parámetro obligatorio.
- Rechazo `403` si el usuario no tiene rol en el tenant solicitado.

---

## 10. Backend — Spring Boot 3

### 10.1 Stack

| Componente | Tecnología |
|------------|-----------|
| Runtime | Java 21 (LTS) |
| Framework | Spring Boot 3.3.x |
| Web | Spring Web (MVC) |
| Persistencia | Spring Data JPA + Hibernate 6 |
| DB Driver | `mysql-connector-j` |
| Migraciones | Flyway |
| Seguridad | Spring Security + JJWT |
| WebSocket | Spring WebSocket (STOMP) |
| Validación | Jakarta Bean Validation |
| Mapeo DTO | MapStruct |
| Build | Maven o Gradle |
| Docs API | springdoc-openapi (Swagger UI) |
| Test | JUnit 5 + Mockito + Testcontainers (MySQL) |

### 10.2 Estructura de Paquetes (feature-based)

```
com.restaurant.app
├── config/              # Seguridad, CORS, WebSocket, OpenAPI
├── security/            # JwtService, TenantFilter, UserDetails
├── common/              # Errores, RFC7807 handler, base entities
├── auth/                # controller, service, dto
├── user/
├── client/
├── menu/                # section, category, product, productoption
├── order/               # order, orderdetail
├── table/
├── billing/   (💰)      # bill, invoice, folio
├── cash/      (💰)      # cashregister, transaction
├── report/
├── supplier/
└── restaurant/          # tenant config, production area
```

### 10.3 Reglas de capa

1. **Controller** solo orquesta y mapea DTOs; sin lógica de negocio.
2. **Service** contiene reglas, es `@Transactional`, valida invariantes.
3. **Repository** (Spring Data) solo acceso a datos; queries filtran por tenant.
4. **Entidad JPA** nunca se serializa al cliente; siempre se mapea a DTO (MapStruct).
5. **Money paths** (`billing`, `cash`) → juicio doble obligatorio.

### 10.4 `application.yml` (extracto)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/restaurant?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=UTC
    username: ${DB_USER:restaurant}
    password: ${DB_PASS:changeme}
  jpa:
    hibernate.ddl-auto: validate      # nunca 'update' en prod; el esquema lo gobierna Flyway
    properties.hibernate.dialect: org.hibernate.dialect.MySQLDialect
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-minutes: 120
  cors:
    allowed-origins: ${FRONTEND_URL:http://localhost:3030}
```

### 10.5 Patrón de servicio con folio (ADR-003, INV-02)

```java
@Transactional
public Invoice createInvoice(String restaurantId, CreateInvoiceCmd cmd) {
    // bloqueo pesimista de la secuencia para folio contiguo
    FolioSequence seq = folioRepo.lockByRestaurant(restaurantId); // SELECT ... FOR UPDATE
    long folio = seq.getNextFolio();
    seq.setNextFolio(folio + 1);

    Invoice invoice = invoiceMapper.from(cmd);
    invoice.setFolio(folio);
    invoice.setRestaurantId(restaurantId);
    return invoiceRepo.save(invoice);
}
```

### 10.6 Idempotencia de cobro (INV-03)

```java
@Transactional
public void payInvoice(String invoiceId, PaymentCmd cmd) {
    Invoice inv = invoiceRepo.findByIdAndTenant(invoiceId, tenant());
    // referencia única evita doble transacción
    if (txRepo.existsByReferenceId(inv.getId())) return; // idempotente
    CashRegister cr = cashRepo.findOpenByUser(currentUserId())
        .orElseThrow(() -> new ConflictException("Sin caja abierta"));
    txRepo.save(Transaction.income(cr, cmd.amount(), inv.getId(), cmd.paymentMethod()));
    inv.markPaid();
}
```

---

## 11. Frontend — React 18 + MUI

> **Sin cambios** respecto al sistema existente. Esta sección documenta cómo el frontend consume el nuevo backend.

### 11.1 Stack vigente

React 18.2 · TypeScript 5.9 · Vite 3.2 · MUI v5 · Redux Toolkit · Zustand · TanStack Query v5 · React Router 6 · Axios · Socket.IO Client · React Hook Form.

### 11.2 Estado híbrido

| Tecnología | Uso |
|------------|-----|
| Redux Toolkit | Auth, menu, orders, tables, users, clients (estado global sincrónico) |
| Zustand | Restaurant activo, caja, drawers (estado local de módulo) |
| TanStack Query | Datos de servidor + caché + invalidación |
| React Context | Socket, Order, Sidebar |

### 11.3 Capa de API (Axios)

```typescript
const restauranteApi = axios.create({ baseURL: import.meta.env.VITE_API_URL });

restauranteApi.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  const restaurantId = useRestaurantStore.getState().restaurant?.id;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  if (restaurantId) config.headers['x-restaurant-id'] = restaurantId;
  return config;
});
```

### 11.4 Contrato de errores

El frontend mapea los códigos del backend (§7.2): `401 → redirige a login`, `403 → página no autorizado`, `409 → toast de conflicto con `detail` de RFC 7807`, `400/422 → errores de formulario`.

### 11.5 Sincronización en tiempo real

`useOnWebSocketsEvent('order:created', (order) => { /* actualiza Redux/cache */ })`. El socket se conecta al autenticar y se cierra al salir, con headers `authentication` y `restaurantId`.

---

## 12. Estrategia de Testing

### 12.1 Backend

| Nivel | Herramienta | Cobertura objetivo |
|-------|-------------|--------------------|
| Unitario (Service) | JUnit 5 + Mockito | Reglas de negocio e invariantes |
| Integración (Repository/JPA) | Testcontainers + MySQL real | Queries, FK, locks de folio |
| API (Controller) | `@SpringBootTest` + MockMvc | Contratos REST y códigos de estado |
| Seguridad | Spring Security Test | RBAC y multi-tenancy |

### 12.2 Tests obligatorios de invariantes (💰)

- [ ] **INV-02 Folio:** 1000 facturas concurrentes → folios contiguos sin huecos ni duplicados.
- [ ] **INV-03 Doble cobro:** dos `pay` simultáneos sobre la misma invoice → una sola transacción.
- [ ] **INV-04 Total:** `order.total` siempre igual a la suma de detalles tras cada mutación.
- [ ] **INV-05 Caja cerrada:** transacción sobre caja cerrada → 409.
- [ ] **INV-06 Tenant:** usuario del restaurante A no puede leer/escribir datos del restaurante B → 403.

### 12.3 Reserva de bloques de fixtures

Para evitar colisiones de datos entre suites, cada suite reserva un bloque de IDs/folios de prueba (patrón heredado de `06-testing.md`): p. ej. suite `billing` usa folios `90000–90999`, suite `order` usa `num` `8000–8999`. Documentar el mapa de reservas en `testing-fixtures.md`.

### 12.4 Frontend

Vitest + Testing Library (no configurado actualmente; se recomienda añadir). Prioridad: hooks de mutación de dinero y flujos de cobro.

---

## 13. Migraciones y Versionado de Esquema

- **Herramienta:** Flyway. Migraciones **numeradas** y aplicadas en orden: `V1__init.sql`, `V2__add_folio_sequence.sql`, …
- `ddl-auto: validate` en producción — el esquema lo gobiernan exclusivamente las migraciones, nunca Hibernate.
- Toda migración que toque tablas de dinero (`bill`, `invoice`, `cash_register`, `transaction`, `folio_sequence`) requiere juicio doble.
- Las migraciones son **inmutables una vez mergeadas**; cambios = nueva migración.

```
src/main/resources/db/migration/
├── V1__init_tenancy_users.sql
├── V2__menu.sql
├── V3__tables_clients.sql
├── V4__orders.sql
├── V5__billing_finance.sql      # 💰 juicio doble
└── V6__folio_sequence.sql       # 💰 juicio doble
```

---

## 14. Despliegue y Operación

### 14.1 Entornos

| Entorno | Frontend | Backend | DB |
|---------|----------|---------|-----|
| Dev | Vite dev (`:3030`) | `localhost:8080` | MySQL local / Docker |
| Staging | Build estático (Netlify/Vercel) | JVM/containerizado | MySQL gestionado |
| Prod | CDN estático | Contenedor JVM + réplicas | MySQL gestionado + backups |

### 14.2 Variables de entorno

**Frontend:** `VITE_API_URL`, `VITE_WS_URL`.
**Backend:** `DB_HOST`, `DB_USER`, `DB_PASS`, `JWT_SECRET`, `FRONTEND_URL`.

### 14.3 Operación

- Backups diarios de MySQL; retención ≥ 30 días.
- Logs estructurados (JSON) en backend; nivel `INFO` en prod.
- Healthcheck `GET /actuator/health` (Spring Boot Actuator).
- Métricas vía Actuator/Micrometer.

---

## 15. Convenciones de Desarrollo (SDD)

### 15.1 Conventional Commits

```
feat(order): crear endpoint POST /orders
fix(billing): evitar doble cobro por reference_id
docs(sdd): añadir ADR-003 folio global
refactor(cash): extraer cálculo de arqueo Z
test(billing): invariante de folio concurrente
```

### 15.2 Flujo de Pull Request

1. Crear rama desde `main` (`feat/order-create`).
2. Implementar **contra una spec aprobada** (referenciar su ID en el PR).
3. Ejecutar build y tests.
4. Revisión: 1 revisor normal; **2 revisores adversariales** si toca dinero.
5. Merge solo con specs y tests verdes.

### 15.3 Definición de Hecho (DoD)

- [ ] Spec referenciada y satisfecha.
- [ ] Invariantes con test.
- [ ] Contrato de API documentado (OpenAPI actualizado).
- [ ] Migración numerada si cambió el esquema.
- [ ] Juicio doble aprobado si toca dinero.

### 15.4 Cambios que rompen invariantes

Cualquier cambio que altere una invariante documentada (p. ej. mini‑franquicia: caja por `user_id`, permisos de cobro para `WAITER`, o folio no global) **debe**:
1. Abrir un nuevo ADR que supersede al afectado.
2. Versionar explícitamente la invariante (`INV-0X v2`).
3. Pasar juicio doble.
4. Actualizar la matriz RBAC (§9.2) y las specs de caja (§5).

---

## 16. Trazabilidad Spec → Implementación

| Spec | Backend (paquete) | Frontend (módulo) | Tablas | Invariantes | 💰 |
|------|-------------------|-------------------|--------|-------------|:--:|
| SPEC-AUTH-001 | `auth` | `Public/Auth` | `app_user`, `role`, `user_restaurant_role` | — | |
| SPEC-ORDER-001 | `order` | `Private/Orders` | `order`, `order_detail` | INV-01, INV-04 | |
| SPEC-ORDER-002 | `order` | `Orders/ActiveOrders` | `order_detail` | INV-04 | |
| SPEC-BILL-001 | `billing` | `Orders/EditOrder`, `Bills` | `bill`, `invoice`, `folio_sequence`, `transaction` | INV-02, INV-03 | ✅ |
| SPEC-CASH-001 | `cash` | `Balance` | `cash_register`, `transaction` | INV-05 | ✅ |
| SPEC-MENU-001 | `menu` | `EditMenu` | `section`, `category`, `product`, `product_option` | — | |
| SPEC-TABLE-001 | `table` | `Tables` | `restaurant_table` | — | |
| SPEC-REPORT-001 | `report` | `Reports` | (lectura agregada) | — | |

---

### Apéndice A — Mapa de paridad con el sistema actual

El modelo de datos y los contratos de este documento derivan directamente de la documentación existente del frontend (`data-models.md`, `api-contracts.md`, `architecture.md`). Los enums (`OrderStatus`, `TypeOrder`, `PaymentMethod`, `TableStatus`, `ProductStatus`) y los DTOs (`CreateOrderDto`, `CreateProductDto`, etc.) se conservan idénticos para garantizar que el frontend React **no requiera cambios** al integrarse con el backend Spring Boot 3.

---

*Documento SDD generado para el Sistema de Restaurante — backend Spring Boot 3 + MySQL, frontend React 18 + MUI. Fuente de verdad bajo Spec-Driven Development.*
