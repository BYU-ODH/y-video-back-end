;; All functions - set up db such that target object could be add,
;; but do not add target object. Return target object.

;; For example: each Word requires a User (for its user-id field).
;; (get-word) creates a new User and adds it to the database, creates
;; a new Word with that User's id as its user-id, and returns the new
;; Word. However, it does not add the Word to the database.

;; Returned variable names only have -'s, no _'s.

(ns y-video-back.utils.db-populator
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]))

(defn get-user
  "Creates user, ready to be added to db"
  []
  (g/get-random-user-without-id))

(defn get-word
  "Creates word, ready to be added to db"
  []
  (let [new-user (ut/under-to-hyphen (users/CREATE (get-user)))]
    (g/get-random-word-without-id (:id new-user))))

(defn get-course
  "Creates course, ready to be added to db"
  []
  (g/get-random-course-without-id))

(defn get-collection
  "Creates collection, ready to be added to db"
  []
  (let [new-user (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id)))]
    (g/get-random-collection-without-id (:id new-user))))

(defn get-content
  "Creates content, ready to be added to db"
  []
  (let [new-coll (ut/under-to-hyphen (collections/CREATE (get-collection)))
        new-rsrc (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
    (g/get-random-content-without-id (:id new-coll) (:id new-rsrc))))

(defn get-resource
  "Creates resource, ready to be added to db"
  []
  (g/get-random-resource-without-id))

(defn get-file
  "Creates file, ready to be added to db"
  []
  (let [new-rsrc (ut/under-to-hyphen (resources/CREATE (get-resource)))]
    (g/get-random-file-without-id (:id new-rsrc))))
