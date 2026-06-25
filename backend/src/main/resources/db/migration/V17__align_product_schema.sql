-- ============ PRODUCT SCHEMA ALIGNMENT ============
-- Migration V17__align_product_schema.sql
-- Adds columns required by the Product entity that were missing from V2__init_menu.sql.
-- This is a corrective migration discovered during integration test execution.

-- Idempotent: this corrective migration may run against databases where some
-- or all of these columns were already added by an earlier migration revision.
ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(255);
ALTER TABLE product ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0;
ALTER TABLE product ADD COLUMN IF NOT EXISTS stock INT NOT NULL DEFAULT 0;
ALTER TABLE product ADD COLUMN IF NOT EXISTS manage_stock BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE product ADD COLUMN IF NOT EXISTS preparation_time INT NOT NULL DEFAULT 15;
