(ns y-video-back.db.users-collections-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :users-collections-assoc))
(def READ  (partial db/READ :users-collections-assoc-undeleted))
(def READ-ALL  (partial db/READ :users-collections-assoc))
(def UPDATE (partial db/UPDATE :users-collections-assoc))
(def DELETE (partial db/mark-deleted :users-collections-assoc))
(def CLONE (partial db/CLONE :users-collections-assoc))
(def PERMANENT-DELETE (partial db/DELETE :users-collections-assoc))
