(ns y-video-back.db.user-type-exceptions
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :user-type-exceptions))
(def READ  (partial db/READ :user-type-exceptions-undeleted))
(def READ-BY-USERNAME (partial db/read-where-and :user-type-exceptions-undeleted [:username]))
(def READ-ALL  (partial db/READ :user-type-exceptions))
(def UPDATE (partial db/UPDATE :user-type-exceptions))
(def DELETE (partial db/mark-deleted :user-type-exceptions))
(def CLONE (partial db/CLONE :user-type-exceptions))
(def PERMANENT-DELETE (partial db/DELETE :user-type-exceptions))
(defn EXISTS? [id] (not (nil? (db/READ :user-type-exceptions-undeleted id))))
