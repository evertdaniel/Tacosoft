-- Rename product_option.price to price_adjustment to match entity
ALTER TABLE product_option CHANGE price price_adjustment DECIMAL(12,2) NOT NULL;
