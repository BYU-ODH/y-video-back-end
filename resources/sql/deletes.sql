/* DELETE BASE TABLE STATEMENTS */

-- :name delete-account :? :n
-- :doc deletes account with given account_id
DELETE FROM Account
WHERE account_id = :account_id

-- :name delete-tword :? :n
-- :doc deletes tword with given tword_id
DELETE FROM TWord
WHERE tword_id = :tword_id

-- :name delete-collection :? :n
-- :doc deletes collection with given collection_id
DELETE FROM Collection
WHERE collection_id = :collection_id

-- :name delete-course :? :n
-- :doc deletes course with given course_id
DELETE FROM Course
WHERE course_id = :course_id

-- :name delete-content :? :n
-- :doc deletes content with given content_id
DELETE FROM Content
WHERE content_id = :content_id

-- :name delete-file :? :n
-- :doc deletes file with given file_id
DELETE FROM File
WHERE file_id = :file_id

/* DELETE MANY-TO-MANY TABLE STATEMENTS */

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
