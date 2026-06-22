-- ============ TABLES & CLIENTS ============
-- Migration V3__init_tables_clients.sql
-- Implements: SPEC-TABLE-001

CREATE TABLE restaurant_table (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)   NOT NULL,
  name          VARCHAR(60) NOT NULL,
  seats         INT         NOT NULL DEFAULT 4,
  pos_x         INT         NOT NULL DEFAULT 0,
  pos_y         INT         NOT NULL DEFAULT 0,
  status        VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE|OCCUPIED|RESERVED|CLEANING
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_table_rest (restaurant_id),
  CONSTRAINT fk_table_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT chk_table_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'CLEANING'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE client (
  id            CHAR(36)      NOT NULL,
  restaurant_id CHAR(36)     NOT NULL,
  person_id     CHAR(36)      NOT NULL,
  type          VARCHAR(30)   NOT NULL DEFAULT 'REGULAR',
  credit_limit  DECIMAL(12,2) NOT NULL DEFAULT 0,
  is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_client_person FOREIGN KEY (person_id) REFERENCES person(id),
  CONSTRAINT fk_client_rest   FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
