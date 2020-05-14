CREATE TABLE user
(
  id INTEGER PRIMARY KEY,
  email TEXT UNIQUE,
  lastLogin TEXT,
  name TEXT,
  role INTEGER,
  username TEXT,
  assoc_collections {id INTEGER,
                     name TEXT,
                     role INTEGER}[]
);

CREATE TABLE collection
(
  id TEXT PRIMARY KEY,
  name TEXT,
  published BOOLEAN,
  archived BOOLEAN
);

CREATE TABLE course
(
  id INTEGER PRIMARY KEY,
  department TEXT,
  catalog_number TEXT,
  section_number TEXT
);

CREATE TABLE content
(
  id INTEGER PRIMARY KEY,
  collection_id INTEGER,
  name TEXT,
  type TEXT,
  requester_email TEXT,
  thumbnail TEXT,
  copyrighted BOOLEAN,
  physical_copy_exists BOOLEAN,
  full_video BOOLEAN,
  published BOOLEAN,
  data_validated TEXT,
  metadata TEXT
);

CREATE TABLE file
(
  id INTEGER PRIMARY KEY,
  path TEXT,
  mime TEXT,
  metadata TEXT
);
