-- ============ CASH REGISTERS (💰) ============
-- Migration V6__init_cash_registers.sql
-- Implements: SPEC-CASH-001, INV-03 (idempotent payments), INV-05
-- Judgment double required

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
  CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT chk_cr_status CHECK (status IN ('OPEN', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transaction` (
  id              CHAR(36)      NOT NULL,
  cash_register_id CHAR(36)     NOT NULL,
  type            VARCHAR(20)   NOT NULL,  -- INCOME | EXPENSE
  amount          DECIMAL(12,2) NOT NULL,
  description     VARCHAR(255),
  payment_method  VARCHAR(20),
  reference_id    CHAR(36),                -- bill/invoice asociado (INV-03 idempotencia)
  created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tx_cr (cash_register_id),
  UNIQUE KEY uk_tx_reference (reference_id),  -- INV-03: evita doble cobro
  CONSTRAINT fk_tx_cr FOREIGN KEY (cash_register_id) REFERENCES cash_register(id),
  CONSTRAINT chk_tx_type CHECK (type IN ('INCOME', 'EXPENSE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
