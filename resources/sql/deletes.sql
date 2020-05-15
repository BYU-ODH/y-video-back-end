/* DELETE BASE TABLE STATEMENTS */

-- :name delete-collection :? :n
-- :doc deletes collection with given collection_id
DELETE FROM Collection
WHERE collection_id = :collection_id

/* DELTE MANY-TO-MANY TABLE STATEMENTS */

-- :name delete-account-collection :? :n
-- :doc deletes connection between account and collection
DELETE FROM Account_Collection
WHERE account_id = :account_id
AND collection_id = :collection_id

-- :name delete-collection-course :? :n
-- :doc deletes connection between collection and course
DELETE FROM Collection_Course
WHERE collection_id = :collection_id
AND course_id = :course_id

-- :name delete-collection-content :? :n
-- :doc deletes connection between collection and content
DELETE FROM Collection_Content
WHERE collection_id = :collection_id
AND content_id = :content_id

-- :name delete-content-file :? :n
-- :doc deletes connection between content and file
DELETE FROM Content_File
WHERE content_id = :content_id
AND file_id = :file_id
