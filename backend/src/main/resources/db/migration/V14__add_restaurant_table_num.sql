-- Add missing num column to restaurant_table
ALTER TABLE restaurant_table ADD COLUMN num INT NOT NULL DEFAULT 1 AFTER restaurant_id;
