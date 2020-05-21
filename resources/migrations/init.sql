DROP EXTENSION IF EXISTS pgcrypto CASCADE;
--;;
CREATE EXTENSION pgcrypto;
--;;
DROP TABLE IF EXISTS users CASCADE;
--;;
CREATE TABLE users (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,email TEXT UNIQUE
   ,last_login TEXT
   ,account_name TEXT
   ,account_role INTEGER
   ,username TEXT
);

--;;
COMMENT ON TABLE users IS 'User-accounts matching netid';

--;;
DROP TABLE IF EXISTS words CASCADE;
--;;
CREATE TABLE words (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,user_id UUID REFERENCES users(id)
   ,word TEXT
   ,src_lang TEXT
   ,dest_lang TEXT
);

--;;
COMMENT ON TABLE words IS 'Vocab words with source and destination language codes';


--;;
DROP TABLE IF EXISTS collections CASCADE;
--;;
CREATE TABLE collections (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,collection_name TEXT -- Because name is a reserved word
   ,published BOOLEAN
   ,archived BOOLEAN
);

--;;
COMMENT ON TABLE collections IS 'Collections of content/resources';

--;;
DROP TABLE IF EXISTS courses CASCADE;
--;;
CREATE TABLE courses (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,department TEXT -- should this be a foreign key?
   ,catalog_number TEXT
   ,section_number TEXT
);

--;;
COMMENT ON TABLE courses IS 'Courses (or classes) at BYU';

--;;
DROP TABLE IF EXISTS contents CASCADE;
--;;
CREATE TABLE contents (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,collection_id UUID REFERENCES collections(id)
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

--;;
COMMENT ON TABLE contents IS 'Contained within collections, hold media in files table';

--;;
DROP TABLE IF EXISTS files CASCADE;
--;;
CREATE TABLE files (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,filepath TEXT
   ,mime TEXT
   ,metadata TEXT
);

--;;
COMMENT ON TABLE files IS 'Files represent media (i.e. videos) with path to file and metadata';

--;;
DROP TABLE IF EXISTS user_collections CASCADE;
--;;
CREATE TABLE user_collections (
   user_id UUID REFERENCES users(id) ON DELETE CASCADE
   ,collection_id UUID REFERENCES collections(id) ON DELETE CASCADE
   ,account_role TEXT -- role is a reserved word
   ,PRIMARY KEY (user_id, collection_id)
);

--;;
COMMENT ON TABLE user_collections IS 'Many-to-many table connecting users and collections, incl. user roles in collections';

--;;
DROP TABLE IF EXISTS collection_courses CASCADE;
--;;
CREATE TABLE collection_courses (
   collection_id UUID REFERENCES collections(id)
   ,course_id UUID REFERENCES courses(id)
   ,PRIMARY KEY (collection_id, course_id)
);

--;;
COMMENT ON TABLE collection_courses IS 'Many-to-many table connecting collections and courses';

--;;
DROP TABLE IF EXISTS content_files CASCADE;
--;;
CREATE TABLE content_files (
   content_id UUID REFERENCES contents(id)
   ,file_id UUID REFERENCES files(id)
   ,PRIMARY KEY (content_id, file_id)
);

--;;
COMMENT ON TABLE content_files IS 'Many-to-many table connecting contents and files';
