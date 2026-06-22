-- Add missing description column to product_option table
ALTER TABLE product_option ADD COLUMN description VARCHAR(255) AFTER name;
