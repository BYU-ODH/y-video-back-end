(ns y-video-back.db.users
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :users))
(def READ  (partial db/READ :users-undeleted))

(defn READ-BY-USERNAME
  "READ user by username `un`, converting to a collection if necessary"
  [un]
  (let [un (if (coll? un) un [un])]
    (db/read-where-and :users-undeleted [:username] un)))

(defn READ-BY-EMAIL
  "READ user by username `un`, converting to a collection if necessary"
  [em]
  (let [em (if (coll? em) em [em])]
    (db/read-where-and :users-undeleted [:email] em)))

(def READ-ALL  (partial db/READ :users))
(def UPDATE (partial db/UPDATE :users))
(def DELETE (partial db/mark-deleted :users))
(def CLONE (partial db/CLONE :users))
(def PERMANENT-DELETE (partial db/DELETE :users))
(def READ-WORDS (partial db/read-all-where :words-undeleted :user-id))
(def READ-COLLECTIONS-BY-USER-VIA-COURSES (partial db/read-all-where :collections-by-users-via-courses :user-id))
(defn EXISTS? [id] (not (nil? (db/READ :users-undeleted id))))
(defn id-to-username
  "Returns username. Returns nil if id is nil or user doesn't exist."
  [id]
  (if (nil? id)
    nil
    (let [res (READ id)]
      (if (nil? res)
        nil
        (:username res)))))
