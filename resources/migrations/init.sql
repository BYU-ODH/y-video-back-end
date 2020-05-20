-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- needs to be executed once by db admin on this individual db
--;;
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,email TEXT UNIQUE
   ,last_login TEXT
   ,account_name TEXT
   ,account_role INTEGER
   ,username TEXT
);
COMMENT ON TABLE users IS 'User-accounts matching netid';

DROP TABLE IF EXISTS words CASCADE;
CREATE TABLE words (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,user_id TEXT REFERENCES users(id)
   ,word TEXT
   ,src_lang TEXT
   ,dest_lang TEXT
);
COMMENT ON TABLE translation_word IS 'Is this what tword stood for? It needs to be more clear';

DROP TABLE IF EXISTS collections CASCADE;
CREATE TABLE collections (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,collection_name TEXT -- Because "name" is a reserved word
   ,published BOOLEAN
   ,archived BOOLEAN
);

DROP TABLE IF EXISTS courses CASCADE;
CREATE TABLE courses (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,department TEXT -- should this be a foreign key?
   ,catalog_number TEXT
   ,section_number TEXT
);

DROP TABLE IF EXISTS content CASCADE;
CREATE TABLE contents (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,collection_id TEXT
   ,content_name TEXT
   ,content_type TEXT -- type is a reserved word
   ,requester_email TEXT
   ,thumbnail TEXT
   ,copyrighted BOOLEAN
   ,physical_copy_exists BOOLEAN
   ,full_video BOOLEAN
   ,published BOOLEAN
   ,allow_definitions BOOLEAN
   ,allow_notes BOOLEAN
   ,allow_captions BOOLEAN
   ,date_validated TEXT
   ,views INTEGER
   ,metadata TEXT
);

DROP TABLE IF EXISTS files CASCADE;
CREATE TABLE files (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,filepath TEXT
   ,mime TEXT
   ,metadata TEXT
);

DROP TABLE IF EXISTS account_collections CASCADE;
CREATE TABLE account_collections (
   user_id UUID REFERENCES users(id) ON DELETE CASCADE
   ,collection_id TEXT REFERENCES collections(id) ON DELETE CASCADE
   ,account_role TEXT -- role is a reserved word
   ,PRIMARY KEY (user_id, collection_id)
);

DROP TABLE IF EXISTS collection_courses CASCADE;
CREATE TABLE collection_courses (
   collection TEXT REFERENCES collections(id)
   ,course_id TEXT REFERENCES courses(id)
   ,PRIMARY KEY (collection_id, course_id)
);

DROP TABLE IF EXISTS content_files CASCADE;
CREATE TABLE content_files (
   contents TEXT REFERENCES contents(id)
   ,file_id TEXT REFERENCES files(id)
   ,PRIMARY KEY (content_id, file_id)
);



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

---------------------
-- UNDELETED VIEWS --
---------------------
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    t text;
BEGIN
    FOR t IN 
    SELECT tabs.table_name, table_type FROM information_schema.columns AS cols
    JOIN information_schema.tables AS tabs
    ON tabs.table_name = cols.table_name
    WHERE column_name = 'deleted' AND tabs.table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('DROP VIEW IF EXISTS %I_undeleted CASCADE',
	t);
        EXECUTE format('CREATE VIEW %I_undeleted
                           AS SELECT %I.* FROM %I where %I.deleted IS NULL',
                        t,t,t,t);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

---------------------
-- DEPENDENT VIEWS --
---------------------

-- TODO Need to include source name
-- Problem: is returning multiples

DROP VIEW IF EXISTS unannotated_material CASCADE;
CREATE VIEW unannotated_material AS
SELECT m.* 
FROM material m
  INNER JOIN sources s ON s.source_id = m.source_id
  LEFT JOIN products_undeleted pu ON pu.material_id = m.id
WHERE pu.material_id IS NULL
ORDER BY retrieved DESC;

   -- SELECT m.*, s.source_name
   --    FROM material_undeleted m
   --    LEFT JOIN sources_undeleted s
   --       ON m.source_id = s.id
   --    WHERE m.id NOT IN (SELECT p.material_id FROM products_undeleted p)
   --    ORDER BY retrieved DESC;
-- WIP: produces duplicates

COMMENT ON VIEW unannotated_material IS 'Undeleted material which is unannotated'; 
