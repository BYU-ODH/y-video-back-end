(ns y-video-back.db.users
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :users))
(def READ  (partial db/READ :users-undeleted))
(def READ-BY-USERNAME (partial db/read-where-and :users [:username]))
(def READ-ALL  (partial db/READ :users))
(def UPDATE (partial db/UPDATE :users))
(def DELETE (partial db/mark-deleted :users))
(def CLONE (partial db/CLONE :users))
(def PERMANENT-DELETE (partial db/DELETE :users))
(def READ-WORDS (partial db/read-all-where :words-undeleted :user-id))
(def READ-COLLECTIONS-BY-USER-VIA-COURSES (partial db/read-all-where :collections_by_users_via_courses :user_id))
