

-------------------------------
-- Auto-update updated --
-------------------------------
CREATE OR REPLACE FUNCTION modified_timestamp() RETURNS TRIGGER 
LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

DO $$
DECLARE
    t text;
BEGIN
    FOR t IN 
    SELECT tabs.table_name, table_type FROM information_schema.columns AS cols
    JOIN information_schema.tables AS tabs
    ON tabs.table_name = cols.table_name
    WHERE column_name = 'updated' AND tabs.table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS updated_%I on %I',
	t,t);
        EXECUTE format('CREATE TRIGGER updated_%I
                        BEFORE UPDATE ON %I
                        FOR EACH ROW EXECUTE PROCEDURE modified_timestamp()',
                        t,t);
    END LOOP;
END;
$$ LANGUAGE plpgsql;
