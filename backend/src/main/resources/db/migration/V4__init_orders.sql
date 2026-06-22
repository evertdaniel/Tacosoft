-- ============ ORDERS ============
-- Migration V4__init_orders.sql
-- Implements: SPEC-ORDER-001, SPEC-ORDER-002, INV-01, INV-04

CREATE TABLE `order` (
  id            CHAR(36)      NOT NULL,
  restaurant_id CHAR(36)      NOT NULL,
  num           INT           NOT NULL,           -- correlativo por restaurante (INV-01)
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
  UNIQUE KEY uk_order_num (restaurant_id, num),  -- INV-01
  KEY idx_order_rest_status (restaurant_id, status),
  CONSTRAINT fk_order_rest   FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_order_table  FOREIGN KEY (table_id) REFERENCES restaurant_table(id),
  CONSTRAINT fk_order_client FOREIGN KEY (client_id) REFERENCES client(id),
  CONSTRAINT fk_order_user   FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT chk_order_type   CHECK (type IN ('IN_PLACE', 'TAKE_AWAY')),
  CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'READY', 'DELIVERED', 'CANCELLED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  CONSTRAINT fk_detail_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_detail_option  FOREIGN KEY (product_option_id) REFERENCES product_option(id),
  CONSTRAINT fk_detail_created_by FOREIGN KEY (created_by) REFERENCES app_user(id),
  CONSTRAINT fk_detail_updated_by FOREIGN KEY (updated_by) REFERENCES app_user(id),
  CONSTRAINT chk_detail_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'READY', 'DELIVERED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
