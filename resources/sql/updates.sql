/* UPDATE STATEMENTS */

-- :name update-user :! :n
-- :doc updates user
UPDATE users
SET email = :email, last_login = :last_login, account_name = :account_name, account_role = :account_role,
username = :username
WHERE id = :id

-- :name update-word :! :n
-- :doc updates word
UPDATE words
SET word = :word, src_lang = :src_lang, dest_lang = :dest_lang
WHERE id = :id

-- :name update-collection :! :n
-- :doc updates collection
UPDATE collections
SET collection_name = :collection_name, published = :published, archived = :archived
WHERE id = :id

-- :name update-course :! :n
-- :doc updates course
UPDATE courses
SET department = :department, catalog_number = :catalog_number,
section_number = :section_number
WHERE id = :id

-- :name update-content :! :n
-- :doc updates content
UPDATE contents
SET id = :id, content_name = :content_name, content_type = :content_type,
  requester_email = :requester_email, thumbnail = :thumbnail, filters = :filters,
  copyrighted = :copyrighted, physical_copy_exists = :physical_copy_exists,
  full_video = :full_video, published = :published,
  allow_definitions = :allow_definitions, allow_notes = :allow_notes,
  allow_captions = :allow_captions, date_validated = :date_validated,
  views = :views, metadata = :metadata
WHERE id = :id

-- :name update-file :! :n
-- :doc updates file
UPDATE files
SET filepath = :filepath, mime = :mime, metadata = :metadata
WHERE id = :id

-- :name add-view-to-content :! :n
-- :doc adds view to content
UPDATE contents
SET views = views + 1
WHERE id = :id
