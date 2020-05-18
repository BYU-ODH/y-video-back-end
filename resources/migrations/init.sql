DROP TABLE IF EXISTS TWord;
DROP TABLE IF EXISTS Account_Collection;
DROP TABLE IF EXISTS Collection_Course;
DROP TABLE IF EXISTS Collection_Content;
DROP TABLE IF EXISTS Content_File;
DROP TABLE IF EXISTS Account;
DROP TABLE IF EXISTS Collection;
DROP TABLE IF EXISTS Course;
DROP TABLE IF EXISTS Content;
DROP TABLE IF EXISTS File;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--;;
CREATE TABLE Account (
  account_id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  email TEXT UNIQUE,
  lastlogin TEXT,
  name TEXT,
  role INTEGER,
  username TEXT
);
--;;
CREATE TABLE TWord (
  tword_id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  account_id TEXT REFERENCES Account(account_id),
  tword TEXT,
  src_lang TEXT,
  dest_lang TEXT
);
--;;
CREATE TABLE Collection (
  collection_id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  name TEXT,
  published BOOLEAN,
  archived BOOLEAN
);
--;;
CREATE TABLE Course (
  course_id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  department TEXT,
  catalog_number TEXT,
  section_number TEXT
);
--;;
CREATE TABLE Content (
  content_id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
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
  metadata TEXT
);
--;;
CREATE TABLE File (
  file_id TEXT PRIMARY KEY DEFAULT uuid_generate_v4 (),
  filepath TEXT,
  mime TEXT,
  metadata TEXT
);
--;;
CREATE TABLE Account_Collection (
  account_id TEXT REFERENCES Account(account_id),
  collection_id TEXT REFERENCES Collection(collection_id),
  role TEXT,
  PRIMARY KEY (account_id, collection_id)
);
--;;
CREATE TABLE Collection_Course (
  collection_id TEXT REFERENCES Collection(collection_id),
  course_id TEXT REFERENCES Course(course_id),
  PRIMARY KEY (collection_id, course_id)
);
--;;
CREATE TABLE Content_File (
  content_id TEXT REFERENCES Content(content_id),
  file_id TEXT REFERENCES File(file_id),
  PRIMARY KEY (content_id, file_id)
);
