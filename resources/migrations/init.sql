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
  id TEXT REFERENCES Account(id),
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
  id TEXT,
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
  id TEXT REFERENCES Account(id) ON DELETE CASCADE,
  id TEXT REFERENCES Collection(id) ON DELETE CASCADE,
  role TEXT,
  PRIMARY KEY (id, id)
);
--;;
CREATE TABLE Collection_Course (
  id TEXT REFERENCES Collection(id),
  id TEXT REFERENCES Course(id),
  PRIMARY KEY (id, id)
);
--;;
CREATE TABLE Content_File (
  id TEXT REFERENCES Content(id),
  id TEXT REFERENCES File(id),
  PRIMARY KEY (id, id)
);
