(ns y-video-back.db.users-by-collection
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :users-by-collection))
(def READ  (partial db/READ :users-by-collection))
(def READ-ALL  (partial db/READ :users-by-collection))
(def UPDATE (partial db/UPDATE :users-by-collection))
(def DELETE (partial db/mark-deleted :users-by-collection))
(def CLONE (partial db/CLONE :users-by-collection))
(def PERMANENT-DELETE (partial db/DELETE :users-by-collection))
(def READ-BY-COLLECTION (partial db/read-all-where :users-by-collection :collection-id))
