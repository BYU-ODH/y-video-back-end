/* DELETE BASE TABLE STATEMENTS */

-- :name delete-account :? :n
-- :doc deletes account with given id
DELETE FROM Account
WHERE id = :id

-- :name delete-tword :? :n
-- :doc deletes tword with given id
DELETE FROM TWord
WHERE id = :id

-- :name delete-collection :? :n
-- :doc deletes collection with given id
DELETE FROM Collection
WHERE id = :id

-- :name delete-course :? :n
-- :doc deletes course with given id
DELETE FROM Course
WHERE id = :id

-- :name delete-content :? :n
-- :doc deletes content with given id
DELETE FROM Content
WHERE id = :id

-- :name delete-file :? :n
-- :doc deletes file with given id
DELETE FROM File
WHERE id = :id

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

-- :name delete-content-file :? :n
-- :doc deletes connection between content and file
DELETE FROM Content_File
WHERE content_id = :content_id
AND file_id = :file_id
