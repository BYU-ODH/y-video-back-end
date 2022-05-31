; All get functions - set up db such that target object could be add,
; but do not add target object. Return target object.

; For example: each Word requires a User (for its user-id field).
; (get-word) creates a new User and adds it to the database, creates
; a new Word with that User's id as its user-id, and returns the new
; Word. However, it does not add the Word to the database.

; All add functions - same as get functions, with additional step of
; adding target object to the db. Target object is returned with
; id included.

(ns legacy.utils.db-populator
    (:require
      [legacy.utils.model-generator :as g]
      [legacy.utils.utils :as ut]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.resource-access :as resource-access]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.languages :as languages]
      [y-video-back.db.subtitles :as subtitles]
      [y-video-back.db.files :as files]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.utils.account-permissions :as ac]))

(defn get-user
  "Creates user, ready to be added to db"
  ([]
   (g/get-random-user-without-id))
  ([account-type]
   (-> (get-user)
       (dissoc :account-type)
       (assoc :account-type (ac/to-int-type account-type)))))

(defn add-user
  "Creates user, adds to db"
  ([]
   (let [user-one (get-user)
         user-one-add (users/CREATE user-one)]
     (assoc user-one :id (:id user-one-add))))
  ([account-type]
   (let [user-one (get-user account-type)
         user-one-add (users/CREATE user-one)]
     (assoc user-one :id (:id user-one-add)))))

(defn get-word
  "Creates word, ready to be added to db"
  ([]
   (let [new-user (ut/under-to-hyphen (users/CREATE (get-user)))]
     (get-word (:id new-user))))
  ([user-id]
   (g/get-random-word-without-id user-id)))

(defn add-word
  "Creates word, adds to db"
  ([]
   (let [word-one (get-word)
         word-one-add (words/CREATE word-one)]
     (assoc word-one :id (:id word-one-add))))
  ([user-id]
   (let [word-one (get-word user-id)
         word-one-add (words/CREATE word-one)]
     (assoc word-one :id (:id word-one-add)))))

(defn get-course
  "Creates course, ready to be added to db"
  ([]
   (g/get-random-course-without-id))
  ([dep cat sec]
   (-> (get-course)
       (dissoc :department :catalog-number :section-number)
       (assoc :department dep :catalog-number cat :section-number sec))))

(defn add-course
  "Creates course, adds to db"
  ([]
   (let [crse-one (get-course)
         crse-one-add (courses/CREATE crse-one)]
     (assoc crse-one :id (:id crse-one-add))))
  ([dep cat sec]
   (let [crse-one (get-course dep cat sec)
         crse-one-add (courses/CREATE crse-one)]
     (assoc crse-one :id (:id crse-one-add)))))

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

(defn get-public-collection
  "Creates public collection, ready to be added to db"
  ([]
   (let [new-user (add-user "admin")]
     (-> (g/get-random-collection-without-id (:id new-user))
         (dissoc :public)
         (assoc :public true))))
  ([owner-id]
   (-> (g/get-random-collection-without-id owner-id)
       (dissoc :public)
       (assoc :public true))))


(defn add-public-collection
  "Creates public collection, adds to db"
  ([]
   (let [coll-one (get-public-collection)
         coll-one-add (collections/CREATE coll-one)]
     (assoc coll-one :id (:id coll-one-add))))
  ([owner-id]
   (let [coll-one (get-public-collection owner-id)
         coll-one-add (collections/CREATE coll-one)]
     (assoc coll-one :id (:id coll-one-add)))))



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

(defn get-public-resource
  "Creates resource, ready to be added to db"
  []
  (-> (g/get-random-resource-without-id)
      (dissoc :public)
      (assoc :public true)))

(defn add-public-resource
  "Creates resource, adds to db"
  []
  (let [rsrc-one (get-public-resource)
        rsrc-one-add (resources/CREATE rsrc-one)]
    (assoc rsrc-one :id (:id rsrc-one-add))))

(defn get-language
  "Creates language, ready to be added to db"
  ([]
   (g/get-random-language-without-id)))

(defn add-language
  "Creates language, adds to db"
  []
  (let [lang-one (get-language)
        lang-one-add (languages/CREATE lang-one)]
    (assoc lang-one :id (:id lang-one-add))))

(defn get-file
  "Creates file, ready to be added to db. Contains random filepath."
  ([]
   (get-file (:id (add-resource))))
  ([resource-id]
   (assoc (dissoc (g/get-random-file-without-id resource-id)
                  :file-version)
          :file-version
          (:id (add-language)))))

(defn add-file
  "Creates file, adds to db. Contains random filepath."
  ([]
   (let [file-one (get-file)
         file-one-add (files/CREATE file-one)]
     (assoc file-one :id (:id file-one-add))))
  ([resource-id]
   (let [file-one (get-file resource-id)
         file-one-add (files/CREATE file-one)]
     (assoc file-one :id (:id file-one-add)))))

