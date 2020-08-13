(ns y-video-back.db.collections
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :collections))
(def READ  (partial db/READ :collections-undeleted))
(def READ-ALL  (partial db/READ :collections))
(def UPDATE (partial db/UPDATE :collections))
(def DELETE (partial db/mark-deleted :collections))
(def CLONE (partial db/CLONE :collections))
(def PERMANENT-DELETE (partial db/DELETE :collections))
(def READ-ANNOTATIONS (partial db/read-all-where :contents :collection-id))
(def READ-ALL-BY-NAME-OWNER (partial db/read-where-and :collections-undeleted [:collection-name :owner]))
(defn EXISTS? [id] (not (nil? (db/READ :collections-undeleted id))))
(defn EXISTS-NAME-OWNER? [name owner] (seq (db/read-where-and :collections-undeleted [:collection-name :owner] [name owner])))
(def READ-ALL-BY-OWNER (partial db/read-where-and :collections-undeleted [:owner]))
(defn READ-PUBLIC
  "Read by id, restrict to public results only"
  [id]
  (let [res (READ id)]
    (if-not (:public res)
      nil
      res)))
(defn READ-ALL-PUBLIC
  "Read all public results"
  []
  (db/read-all-where :collections-undeleted :public true))
