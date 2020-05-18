(ns y-video-postgres-swagger.dbaccess.access
  (:require [y-video-postgres-swagger.db.core :as db]))



(defn get_collections
  "Retrieve all collections available to given user_id"
  [user_id]
  (map #(get_collection (get % :collection_id)) (db/get-collections-by-account {:user_id user_id})))

(defn get_collection
  "Retrieve collection with given id"
  [collection_id]
  ; get collection info
  (def base-collection (db/get-collection {:collection_id collection_id}))
  ; add in assoc_users, assoc_courses, and assoc_content
  (into base-collection [{:assoc_users (db/get-accounts-by-collection {:collection_id collection_id})
                          :assoc_courses (db/get-courses-by-collection {:collection_id collection_id})
                          :assoc_content (db/get-contents-by-collection {:collection_id collection_id})}]))

(defn add_collection
  "Add collection with given values"
  [current_user_id name published archived]
  (try
    (def res (db/add-collection! {:name name :published published :archived archived}))
    (db/add-account-collection! {:user_id current_user_id :collection_id (:collection_id (get res 0)) :role 0})
    (get_collection (:collection_id (get res 0)))
   (catch Exception e
     {:message (.getCause e)})))


(defn get_user
  "Retrieve collection with given id"
  [user_id]
  (db/get-account {:user_id user_id}))
