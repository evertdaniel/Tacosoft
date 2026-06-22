-- Add restaurant_id to app_user (tenant-scoped user lookup)
ALTER TABLE app_user ADD COLUMN restaurant_id CHAR(36) NULL AFTER person_id;
