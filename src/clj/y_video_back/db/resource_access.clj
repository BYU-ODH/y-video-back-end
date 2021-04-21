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
(defn UPDATE-LAST-VERIFIED [id]
  (db/update-resource-access-last-verified (str id)))
(defn EXISTS-USERNAME-RESOURCE?
  [username resource-id]
  (not (= 0 (count (db/read-where-and
                     :resource-access-undeleted
                     [:username :resource-id]
                     [username resource-id])))))

(defn READ-BY-USERNAME-RESOURCE
  [username resource-id]
  (first (db/read-where-and
           :resource-access-undeleted
           [:username :resource-id]
           [username resource-id])))

(defn READ-USERNAMES-BY-RESOURCE
  "Returns all usernames connected to resource"
  [resource-id]
  (db/read-all-where :resource-access-undeleted :resource-id resource-id [:username :last-verified]))