(defn get-content
  "Creates content, ready to be added to db"
  ([]
   (let [new-coll (ut/under-to-hyphen (collections/CREATE (get-collection)))
         new-rsrc (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))
         new-file (ut/under-to-hyphen (files/CREATE (get-file)))]
     (g/get-random-content-without-id (:id new-coll) (:id new-rsrc) (:id new-file))))
  ([collection-id resource-id file-id]
   (g/get-random-content-without-id collection-id resource-id file-id)))

(defn add-content
  "Creates content, adds to db"
  ([]
   (let [cont-one (get-content)
         cont-one-add (contents/CREATE cont-one)]
     (assoc cont-one :id (:id cont-one-add))))
  ([collection-id resource-id file-id]
   (let [cont-one (get-content collection-id resource-id file-id)
         cont-one-add (contents/CREATE cont-one)]
     (assoc cont-one :id (:id cont-one-add)))))

(defn get-public-content
  "Creates content, ready to be added to db"
  ([]
   (let [new-coll (ut/under-to-hyphen (collections/CREATE (get-public-collection)))
         new-rsrc (ut/under-to-hyphen (resources/CREATE (get-public-resource)))]
     (-> (g/get-random-content-without-id (:id new-coll) (:id new-rsrc))
         (dissoc :public)
         (assoc :public true))))
  ([collection-id resource-id]
   (-> (g/get-random-content-without-id collection-id resource-id)
       (dissoc :public)
       (assoc :public true))))

(defn add-public-content
  "Creates content, adds to db"
  ([]
   (let [cont-one (get-public-content)
         cont-one-add (contents/CREATE cont-one)]
     (assoc cont-one :id (:id cont-one-add))))
  ([collection-id resource-id]
   (let [cont-one (get-public-content collection-id resource-id)
         cont-one-add (contents/CREATE cont-one)]
     (assoc cont-one :id (:id cont-one-add)))))

(defn get-subtitle
  "Creates subtitle, ready to be added to db"
  ([]
   (get-subtitle (:id (add-content))))
  ([content-id]
   (g/get-random-subtitle-without-id content-id)))

(defn add-subtitle
  "Creates subtitle, adds to db"
  ([]
   (add-subtitle (:id (add-content))))
  ([content-id]
   (let [sbtl-one (get-subtitle content-id)
         sbtl-one-add (subtitles/CREATE sbtl-one)]
     (assoc sbtl-one :id (:id sbtl-one-add)))))

(defn get-user-coll-assoc
  "Creates user-coll-assoc, ready to be added to db"
  ([username collection-id role]
   (assoc (dissoc (g/get-random-user-collections-assoc-without-id username collection-id)
                  :account-role)
          :account-role
          (ac/to-int-role role)))
  ([username collection-id]
   (g/get-random-user-collections-assoc-without-id username collection-id))
  ([]
   (let [user-one (add-user)
         coll-one (add-collection)]
     (get-user-coll-assoc (:id user-one) (:id coll-one)))))

(defn add-user-coll-assoc
  "Creates user-coll-assoc, adds to db"
  ([username collection-id role]
   (let [uca-one (get-user-coll-assoc username collection-id role)
         uca-one-add (user-collections-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add))))
  ([username collection-id]
   (let [uca-one (get-user-coll-assoc username collection-id)
         uca-one-add (user-collections-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add))))
  ([]
   (let [uca-one (get-user-coll-assoc)
         uca-one-add (user-collections-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add)))))

(defn get-user-crse-assoc
  "Creates user-crse-assoc, ready to be added to db"
  ([user-id course-id role]
   (assoc (dissoc
            (g/get-random-user-courses-assoc-without-id user-id course-id)
            :account-role)
          :account-role (ac/to-int-role role)))
  ([user-id course-id]
   (g/get-random-user-courses-assoc-without-id user-id course-id))
  ([]
   (let [user-one (add-user)
         crse-one (add-course)]
     (get-user-crse-assoc (:id user-one) (:id crse-one)))))

(defn add-user-crse-assoc
  "Creates user-crse-assoc, adds to db"
  ([user-id course-id role]
   (let [uca-one (get-user-crse-assoc user-id course-id role)
         uca-one-add (user-courses-assoc/CREATE uca-one)]
     (assoc uca-one :id (:id uca-one-add))))
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

(defn get-resource-access
  "Creates resource-id, ready to be added to db"
  [username resource-id]
  {:username username :resource-id resource-id})

(defn add-resource-access
  "Creates resourece-access"
  ([username resource-id]
   (let [rsrc-acc (get-resource-access username resource-id)
         rsrc-acc-add (resource-access/CREATE rsrc-acc)
         temp (resource-access/READ (:id rsrc-acc-add))]
     (assoc rsrc-acc :id (:id rsrc-acc-add)))))









