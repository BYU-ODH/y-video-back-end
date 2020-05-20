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
CREATE TABLE Account (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  email TEXT UNIQUE,
  lastlogin TEXT,
  name TEXT,
  role INTEGER,
  username TEXT
);
--;;
CREATE TABLE TWord (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  account_id TEXT REFERENCES Account(id),
  tword TEXT,
  src_lang TEXT,
  dest_lang TEXT
);
--;;
CREATE TABLE Collection (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  name TEXT,
  published BOOLEAN,
  archived BOOLEAN
);
--;;
CREATE TABLE Course (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  department TEXT,
  catalog_number TEXT,
  section_number TEXT
);
--;;
CREATE TABLE Content (
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
CREATE TABLE File (
  id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  filepath TEXT,
  mime TEXT,
  metadata TEXT
);
--;;
CREATE TABLE Account_Collection (
  account_id TEXT REFERENCES Account(id) ON DELETE CASCADE,
  collection_id TEXT REFERENCES Collection(id) ON DELETE CASCADE,
  role TEXT,
  PRIMARY KEY (account_id, collection_id)
);
--;;
CREATE TABLE Collection_Course (
  collection_id TEXT REFERENCES Collection(id),
  course_id TEXT REFERENCES Course(id),
  PRIMARY KEY (collection_id, course_id)
);
--;;
CREATE TABLE Content_File (
  content_id TEXT REFERENCES Content(id),
  file_id TEXT REFERENCES File(id),
  PRIMARY KEY (content_id, file_id)
);
