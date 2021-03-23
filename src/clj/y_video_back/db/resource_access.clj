(ns y-video-back.db.resource-access
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :resource-access))
(def READ  (partial db/READ :resource-access-undeleted))
(def READ-ALL  (partial db/READ :resource-access))
(def UPDATE (partial db/UPDATE :resource-access))
(def DELETE (partial db/mark-deleted :resource-access))
(def CLONE (partial db/CLONE :resource-access))
(def PERMANENT-DELETE (partial db/DELETE :resource-access))
(defn EXISTS? [id] (not (nil? (db/READ :resource-access-undeleted id))))
(defn EXISTS-USERNAME-RESOURCE? [username resource-id] (not (= 0 (count (db/read-where-and
                                                                          :resource-access-undeleted
                                                                          [:username :resource-id]
                                                                          [username resource-id])))))
