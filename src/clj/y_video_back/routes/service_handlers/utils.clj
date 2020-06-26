(ns y-video-back.routes.service-handlers.utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]))
(defn remove-db-only
  "Removes created, updated, and deleted fields from map"
  [my-map]
  (dissoc my-map :created :updated :deleted))

(defn add-namespace ; Can probably delete this function, not in use?
  "Converts all keywords to namespace-keywords"
  [namespace m]
  (into {}
    (map (fn [val]
           {
            (keyword
              namespace
              (clojure.string/replace
                (str
                  (get val 0))
                ":"
                ""))
            (get val 1)})
      m)))

(defn to-uuid
  [text-in]
  (java.util.UUID/fromString text-in))

(defn get-id
  [res]
  (str (:id res)))

(defn user-db-to-front
  "Replace keywords with what the front end expects"
  [user]
  {:id (:id user)
   :username (:username user)
   :name (:account-name user)
   :email (:email user)
   :roles [(:account-type user)]
   :lastLogin (:last-login user)})

(defn coll-db-to-front
  "Replace keywords with what the front end expects"
  [coll]
  {:id (:id coll)
   :name (:collection-name coll)
   :published (:published coll)
   :archived (:archived coll)})

(defn cont-db-to-front
  "Replace keywords with what the front end expects"
  [cont]
  {:id (:id cont)
   :name (:resource-name cont)
   :resourceType (:resource-type cont)
   :requester (:requester-email cont)
   :thumbnail (:thumbnail cont)
   :isCopyrighted (:copyrighted cont)
   :physicalCopyExists (:physical-copy-exists cont)
   :fullVideo (:full-video cont)
   :published (:published cont)
   :allow-definitions (:allow-definitions cont)
   :allow-notes (:allow-notes cont)
   :allow-captions (:allow-captions cont)
   :dateValidated (:date-validated cont)
   :views (:views cont)
   :metadata (:metadata cont)})
