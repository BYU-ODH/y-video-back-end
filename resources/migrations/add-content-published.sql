ALTER TABLE contents
ADD COLUMN published BOOLEAN;

UPDATE contents
SET published = true;
