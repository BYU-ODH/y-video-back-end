/* SELECT BY ID STATEMENTS */

-- :name get-account :? :1
-- :doc retrieves account with given user_id
SELECT * FROM Account
WHERE user_id = :user_id

-- :name get-tword :? :1
-- :doc retreives tword with given word_id
SELECT * FROM TWord
WHERE word_id = :word_id

-- :name get-collection :? :1
-- :doc retreives collection with given collection_id
SELECT * FROM Collection
WHERE collection_id = :collection_id

-- :name get-course :? :1
-- :doc retreives course with given course_id
SELECT * FROM Course
WHERE course_id = :course_id

-- :name get-content :? :1
-- :doc retreives content with given content_id
SELECT * FROM Content
WHERE content_id = :content_id

-- :name get-file :? :1
-- :doc retreives file with given file_id
SELECT * FROM File
WHERE file_id = :file_id


/* SELECT FROM MANY-TO-MANY TABLES STATEMENTS */

-- :name get-collections-by-account :? :*
-- :doc retrieves all collections connected to given account
SELECT c.*
FROM Account as a
INNER JOIN Account_Collection as ac
  ON a.user_id = ac.user_id
INNER JOIN Collection as c
  ON ac.collection_id = c.collection_id
WHERE a.user_id = :user_id

-- :name get-accounts-by-collection :? :*
-- :doc retrieves all accounts connected to given collection
SELECT a.*
FROM Account as a
INNER JOIN Account_Collection as ac
  ON a.user_id = ac.user_id
INNER JOIN Collection as c
  ON ac.collection_id = c.collection_id
WHERE c.collection_id = :collection_id

-- :name get-collections-by-course :? :*
-- :doc retreives all collections connected to given course
SELECT cll.*
FROM Course as crs
INNER JOIN Collection_Course as cllcrs
  ON crs.course_id = cllcrs.course_id
INNER JOIN Collection as cll
  ON cllcrs.collection_id = cll.collection_id
WHERE crs.course_id = :course_id

-- :name get-courses-by-collection :? :*
-- :doc retreives all courses connected to given collection
SELECT crs.*
FROM Course as crs
INNER JOIN Collection_Course as cllcrs
  ON crs.course_id = cllcrs.course_id
INNER JOIN Collection as cll
  ON cllcrs.collection_id = cll.collection_id
WHERE cll.collection_id = :collection_id

-- :name get-collections-by-content :? :*
-- :doc retreives all collections connected to given content
SELECT cll.*
FROM Content as cnt
INNER JOIN Collection_Content as cllcnt
  ON cnt.content_id = cllcnt.content_id
INNER JOIN Collection as cll
  ON cllcnt.collection_id = cll.collection_id
WHERE cnt.content_id = :content_id

-- :name get-contents-by-collection :? :*
-- :doc retreives all contents connected to given collection
SELECT cnt.*
FROM Content as cnt
INNER JOIN Collection_Content as cllcnt
  ON cnt.content_id = cllcnt.content_id
INNER JOIN Collection as cll
  ON cllcnt.collection_id = cll.collection_id
WHERE cll.collection_id = :collection_id

-- :name get-contents-by-file :? :*
-- :doc retreives all contents connected to given file
SELECT cnt.*
FROM Content as cnt
INNER JOIN Content_File as cntf
  ON cnt.content_id = cntf.content_id
INNER JOIN File as f
  ON cntf.file_id = f.file_id
WHERE f.file_id = :file_id

-- :name get-files-by-content :? :*
-- :doc retreives all files connected to given content
SELECT f.*
FROM Content as cnt
INNER JOIN Content_File as cntf
  ON cnt.content_id = cntf.content_id
INNER JOIN File as f
  ON cntf.file_id = f.file_id
WHERE cnt.content_id = :content_id
