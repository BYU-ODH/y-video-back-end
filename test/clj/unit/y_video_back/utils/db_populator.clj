;; All get functions - set up db such that target object could be add,
;; but do not add target object. Return target object.

;; For example: each Word requires a User (for its user-id field).
;; (get-word) creates a new User and adds it to the database, creates
;; a new Word with that User's id as its user-id, and returns the new
;; Word. However, it does not add the Word to the database.

;; All add functions - same as get functions, with additional step of
;; adding target object to the db. Target object is returned with
;; id included.

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
      [y-video-back.db.subtitles :as subtitles]
      [y-video-back.db.files :as files]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]))

(defn get-user
  "Creates user, ready to be added to db"
  []
  (g/get-random-user-without-id))

(defn add-user
  "Creates user, adds to db"
  []
  (let [user-one (get-user)
        user-one-add (users/CREATE user-one)]
    (assoc user-one :id (:id user-one-add))))

(defn get-word
  "Creates word, ready to be added to db"
  []
  (let [new-user (ut/under-to-hyphen (users/CREATE (get-user)))]
    (g/get-random-word-without-id (:id new-user))))

(defn get-course
  "Creates course, ready to be added to db"
  []
  (g/get-random-course-without-id))

(defn add-course
  "Creates course, adds to db"
  []
  (let [coll-one (get-course)
        coll-one-add (courses/CREATE coll-one)]
    (assoc coll-one :id (:id coll-one-add))))

(defn get-collection
  "Creates collection, ready to be added to db"
  ([]
   (let [new-user (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id)))]
     (g/get-random-collection-without-id (:id new-user))))
  ([owner-id]
   (g/get-random-collection-without-id owner-id)))


(defn add-collection
  "Creates collection, adds to db"
  ([]
   (let [coll-one (get-collection)
         coll-one-add (collections/CREATE coll-one)]
     (assoc coll-one :id (:id coll-one-add))))
  ([owner-id]
   (let [coll-one (get-collection owner-id)
         coll-one-add (collections/CREATE coll-one)]
     (assoc coll-one :id (:id coll-one-add)))))

(defn get-content
  "Creates content, ready to be added to db"
  ([]
   (let [new-coll (ut/under-to-hyphen (collections/CREATE (get-collection)))
         new-rsrc (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
     (g/get-random-content-without-id (:id new-coll) (:id new-rsrc))))
  ([collection-id resource-id]
   (g/get-random-content-without-id collection-id resource-id)))

(defn add-content
  "Creates content, adds to db"
  ([]
   (let [cont-one (get-content)
         cont-one-add (contents/CREATE cont-one)]
     (assoc cont-one :id (:id cont-one-add))))
  ([collection-id resource-id]
   (let [cont-one (get-content collection-id resource-id)
         cont-one-add (contents/CREATE cont-one)]
     (assoc cont-one :id (:id cont-one-add)))))


(defn get-resource
  "Creates resource, ready to be added to db"
  []
  (g/get-random-resource-without-id))

(defn add-resource
  "Creates resource, adds to db"
  []
  (let [rsrc-one (get-resource)
        rsrc-one-add (resources/CREATE rsrc-one)]
    (assoc rsrc-one :id (:id rsrc-one-add))))

(defn get-subtitle
  "Creates subtitle, ready to be added to db"
  ([]
   (let [new-rsrc (add-resource)]
     (g/get-random-subtitle-without-id (:id new-rsrc))))
  ([resource-id]
   (g/get-random-subtitle-without-id resource-id)))
(defn add-subtitle
  "Creates subtitle, adds to db"
  ([]
   (add-subtitle (:id (add-resource))))
  ([resource-id]
   (let [sbtl-one (get-subtitle resource-id)
         sbtl-one-add (subtitles/CREATE sbtl-one)]
     (assoc sbtl-one :id (:id sbtl-one-add)))))

(defn get-file
  "Creates file, ready to be added to db"
  ([]
   (let [new-rsrc (ut/under-to-hyphen (resources/CREATE (get-resource)))]
     (g/get-random-file-without-id (:id new-rsrc))))
  ([resource-id]
   (g/get-random-file-without-id resource-id)))


(defn add-file
  "Creates file, adds to db"
  ([]
   (let [file-one (get-file)
         file-one-add (files/CREATE file-one)]
     (assoc file-one :id (:id file-one-add))))
  ([resource-id]
   (let [file-one (get-file resource-id)
         file-one-add (files/CREATE file-one)]
     (assoc file-one :id (:id file-one-add)))))

(defn get-user-coll-assoc
  "Creates user-coll-assoc, ready to be added to db"
  ([user-id collection-id]
   (g/get-random-user-collections-assoc-without-id user-id collection-id))
  ([]
   (let [user-one (add-user)
         coll-one (add-collection)]
     (get-user-coll-assoc (:id user-one) (:id coll-one)))))

(defn add-user-coll-assoc
  "Creates user-coll-assoc, adds to db"
  ([user-id collection-id]
   (let [uca-one (get-user-coll-assoc user-id collection-id)
         uca-one-add (user-collections-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add))))
  ([]
   (let [uca-one (get-user-coll-assoc)
         uca-one-add (user-collections-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add)))))

(defn get-user-crse-assoc
  "Creates user-crse-assoc, ready to be added to db"
  ([user-id course-id]
   (g/get-random-user-courses-assoc-without-id user-id course-id))
  ([]
   (let [user-one (add-user)
         crse-one (add-course)]
     (get-user-crse-assoc (:id user-one) (:id crse-one)))))

(defn add-user-crse-assoc
  "Creates user-crse-assoc, adds to db"
  ([user-id course-id]
   (let [uca-one (get-user-crse-assoc user-id course-id)
         uca-one-add (user-courses-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add))))
  ([]
   (let [uca-one (get-user-crse-assoc)
         uca-one-add (user-courses-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add)))))

(defn get-coll-crse-assoc
  "Creates coll-crse-assoc, ready to be added to db"
  ([coll-id course-id]
   (g/get-random-collection-courses-assoc-without-id coll-id course-id))
  ([]
   (let [coll-one (add-collection)
         crse-one (add-course)]
     (get-coll-crse-assoc (:id coll-one) (:id crse-one)))))

(defn add-coll-crse-assoc
  "Creates coll-crse-assoc, adds to db"
  ([coll-id course-id]
   (let [cca-one (get-coll-crse-assoc coll-id course-id)
         cca-one-add (collection-courses-assoc/CREATE cca-one)]
     (assoc cca-one :id (:id cca-one-add))))
  ([]
   (let [cca-one (get-coll-crse-assoc)
         cca-one-add (collection-courses-assoc/CREATE cca-one)]
     (assoc cca-one :id (:id cca-one-add)))))
