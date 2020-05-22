/* DELETE BASE TABLE STATEMENTS */

-- :name delete-all :? :n
-- :doc deletes contents from all tables
DELETE FROM user_collections;
DELETE FROM collection_courses;
DELETE FROM content_files;
DELETE FROM files;
DELETE FROM words;
DELETE FROM contents;
DELETE FROM courses;
DELETE FROM collections;
DELETE FROM users;


-- :name delete-user :? :n
-- :doc deletes user with given id
DELETE FROM users
WHERE id = :id

-- :name delete-word :? :n
-- :doc deletes word with given id
DELETE FROM words
WHERE id = :id

-- :name delete-collection :? :n
-- :doc deletes collection with given id
DELETE FROM collections
WHERE id = :id

-- :name delete-course :? :n
-- :doc deletes course with given id
DELETE FROM courses
WHERE id = :id

-- :name delete-content :? :n
-- :doc deletes content with given id
DELETE FROM contents
WHERE id = :id

-- :name delete-file :? :n
-- :doc deletes file with given id
DELETE FROM files
WHERE id = :id

/* DELETE MANY-TO-MANY TABLE STATEMENTS */

-- :name delete-user-collection :? :n
-- :doc deletes connection between user and collection
DELETE FROM user_collections
WHERE user_id = :user_id
AND collection_id = :collection_id

-- :name delete-collection-course :? :n
-- :doc deletes connection between collection and course
DELETE FROM collection_courses
WHERE collection_id = :collection_id
AND course_id = :course_id

-- :name delete-content-file :? :n
-- :doc deletes connection between content and file
DELETE FROM content_files
WHERE content_id = :content_id
AND file_id = :file_id
