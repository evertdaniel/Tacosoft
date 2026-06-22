-- Add missing description column to section table
ALTER TABLE section ADD COLUMN description VARCHAR(255) AFTER name;
