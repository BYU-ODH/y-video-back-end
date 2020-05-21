(ns y-video-postgres-swagger.dbaccess.access
  (:require [y-video-postgres-swagger.db.core :as db]))

(declare associate_user_with_collection)

(defn to_uuid
  [text_in]
  (str "#uuid " text_in))


(defn get_collection
  "Retrieve collection with given id"
  [collection_id]
  ; get collection info
  (def base-collection (db/get-collection {:collection_id collection_id}))
  ; add in assoc_users, assoc_courses, and assoc_content
  (into base-collection [{:assoc_users (db/get-users-by-collection {:collection_id collection_id})
                          :assoc_courses (db/get-courses-by-collection {:collection_id collection_id})
                          :assoc_content (db/get-contents-by-collection {:collection_id collection_id})}]))

(defn get_collections
  "Retrieve all collections available to given user_id"
  [user_id]
  (map #(get_collection (get % :collection_id)) (db/get-collections-by-user {:user_id user_id})))

(defn add_collection
  "Add collection with current user as owner"
  [current_user_id name published archived]
  (try
    (let [collection_id (:collection_id (get (db/add-collection! {:collection_name name :published published :archived archived}) 0))]
      (associate_user_with_collection current_user_id collection_id 0)
      {:message (str "1 collection added with ID: " collection_id)})
   (catch Exception e
     {:message (.getCause e)})))


(defn add_collection_old
  "Add collection with given values, adds associated users, contents, courses"
  [current_user_id name published archived assoc_users assoc_content assoc_courses]
  (try
    (def collection_id (:collection_id (get (db/add-collection! {:collection_name name :published published :archived archived}) 0)))
    (db/add-user-collection! {:user_id current_user_id :collection_id collection_id :account_role 0})
    (get_collection collection_id)
   (catch Exception e
     {:message (.getCause e)})))


(defn get_user
  "Retrieve collection with given id"
  [id]
  (update (db/get-user {:id id}) :id str))

(defn associate_user_with_collection
  "Adds collection to user's assoc_collections"
  [user_id collection_id account_role]
  (db/add-user-collection! {:user_id user_id :collection_id collection_id
                               :account_role account_role}))
(defn add_user
  "Adds new user to database"
  [user_without_id]
  (str (:id (get (db/add-user! user_without_id) 0))))
