(ns y-video-postgres-swagger.dbaccess.access
  (:require [y-video-postgres-swagger.db.core :as db]))



(defn get_collections
  "Retrieve all collections available to given user_id"
  [user_id]
  ;; {:collection_id "id" :name "name" :published false :archived false}
  (db/get-collections-by-account {:account_id user_id}))

(defn add_collection
  "Add collection with given values"
  [id name published archived]
  (try
    (def num_added (db/add-collection! {:id id :name name :published published :archived archived}))
    {:message (str num_added " collection added")}
  (catch Exception e
    {:message (.getCause e)}
    )))
