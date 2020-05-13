-- :name get-collection :? :1
-- :doc retrieves collection with given id
SELECT * FROM collections
WHERE id = :id

-- :name add-collection! :! :n
-- :doc creates a new collection record
INSERT INTO collections
(id, name, published, archived)
VALUES (:id, :name, :published, :archived)



-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id
