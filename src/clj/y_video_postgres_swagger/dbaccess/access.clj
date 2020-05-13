(ns y-video-postgres-swagger.dbaccess.access
  (:require [y-video-postgres-swagger.db.core :as db]))


(defn get_collection
  "Retrieve collection by given id"
  [collection_id]
  ;; {:collection_id "id" :name "name" :published false :archived false}
  (db/get-collection {:id collection_id}))
