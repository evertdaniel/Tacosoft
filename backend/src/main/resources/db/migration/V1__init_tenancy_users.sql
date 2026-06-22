-- ============ TENANCY & USERS ============
-- Migration V1__init_tenancy_users.sql
-- Implements: INV-06 (tenant isolation), SPEC-AUTH-001

-- Restaurant table (tenant)
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

-- Person table (shared personal data)
CREATE TABLE person (
  id          CHAR(36)      NOT NULL,
  first_name  VARCHAR(80)   NOT NULL,
  last_name   VARCHAR(80)   NOT NULL,
  email       VARCHAR(120),
  phone       VARCHAR(40),
  address     VARCHAR(255),
  document_id VARCHAR(40),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- App user table
CREATE TABLE app_user (
  id          CHAR(36)      NOT NULL,
  username    VARCHAR(60)   NOT NULL,
  password    VARCHAR(100)  NOT NULL,   -- BCrypt
  person_id   CHAR(36)      NOT NULL,
  primary_role_id INT       NULL,
  is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
  online      BOOLEAN       NOT NULL DEFAULT FALSE,
  last_login  TIMESTAMP     NULL,
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES person(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Role table
CREATE TABLE role (
  id          INT           NOT NULL AUTO_INCREMENT,
  name        VARCHAR(20)   NOT NULL,   -- ADMIN, COOK, WAITER, CASHIER
  description VARCHAR(120),
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- User-restaurant-role association (RBAC + multi-tenancy)
CREATE TABLE user_restaurant_role (
  id            CHAR(36)    NOT NULL,
  user_id       CHAR(36)    NOT NULL,
  restaurant_id CHAR(36)   NOT NULL,
  role_id       INT         NOT NULL,
  created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_urr (user_id, restaurant_id, role_id),
  CONSTRAINT fk_urr_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_urr_rest FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
  CONSTRAINT fk_urr_role FOREIGN KEY (role_id) REFERENCES role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Seed roles
INSERT INTO role (name, description) VALUES
('ADMIN', 'Full system access'),
('COOK', 'Kitchen operations'),
('WAITER', 'Table service'),
('CASHIER', 'Cash register and billing');

-- Add role FK after role table creation
ALTER TABLE app_user ADD CONSTRAINT fk_user_role
    FOREIGN KEY (primary_role_id) REFERENCES role(id);
