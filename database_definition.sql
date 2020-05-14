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

CREATE TABLE Account (
  account_id INTEGER PRIMARY KEY,
  email TEXT UNIQUE,
  lastLogin TEXT,
  name TEXT,
  role INTEGER,
  username TEXT
  /*
    DONE - assoc_collections
    DONE - words
  */
);

CREATE TABLE TWord (
  word_id INTEGER PRIMARY KEY,
  account_id INTEGER REFERENCES Account(account_id),
  this_word TEXT,
  src_lang TEXT,
  dest_lang TEXT
);

CREATE TABLE Collection (
  collection_id INTEGER PRIMARY KEY,
  name TEXT,
  published BOOLEAN,
  archived BOOLEAN
  /*
    DONE - assoc_courses
    DONE - assoc_accounts
    DONE - assoc_content
  */
);

CREATE TABLE Account_Collection (
  account_id INTEGER REFERENCES Account(account_id),
  collection_id INTEGER REFERENCES Collection(collection_id),
  role TEXT,
  PRIMARY KEY (account_id, collection_id)
);

CREATE TABLE Course (
  course_id INTEGER PRIMARY KEY,
  department TEXT,
  catalog_number TEXT,
  section_number TEXT
);

CREATE TABLE Collection_Course (
  collection_id INTEGER REFERENCES Collection(collection_id),
  course_id INTEGER REFERENCES Course(course_id),
  PRIMARY KEY (collection_id, course_id)
);

CREATE TABLE Content (
  content_id INTEGER PRIMARY KEY,
  collection_id INTEGER,
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
  /*
  files
  */
);

CREATE TABLE Collection_Content (
  collection_id INTEGER REFERENCES Collection(collection_id),
  content_id INTEGER REFERENCES Content(content_id),
  allow_definitions BOOLEAN,
  allow_notes BOOLEAN,
  allow_captions BOOLEAN,
  PRIMARY KEY (collection_id, content_id)
);

CREATE TABLE File (
  file_id INTEGER PRIMARY KEY,
  filepath TEXT,
  mime TEXT,
  metadata TEXT
);

CREATE TABLE Content_File (
  content_id INTEGER REFERENCES Content(content_id),
  file_id INTEGER REFERENCES File(file_id),
  PRIMARY KEY (content_id, file_id)
);
