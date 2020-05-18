/* INSERT INTO BASE TABLES STATEMENTS */

-- :name add-account! :<! :raw
-- :doc creates a new account, returns user_id
INSERT INTO Account
(email, lastlogin, name, role, username)
VALUES (:email, :lastlogin, :name, :role, :username)
RETURNING user_id

-- :name add-tword! :<! :raw
-- :doc creates a new tword, returns word_id
INSERT INTO TWord
(user_id, tword, src_lang, dest_lang)
VALUES (:user_id, :tword, :src_lang, :dest_lang)
RETURNING word_id

-- :name add-collection! :<! :raw
-- :doc creates a new collection, returns collection_id
INSERT INTO Collection
(name, published, archived)
VALUES (:name, :published, :archived)
RETURNING collection_id

-- :name add-course! :<! :raw
-- :doc creates a new course, returns course_id
INSERT INTO Course
(department, catalog_number, section_number)
VALUES (:department, :catalog_number, :section_number)
RETURNING course_id

-- :name add-content! :<! :raw
-- :doc creates a new content, returns content_id
INSERT INTO Content
(collection_id, name, type, requester_email, thumbnail, copyrighted,
  physical_copy_exists, full_video, published, allow_definitions,
  allow_notes, allow_captions, date_validated, metadata)
VALUES (:collection_id, :name, :type, :requester_email, :thumbnail,
  :copyrighted, :physical_copy_exists, :full_video, :published,
  :allow_definitions, :allow_notes, :allow_captions,
  :date_validated, :metadata)
RETURNING content_id

-- :name add-file! :<! :raw
-- :doc creates a new file, returns file_id
INSERT INTO File
(filepath, mime, metadata)
VALUES (:filepath, :mime, :metadata)
RETURNING file_id

/* INSERT INTO MANY-TO-MANY TABLES STATEMENTS */

-- :name add-account-collection! :! :n
-- :doc connects account and collection
INSERT INTO Account_Collection
(user_id, collection_id, role)
VALUES (:user_id, :collection_id, :role)

-- :name add-collection-course! :! :n
-- :doc connects collection and course
INSERT INTO Collection_Course
(collection_id, course_id)
VALUES (:collection_id, :course_id)

-- :name add-collection-content! :! :n
-- :doc connects collection and content
INSERT INTO Collection_Content
(collection_id, content_id, allow_definitions, allow_notes, allow_captions)
VALUES (:collection_id, :content_id, :allow_definitions, :allow_notes, :allow_captions)

-- :name add-content-file! :! :n
-- :doc connects content and file
INSERT INTO Content_File
(content_id, file_id)
VALUES (:content_id, :file_id)
