-- ============ MENU ============
-- Migration V2__init_menu.sql
-- Implements: SPEC-MENU-001

CREATE TABLE section (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)   NOT NULL,
  name          VARCHAR(120) NOT NULL,
  display_order INT         NOT NULL DEFAULT 0,
  is_public     BOOLEAN     NOT NULL DEFAULT TRUE,
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_section_rest (restaurant_id),
  CONSTRAINT fk_section_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE category (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)   NOT NULL,
  section_id    CHAR(36)   NOT NULL,
  name          VARCHAR(120) NOT NULL,
  is_public     BOOLEAN     NOT NULL DEFAULT TRUE,
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_category_section (section_id),
  CONSTRAINT fk_category_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_category_section FOREIGN KEY (section_id) REFERENCES section(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE production_area (
  id            CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)   NOT NULL,
  name          VARCHAR(120) NOT NULL,
  description   VARCHAR(255),
  is_active     BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_parea_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_product_category (category_id),
  CONSTRAINT fk_product_rest   FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT fk_product_parea   FOREIGN KEY (production_area_id) REFERENCES production_area(id),
  CONSTRAINT chk_product_status CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'OUT_OF_SEASON'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_option_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
