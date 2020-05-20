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
WHERE id = :id
AND id = :id

-- :name delete-collection-course :? :n
-- :doc deletes connection between collection and course
DELETE FROM Collection_Course
WHERE id = :id
AND id = :id

-- :name delete-collection-content :? :n
-- :doc deletes connection between collection and content
DELETE FROM Collection_Content
WHERE id = :id
AND id = :id

-- :name delete-content-file :? :n
-- :doc deletes connection between content and file
DELETE FROM Content_File
WHERE id = :id
AND id = :id
