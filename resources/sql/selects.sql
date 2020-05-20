/* SELECT BY ID STATEMENTS */

-- :name get-account :? :1
-- :doc retrieves account with given id
SELECT * FROM Account
WHERE id = :id

-- :name get-tword :? :1
-- :doc retreives tword with given id
SELECT * FROM TWord
WHERE id = :id

-- :name get-collection :? :1
-- :doc retreives collection with given id
SELECT * FROM Collection
WHERE id = :id

-- :name get-course :? :1
-- :doc retreives course with given id
SELECT * FROM Course
WHERE id = :id

-- :name get-content :? :1
-- :doc retreives content with given id
SELECT * FROM Content
WHERE id = :id

-- :name get-file :? :1
-- :doc retreives file with given id
SELECT * FROM File
WHERE id = :id


/* SELECT FROM MANY-TO-MANY TABLES STATEMENTS */

-- :name get-collections-by-account :? :*
-- :doc retrieves all collections connected to given account
SELECT c.*
FROM Account as a
INNER JOIN Account_Collection as ac
  ON a.id = ac.id
INNER JOIN Collection as c
  ON ac.id = c.id
WHERE a.id = :id

-- :name get-accounts-by-collection :? :*
-- :doc retrieves all accounts connected to given collection
SELECT a.*
FROM Account as a
INNER JOIN Account_Collection as ac
  ON a.id = ac.id
INNER JOIN Collection as c
  ON ac.id = c.id
WHERE c.id = :id

-- :name get-collections-by-course :? :*
-- :doc retreives all collections connected to given course
SELECT cll.*
FROM Course as crs
INNER JOIN Collection_Course as cllcrs
  ON crs.id = cllcrs.id
INNER JOIN Collection as cll
  ON cllcrs.id = cll.id
WHERE crs.id = :id

-- :name get-courses-by-collection :? :*
-- :doc retreives all courses connected to given collection
SELECT crs.*
FROM Course as crs
INNER JOIN Collection_Course as cllcrs
  ON crs.id = cllcrs.id
INNER JOIN Collection as cll
  ON cllcrs.id = cll.id
WHERE cll.id = :id

-- :name get-contents-by-collection :? :*
-- :doc retrieves all contents connected to given collection
SELECT * FROM Content
WHERE id = :id


-- :name get-contents-by-file :? :*
-- :doc retreives all contents connected to given file
SELECT cnt.*
FROM Content as cnt
INNER JOIN Content_File as cntf
  ON cnt.id = cntf.id
INNER JOIN File as f
  ON cntf.id = f.id
WHERE f.id = :id

-- :name get-files-by-content :? :*
-- :doc retreives all files connected to given content
SELECT f.*
FROM Content as cnt
INNER JOIN Content_File as cntf
  ON cnt.id = cntf.id
INNER JOIN File as f
  ON cntf.id = f.id
WHERE cnt.id = :id
