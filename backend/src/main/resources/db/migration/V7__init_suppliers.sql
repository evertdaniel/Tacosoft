-- ============ SUPPLIERS ============
-- Migration V7__init_suppliers.sql

CREATE TABLE supplier (
  id            CHAR(36)      NOT NULL,
  restaurant_id CHAR(36)      NOT NULL,
  name          VARCHAR(150)  NOT NULL,
  contact_name  VARCHAR(120),
  email         VARCHAR(120),
  phone         VARCHAR(40),
  address       VARCHAR(255),
  tax_id        VARCHAR(40),
  is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_supplier_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
