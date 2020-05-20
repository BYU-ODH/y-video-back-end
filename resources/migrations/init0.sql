DROP TABLE IF EXISTS vocab_words;
--;;
DROP TABLE IF EXISTS account_collection;
--;;
DROP TABLE IF EXISTS collection_course;
--;;
DROP TABLE IF EXISTS collection_content;
--;;
DROP TABLE IF EXISTS conent_file;
--;;
DROP TABLE IF EXISTS users;
--;;
DROP TABLE IF EXISTS collections;
--;;
DROP TABLE IF EXISTS courses;
--;;
DROP TABLE IF EXISTS contents;
--;;
DROP TABLE IF EXISTS files;
--;;
DROP EXTENSION IF EXISTS "pgcrypto";
--;;
DROP FUNCTION IF EXISTS uuid_generate_v4;
--;;
CREATE EXTENSION pgcrypto;
--;;
CREATE FUNCTION uuid_generate_v4()
RETURNS uuid
AS '
BEGIN
RETURN gen_random_uuid();
END'
LANGUAGE 'plpgsql';
--;;
CREATE TABLE "users" (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  email TEXT UNIQUE,
  lastlogin TEXT,
  name TEXT,
  role INTEGER,
  username TEXT
);
--;;
CREATE TABLE "vocab_words" (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  user_id TEXT REFERENCES users(id),
  word TEXT,
  src_lang TEXT,
  dest_lang TEXT
);
--;;
CREATE TABLE "collections" (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  name TEXT,
  published BOOLEAN,
  archived BOOLEAN
);
--;;
CREATE TABLE "courses" (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  department TEXT,
  catalog_number TEXT,
  section_number TEXT
);
--;;
CREATE TABLE "contents" (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  collection_id TEXT,
  name TEXT,
  type TEXT,
  requester_email TEXT,
  thumbnail TEXT,
  copyrighted BOOLEAN,
  physical_copy_exists BOOLEAN,
  full_video BOOLEAN,
  published BOOLEAN,
  allow_definitions BOOLEAN,
  allow_notes BOOLEAN,
  allow_captions BOOLEAN,
  date_validated TEXT,
  views INTEGER,
  metadata TEXT
);
--;;
CREATE TABLE "files" (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  filepath TEXT,
  mime TEXT,
  metadata TEXT
);
--;;
CREATE TABLE "account_collection" (
  user_id TEXT REFERENCES users(id) ON DELETE CASCADE,
  collection_id TEXT REFERENCES collections(id) ON DELETE CASCADE,
  role TEXT,
  PRIMARY KEY (user_id, collection_id)
);
--;;
CREATE TABLE "collection_course" (
  collection_id TEXT REFERENCES collections(id),
  course_id TEXT REFERENCES courses(id),
  PRIMARY KEY (collection_id, course_id)
);
--;;
CREATE TABLE "content_file" (
  content_id TEXT REFERENCES contents(id),
  file_id TEXT REFERENCES files(id),
  PRIMARY KEY (content_id, file_id)
);
