-- ============ BILLING (💰) ============
-- Migration V5__init_billing.sql
-- Implements: SPEC-BILL-001, INV-02 (folio sequence)
-- Judgment double required

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_folio (restaurant_id, folio),  -- INV-02
  CONSTRAINT fk_invoice_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES `order`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE folio_sequence (
  restaurant_id CHAR(36) NOT NULL,
  next_folio    BIGINT   NOT NULL DEFAULT 1,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (restaurant_id),
  CONSTRAINT fk_folio_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
