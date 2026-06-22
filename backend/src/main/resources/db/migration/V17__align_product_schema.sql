-- ============ PRODUCT SCHEMA ALIGNMENT ============
-- Migration V17__align_product_schema.sql
-- Adds columns required by the Product entity that were missing from V2__init_menu.sql.
-- This is a corrective migration discovered during integration test execution.

ALTER TABLE product ADD COLUMN image_url VARCHAR(255);
ALTER TABLE product ADD COLUMN tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0;
ALTER TABLE product ADD COLUMN stock INT NOT NULL DEFAULT 0;
ALTER TABLE product ADD COLUMN manage_stock BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE product ADD COLUMN preparation_time INT NOT NULL DEFAULT 15;
