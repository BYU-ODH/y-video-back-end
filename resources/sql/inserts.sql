/* INSERT INTO BASE TABLES STATEMENTS */

-- :name add-account! :<! :raw
-- :doc creates a new account, returns id
INSERT INTO Account
(email, lastlogin, name, role, username)
VALUES (:email, :lastlogin, :name, :role, :username)
RETURNING id

-- :name add-tword! :<! :raw
-- :doc creates a new tword, returns id
INSERT INTO TWord
(id, tword, src_lang, dest_lang)
VALUES (:id, :tword, :src_lang, :dest_lang)
RETURNING id

-- :name add-collection! :<! :raw
-- :doc creates a new collection, returns id
INSERT INTO Collection
(name, published, archived)
VALUES (:name, :published, :archived)
RETURNING id

-- :name add-course! :<! :raw
-- :doc creates a new course, returns id
INSERT INTO Course
(department, catalog_number, section_number)
VALUES (:department, :catalog_number, :section_number)
RETURNING id

-- :name add-content! :<! :raw
-- :doc creates a new content, returns id
INSERT INTO Content
(id, name, type, requester_email, thumbnail, copyrighted,
  physical_copy_exists, full_video, published, allow_definitions,
  allow_notes, allow_captions, date_validated, metadata)
VALUES (:id, :name, :type, :requester_email, :thumbnail,
  :copyrighted, :physical_copy_exists, :full_video, :published,
  :allow_definitions, :allow_notes, :allow_captions,
  :date_validated, :metadata)
RETURNING id

-- :name add-file! :<! :raw
-- :doc creates a new file, returns id
INSERT INTO File
(filepath, mime, metadata)
VALUES (:filepath, :mime, :metadata)
RETURNING id

/* INSERT INTO MANY-TO-MANY TABLES STATEMENTS */

-- :name add-account-collection! :! :n
-- :doc connects account and collection
INSERT INTO Account_Collection
(id, id, role)
VALUES (:id, :id, :role)

-- :name add-collection-course! :! :n
-- :doc connects collection and course
INSERT INTO Collection_Course
(id, id)
VALUES (:id, :id)

-- :name add-collection-content! :! :n
-- :doc connects collection and content
INSERT INTO Collection_Content
(id, id, allow_definitions, allow_notes, allow_captions)
VALUES (:id, :id, :allow_definitions, :allow_notes, :allow_captions)

-- :name add-content-file! :! :n
-- :doc connects content and file
INSERT INTO Content_File
(id, id)
VALUES (:id, :id)
