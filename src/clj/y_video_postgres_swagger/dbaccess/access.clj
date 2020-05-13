(ns y-video-postgres-swagger.dbaccess.access
  (:require [y-video-postgres-swagger.db.core :as db]))



(defn get_collection
  "Retrieve collection by given id"
  [collection_id]
  ;; {:collection_id "id" :name "name" :published false :archived false}
  (db/get-collection {:id collection_id}))

(defn add_collection
  "Add collection with given values"
  [id name published archived]
  (try
    (def num_added (db/add-collection! {:id id :name name :published published :archived archived}))
    {:message (str num_added " collection added")}
  (catch Exception e
    {:message (.getCause e)}
    )))
