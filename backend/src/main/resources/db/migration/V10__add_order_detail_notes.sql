-- Add missing notes column to order_detail table
ALTER TABLE order_detail ADD COLUMN notes VARCHAR(255) AFTER status;
