-- This DB implements UUID ids on (almost) every table, constraints for composite keys, soft-deletes and 'updated' for data audit

DROP TABLE IF EXISTS annotations CASCADE;

DROP EXTENSION IF EXISTS pgcrypto CASCADE; -- uncomment for new db
CREATE EXTENSION pgcrypto; -- uncomment for new db

DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,last_person_api TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,last_course_api TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,email TEXT
   ,last_login TEXT
   ,account_name TEXT
   ,account_type INTEGER
   ,username TEXT
   ,byu_person_id TEXT DEFAULT '000000000'
   , CONSTRAINT no_duplicate_user_emails UNIQUE (deleted, username)
);
COMMENT ON TABLE users IS 'User-accounts matching netid';

DROP TABLE IF EXISTS user_type_exceptions CASCADE;
CREATE TABLE user_type_exceptions (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,username TEXT
   ,account_type INTEGER
   , CONSTRAINT no_duplicate_user_type_exceptions UNIQUE (deleted, username)
);
COMMENT ON TABLE user_type_exceptions IS 'Marks users as lab assistants or admins. Overrides account-type from api on user creation.';

DROP TABLE IF EXISTS words CASCADE;
CREATE TABLE words (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,user_id UUID REFERENCES users(id)
   ,word TEXT
   ,src_lang TEXT
   ,dest_lang TEXT
   , CONSTRAINT no_duplicate_user_words UNIQUE (deleted, user_id, word, src_lang, dest_lang)
);
COMMENT ON TABLE words IS 'Vocab words with source and destination language codes';


DROP TABLE IF EXISTS collections CASCADE;
CREATE TABLE collections (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,collection_name TEXT -- Because name is a reserved word
   ,owner UUID REFERENCES users(id)
   ,published BOOLEAN
   ,archived BOOLEAN
   ,public BOOLEAN
   ,copyrighted BOOLEAN
   , CONSTRAINT no_duplicate_owner_names UNIQUE (deleted, owner, collection_name)
);
COMMENT ON TABLE collections IS 'Collections of content/resources';

DROP TABLE IF EXISTS courses CASCADE;
CREATE TABLE courses (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,department TEXT -- should this be a foreign key?
   ,catalog_number TEXT
   ,section_number TEXT
   , CONSTRAINT no_duplicate_courses UNIQUE (deleted, department, catalog_number, section_number)
);
COMMENT ON TABLE courses IS 'Courses (or classes) at BYU';

DROP TABLE IF EXISTS resources CASCADE;
CREATE TABLE resources (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,resource_name TEXT
   ,resource_type TEXT -- type is a reserved word
   ,requester_email TEXT
   ,copyrighted BOOLEAN
   ,physical_copy_exists BOOLEAN
   ,full_video BOOLEAN
   ,published BOOLEAN
   ,date_validated TEXT
   ,views INTEGER
   ,all_file_versions TEXT
   ,metadata TEXT
   --,public BOOLEAN
);
COMMENT ON TABLE resources IS 'Referenced by contents, hold media in files table';

DROP TABLE IF EXISTS resource_access CASCADE;
CREATE TABLE resource_access (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,username TEXT NOT NULL
    ,resource_id UUID REFERENCES resources(id)
    ,last_verified  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   , CONSTRAINT no_duplicate_resource_access UNIQUE (deleted, username, resource_id)
);
COMMENT ON TABLE resource_access IS 'Tracks which users may add this resource to contents';

