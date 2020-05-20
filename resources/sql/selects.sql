/* SELECT BY ID STATEMENTS */

-- :name get-user :? :1
-- :doc retrieves user with given id
SELECT id::text FROM users
WHERE id::text = :id

-- :name get-word :? :1
-- :doc retreives word with given id
SELECT * FROM words
WHERE id = :id

-- :name get-collection :? :1
-- :doc retreives collection with given id
SELECT * FROM collections
WHERE id = :id

-- :name get-course :? :1
-- :doc retreives course with given id
SELECT * FROM courses
WHERE id = :id

-- :name get-content :? :1
-- :doc retreives content with given id
SELECT * FROM contents
WHERE id = :id

-- :name get-file :? :1
-- :doc retreives file with given id
SELECT * FROM files
WHERE id = :id


/* SELECT FROM MANY-TO-MANY TABLES STATEMENTS */

-- :name get-collections-by-user :? :*
-- :doc retrieves all collections connected to given user
SELECT c.*
FROM users as a
INNER JOIN user_collections as ac
  ON a.id = ac.user_id
INNER JOIN collections as c
  ON ac.collection_id = c.id
WHERE a.id = :id

-- :name get-users-by-collection :? :*
-- :doc retrieves all users connected to given collection
SELECT a.*
FROM users as a
INNER JOIN user_collections as ac
  ON a.id = ac.user_id
INNER JOIN collections as c
  ON ac.collection_id = c.id
WHERE c.id = :id

-- :name get-collections-by-course :? :*
-- :doc retreives all collections connected to given course
SELECT cll.*
FROM courses as crs
INNER JOIN collection_courses as cllcrs
  ON crs.id = cllcrs.course_id
INNER JOIN collections as cll
  ON cllcrs.collection_id = cll.id
WHERE crs.id = :id

-- :name get-courses-by-collection :? :*
-- :doc retreives all courses connected to given collection
SELECT crs.*
FROM courses as crs
INNER JOIN collection_courses as cllcrs
  ON crs.id = cllcrs.course_id
INNER JOIN collections as cll
  ON cllcrs.collection_id = cll.id
WHERE cll.id = :id

-- :name get-contents-by-collection :? :*
-- :doc retrieves all contents connected to given collection
SELECT * FROM contents
WHERE id = :id


-- :name get-contents-by-file :? :*
-- :doc retreives all contents connected to given file
SELECT cnt.*
FROM contents as cnt
INNER JOIN content_files as cntf
  ON cnt.id = cntf.content_id
INNER JOIN files as f
  ON cntf.file_id = f.id
WHERE f.id = :id

-- :name get-files-by-content :? :*
-- :doc retreives all files connected to given content
SELECT f.*
FROM contents as cnt
INNER JOIN content_files as cntf
  ON cnt.id = cntf.content_id
INNER JOIN files as f
  ON cntf.file_id = f.id
WHERE cnt.id = :id
