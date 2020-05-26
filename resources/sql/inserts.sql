/* INSERT INTO BASE TABLES STATEMENTS */

-- :name add-user! :<! :raw
-- :doc creates a new user, returns id
INSERT INTO users
(email, last_login, account_name, account_role, username)
VALUES (:email, :last_login, :account_name, :account_role, :username)
RETURNING id

-- :name add-word! :<! :raw
-- :doc creates a new word, returns id
INSERT INTO words
(user_id, word, src_lang, dest_lang)
VALUES (:user_id, :word, :src_lang, :dest_lang)
RETURNING id

-- :name add-collection! :<! :raw
-- :doc creates a new collection, returns id
INSERT INTO collections
(collection_name, published, archived)
VALUES (:collection_name, :published, :archived)
RETURNING id

-- :name add-course! :<! :raw
-- :doc creates a new course, returns id
INSERT INTO courses
(department, catalog_number, section_number)
VALUES (:department, :catalog_number, :section_number)
RETURNING id

-- :name add-content! :<! :raw
-- :doc creates a new content, returns id
INSERT INTO contents
(content_name, content_type, requester_email, thumbnail, filters,
  copyrighted, physical_copy_exists, full_video, published, allow_definitions,
  allow_notes, allow_captions, date_validated, views, metadata)
VALUES (:content_name, :content_type, :requester_email,
  :thumbnail, :filters, :copyrighted, :physical_copy_exists, :full_video,
  :published, :allow_definitions, :allow_notes, :allow_captions,
  :date_validated, :views, :metadata)
RETURNING id

-- :name add-file! :<! :raw
-- :doc creates a new file, returns id
INSERT INTO files
(filepath, mime, metadata)
VALUES (:filepath, :mime, :metadata)
RETURNING id

/* INSERT INTO MANY-TO-MANY TABLES STATEMENTS */

-- :name add-user-collection! :! :n
-- :doc connects user and collection
INSERT INTO user_collections
(user_id, collection_id, account_role)
VALUES (:user_id, :collection_id, :account_role)

-- :name add-collection-course! :! :n
-- :doc connects collection and course
INSERT INTO collection_courses
(collection_id, course_id)
VALUES (:collection_id, :course_id)

-- :name add-content-file! :! :n
-- :doc connects content and file
INSERT INTO content_files
(content_id, file_id)
VALUES (:content_id, :file_id)
