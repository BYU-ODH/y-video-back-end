/* UPDATE STATEMENTS */

-- :name update-account :! :n
-- :doc updates account
UPDATE Account
SET email = :email, lastlogin = :lastlogin, name = :name, role = :role, username = :username
WHERE account_id = :account_id

-- :name update-tword :! :n
-- :doc updates tword
UPDATE TWord
SET tword = :tword, src_lang = :src_lang, dest_lang = :dest_lang
WHERE tword_id = :tword_id

-- :name update-collection :! :n
-- :doc updates collection
UPDATE Collection
SET name = :name, published = :published, archived = :archived
WHERE collection_id = :collection_id

-- :name update-course :! :n
-- :doc updates course
UPDATE Course
SET department = :department, catalog_number = :catalog_number, section_number = :section_number
WHERE course_id = :course_id

-- :name update-content :! :n
-- :doc updates content
UPDATE Content
SET collection_id = :collection_id, name = :name, type = :type,
requester_email = :requester_email, thumbnail = :thumbnail,
copyrighted = :copyrighted, physical_copy_exists = :physical_copy_exists,
full_video = :full_video, published = :published,
allow_definitions = :allow_definitions, allow_notes = :allow_notes,
allow_captions = :allow_captions, date_validated = :date_validated,
metadata = :metadata
WHERE content_id = :content_id

-- :name update-file :! :n
-- :doc updates file
UPDATE File
SET filepath = :filepath, mime = :mime, metadata = :metadata
WHERE file_id = :file_id
