-- Add missing restaurant_id column to order_detail table
ALTER TABLE order_detail ADD COLUMN restaurant_id CHAR(36) NOT NULL AFTER id;

-- Add missing restaurant_id column to product_option table
ALTER TABLE product_option ADD COLUMN restaurant_id CHAR(36) NOT NULL AFTER id;
