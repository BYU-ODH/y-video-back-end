CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--;;
CREATE TABLE Account (
  account_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
  email TEXT UNIQUE,
  lastlogin TEXT,
  name TEXT,
  role INTEGER,
  username TEXT
);
--;;
CREATE TABLE TWord (
  tword_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
  account_id UUID REFERENCES Account(account_id),
  tword TEXT,
  src_lang TEXT,
  dest_lang TEXT
);
--;;
CREATE TABLE Collection (
  collection_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
  name TEXT,
  published BOOLEAN,
  archived BOOLEAN
);
--;;
CREATE TABLE Course (
  course_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
  department TEXT,
  catalog_number TEXT,
  section_number TEXT
);
--;;
CREATE TABLE Content (
  content_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
  collection_id UUID,
  name TEXT,
  type TEXT,
  requester_email TEXT,
  thumbnail TEXT,
  copyrighted BOOLEAN,
  physical_copy_exists BOOLEAN,
  full_video BOOLEAN,
  published BOOLEAN,
  date_validated TEXT,
  metadata TEXT
);
--;;
CREATE TABLE File (
  file_id UUID PRIMARY KEY DEFAULT uuid_generate_v4 (),
  filepath TEXT,
  mime TEXT,
  metadata TEXT
);
--;;
CREATE TABLE Account_Collection (
  account_id UUID REFERENCES Account(account_id),
  collection_id UUID REFERENCES Collection(collection_id),
  role TEXT,
  PRIMARY KEY (account_id, collection_id)
);
--;;
CREATE TABLE Collection_Course (
  collection_id UUID REFERENCES Collection(collection_id),
  course_id UUID REFERENCES Course(course_id),
  PRIMARY KEY (collection_id, course_id)
);
--;;
CREATE TABLE Collection_Content (
  collection_id UUID REFERENCES Collection(collection_id),
  content_id UUID REFERENCES Content(content_id),
  allow_definitions BOOLEAN,
  allow_notes BOOLEAN,
  allow_captions BOOLEAN,
  PRIMARY KEY (collection_id, content_id)
);
--;;
CREATE TABLE Content_File (
  content_id UUID REFERENCES Content(content_id),
  file_id UUID REFERENCES File(file_id),
  PRIMARY KEY (content_id, file_id)
);