DROP TABLE IF EXISTS languages CASCADE;
CREATE TABLE languages (
    id TEXT NOT NULL PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE languages IS 'List of language options for file-versions';

DROP TABLE IF EXISTS files CASCADE;
CREATE TABLE files (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,resource_id UUID REFERENCES resources(id)
   ,filepath TEXT
   ,file_version TEXT
   ,metadata TEXT
   , CONSTRAINT no_duplicate_filepaths UNIQUE (deleted, filepath)
);

-- replace this line in files table for testing 
-- ,file_version TEXT REFERENCES languages(id) -- for prod
-- ,file_version TEXT -- for test

COMMENT ON TABLE files IS 'Files represent media (i.e. videos) with path to file and metadata';

DROP TABLE IF EXISTS file_keys CASCADE;
CREATE TABLE file_keys (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,file_id UUID REFERENCES files(id)
   ,user_id UUID REFERENCES users(id)
);
COMMENT ON TABLE file_keys IS 'Volatile keys to represent files for streaming to front end';

DROP TABLE IF EXISTS contents CASCADE;
CREATE TABLE contents (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,title TEXT
    ,content_type TEXT
    ,url TEXT
    ,description TEXT
    ,tags TEXT
    ,annotations TEXT
    ,thumbnail TEXT
    ,allow_definitions BOOLEAN
    ,allow_notes BOOLEAN
    ,allow_captions BOOLEAN
    ,views INTEGER
    ,file_version TEXT
    ,published BOOLEAN
    ,words TEXT
    ,clips TEXT
    ,resource_id UUID REFERENCES resources(id)
    ,collection_id UUID REFERENCES collections(id)
    -- ,file_id UUID REFERENCES files(id)
    --,public BOOLEAN
);
COMMENT ON TABLE contents IS 'Contains contents to be applied over resources';

DROP TABLE IF EXISTS subtitles CASCADE;
CREATE TABLE subtitles (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,title TEXT
    ,language TEXT
    ,content TEXT
    ,words TEXT
    ,content_id UUID REFERENCES contents(id)
);
COMMENT ON TABLE subtitles IS 'Contains subtitles to be applied over contents';

DROP TABLE IF EXISTS auth_tokens CASCADE;
CREATE TABLE auth_tokens (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,user_id UUID REFERENCES users(id)
);
COMMENT ON TABLE auth_tokens IS 'Contains auth_tokens (stored in id) mapped to user ids';

DROP TABLE IF EXISTS content_subtitles_assoc CASCADE;

DROP TABLE IF EXISTS user_collections_assoc CASCADE;
CREATE TABLE user_collections_assoc (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,username TEXT
   ,collection_id UUID REFERENCES collections(id) ON DELETE CASCADE
   ,account_role INTEGER
   , CONSTRAINT no_duplicate_user_collections UNIQUE (deleted, username, collection_id)
);
COMMENT ON TABLE user_collections_assoc IS 'Many-to-many table connecting users and collections, incl. user roles in collections';

DROP TABLE IF EXISTS user_courses_assoc CASCADE;
CREATE TABLE user_courses_assoc (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,user_id UUID REFERENCES users(id) ON DELETE CASCADE
   ,course_id UUID REFERENCES courses(id) ON DELETE CASCADE
   ,account_role INTEGER
   , CONSTRAINT no_duplicate_user_courses UNIQUE (deleted, user_id, course_id)
);
COMMENT ON TABLE user_courses_assoc IS 'Many-to-many table connecting users and courses, incl. user roles in courses';


DROP TABLE IF EXISTS collection_courses_assoc CASCADE;
CREATE TABLE collection_courses_assoc (
   id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,collection_id UUID REFERENCES collections(id)
   ,course_id UUID REFERENCES courses(id)
   , CONSTRAINT no_duplicate_course_collections UNIQUE (deleted, course_id, collection_id)
);
COMMENT ON TABLE collection_courses_assoc IS 'Many-to-many table connecting collections and courses';


DROP TABLE IF EXISTS "email_logs" CASCADE;
CREATE TABLE "email_logs" (
    id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,sender_id UUID REFERENCES users(id)
    ,sender_email TEXT NOT NULL
    ,recipients TEXT[] NOT NULL
    ,subject TEXT DEFAULT '' NOT NULL
    ,message TEXT DEFAULT '' NOT NULL
    ,sent_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,deleted TIMESTAMP DEFAULT NULL
    ,created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE email_logs IS 'Tracks all emails sent';


DROP TABLE IF EXISTS collection_resources_assoc CASCADE;


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

-------------------------
-- EXPIRED AUTH-TOKENS --
-------------------------
CREATE OR REPLACE FUNCTION delete_expired_auth_tokens() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  DELETE FROM auth_tokens WHERE created < NOW() - INTERVAL '8 hours 30 minutes';
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS delete_expired_auth_tokens_trigger ON auth_tokens;
CREATE TRIGGER delete_expired_auth_tokens_trigger
    AFTER INSERT ON auth_tokens
    EXECUTE PROCEDURE delete_expired_auth_tokens();

-------------------------
-- EXPIRED FILE-KEYS --
-------------------------
CREATE OR REPLACE FUNCTION delete_expired_file_keys() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  DELETE FROM file_keys WHERE created < NOW() - INTERVAL '6 hours';
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS delete_expired_file_keys_trigger ON file_keys;
CREATE TRIGGER delete_expired_file_keys_trigger
    AFTER INSERT ON file_keys
    EXECUTE PROCEDURE delete_expired_file_keys();

---------------------
-- UNDELETED VIEWS --
---------------------

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

-- These will be views which depend upon other views, such as *_undeleted

DROP VIEW IF EXISTS public_collections_undeleted;
CREATE VIEW public_collections_undeleted AS
    SELECT collections_undeleted.*
    FROM collections_undeleted
    WHERE collections_undeleted.public = true;

DROP VIEW IF EXISTS users_by_collection;
CREATE VIEW users_by_collection AS
    SELECT users_undeleted.*, uca.account_role, uca.collection_id
    FROM users_undeleted JOIN user_collections_assoc_undeleted AS uca
    ON users_undeleted.username = uca.username;

DROP VIEW IF EXISTS collections_by_user;
CREATE VIEW collections_by_user AS
    SELECT collections_undeleted.*, uca.account_role, users.id AS user_id, users.username AS username
    FROM collections_undeleted JOIN user_collections_assoc_undeleted AS uca
    ON collections_undeleted.id = uca.collection_id
    JOIN users_undeleted AS users
    ON users.username = uca.username;

DROP VIEW IF EXISTS users_by_course;
CREATE VIEW users_by_course AS
    SELECT users_undeleted.*, uca.account_role, uca.course_id
    FROM users_undeleted JOIN user_courses_assoc_undeleted AS uca
    ON users_undeleted.id = uca.user_id;

DROP VIEW IF EXISTS courses_by_user;
CREATE VIEW courses_by_user AS
    SELECT courses_undeleted.*, uca.account_role, uca.user_id
    FROM courses_undeleted JOIN user_courses_assoc_undeleted AS uca
    ON courses_undeleted.id = uca.course_id;

DROP VIEW IF EXISTS subtitles_by_resource;
CREATE VIEW subtitles_by_resource AS
    SELECT subtitles_undeleted.*, r.id AS resource_id
    FROM subtitles_undeleted JOIN contents_undeleted
    ON subtitles_undeleted.content_id = contents_undeleted.id
    JOIN resources_undeleted AS r
    ON r.id = contents_undeleted.resource_id;

DROP VIEW IF EXISTS collections_by_course;
CREATE VIEW collections_by_course AS
    SELECT collections_undeleted.*, cca.course_id
    FROM collections_undeleted JOIN collection_courses_assoc_undeleted AS cca
    ON collections_undeleted.id = cca.collection_id;

DROP VIEW IF EXISTS courses_by_collection;
CREATE VIEW courses_by_collection AS
    SELECT courses_undeleted.*, cca.collection_id
    FROM courses_undeleted JOIN collection_courses_assoc_undeleted AS cca
    ON courses_undeleted.id = cca.course_id;

DROP VIEW IF EXISTS collections_by_users_via_courses;
CREATE VIEW collections_by_users_via_courses AS
    SELECT collections_undeleted.*, uca.user_id
    FROM users_undeleted
    JOIN user_courses_assoc_undeleted AS uca
    ON users_undeleted.id = uca.user_id
    JOIN collection_courses_assoc_undeleted AS cca
    ON uca.course_id = cca.course_id
    JOIN collections_undeleted
    ON cca.collection_id = collections_undeleted.id;

DROP VIEW IF EXISTS collections_by_resource;
CREATE VIEW collections_by_resource AS
    SELECT collections_undeleted.*, resources_undeleted.id AS resource_id
    FROM collections_undeleted
    JOIN contents_undeleted
    ON collections_undeleted.id = contents_undeleted.collection_id
    JOIN resources_undeleted ON contents_undeleted.resource_id = resources_undeleted.id;

DROP VIEW IF EXISTS cont_res_sub;


DROP VIEW IF EXISTS user_collection_roles;
CREATE VIEW user_collection_roles AS
    SELECT c.id AS collection_id, u.id AS user_id, uca.account_role AS role
    FROM users_undeleted AS u
    JOIN user_collections_assoc_undeleted AS uca
    ON u.username = uca.username
    JOIN collections_undeleted AS c
    ON c.id = uca.collection_id

    UNION

    SELECT c.id AS collection_id, u.id AS user_id, ucsa.account_role AS role
    FROM users_undeleted AS u
    JOIN user_courses_assoc_undeleted AS ucsa
    ON u.id = ucsa.user_id
    JOIN courses_undeleted AS cs
    ON cs.id = ucsa.course_id
    JOIN collection_courses_assoc_undeleted AS ucca
    ON cs.id = ucca.course_id
    JOIN collections AS c
    ON c.id = ucca.collection_id

    UNION

    SELECT c.id AS collection_id, c.owner AS user_id, 0 AS role
    FROM collections_undeleted AS c;

COMMENT ON VIEW user_collection_roles IS 'Tracks what role each user plays in the collections to which they are connected';


DROP VIEW IF EXISTS parent_collections;
CREATE VIEW parent_collections AS
    SELECT c.id AS collection_id, c.id AS object_id
    FROM collections_undeleted AS c

    UNION

    SELECT c.id AS collection_id, crse.id AS object_id
    FROM collections_undeleted AS c
    JOIN collection_courses_assoc_undeleted AS cca
    ON c.id = cca.collection_id
    JOIN courses_undeleted AS crse
    ON crse.id = cca.course_id

    UNION

    SELECT c.id AS collection_id, cont.id AS object_id
    FROM collections_undeleted AS c
    JOIN contents_undeleted AS cont
    ON cont.collection_id = c.id

    UNION

    SELECT c.id AS collection_id, r.id AS object_id
    FROM collections_undeleted AS c
    JOIN contents_undeleted AS cont
    ON cont.collection_id = c.id
    JOIN resources_undeleted AS r
    ON r.id = cont.resource_id

    UNION

    SELECT c.id AS collection_id, s.id AS object_id
    FROM collections_undeleted AS c
    JOIN contents_undeleted AS cont
    ON cont.collection_id = c.id
    JOIN subtitles_undeleted AS s
    ON cont.id = s.content_id

    UNION

    SELECT c.id AS collection_id, f.id AS object_id
    FROM collections_undeleted AS c
    JOIN contents_undeleted AS cont
    ON cont.collection_id = c.id
    JOIN resources_undeleted AS r
    ON r.id = cont.resource_id
    JOIN files_undeleted AS f
    ON r.id = f.resource_id;

COMMENT ON VIEW parent_collections IS 'Tracks which collections each content, resource, subtitle, and file are connected to (possibly more than one)';

--------------------------------------------------
-- Set up resource placeholder for online media --
--------------------------------------------------

INSERT INTO resources (id, resource_name, resource_type, requester_email, copyrighted, physical_copy_exists, full_video, published, date_validated, views, all_file_versions, metadata)
VALUES ('00000000-0000-0000-0000-000000000000', 'online-media', 'online-media', '', true, false, true, true, '', 0, '', '');

-- ALTER STATEMENTS FOR UPDATED DB

ALTER TABLE files ADD COLUMN aspect_ratio VARCHAR DEFAULT '';

INSERT INTO files (id, resource_id, filepath, file_version, metadata, aspect_ratio)
VALUES ('00000000-0000-0000-0000-000000000000', '00000000-0000-0000-0000-000000000000', 'empty', 'English', '', '0,0');

ALTER TABLE contents ADD COLUMN file_id UUID DEFAULT '00000000-0000-0000-0000-000000000000' REFERENCES files(id);

CREATE OR REPLACE VIEW files_undeleted AS SELECT * FROM files WHERE deleted is NULL;

CREATE OR REPLACE VIEW contents_undeleted AS SELECT * FROM contents WHERE deleted is NULL;