(ns y-video-back.db.users
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :users))
(def READ  (partial db/READ :users-undeleted))
(def READ-ALL  (partial db/READ :users))
(def UPDATE (partial db/UPDATE :users))
(def DELETE (partial db/mark-deleted :users))
(def CLONE (partial db/CLONE :users))
(def PERMANENT-DELETE (partial db/DELETE :users))
(def READ-WORDS (partial db/read-all-where :words-undeleted :user-id))
