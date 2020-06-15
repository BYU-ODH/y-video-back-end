(ns y-video-back.routes.service_handlers.utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]))
(defn remove-db-only
  "Removes created, updated, and deleted fields from map"
  [my_map]
  (dissoc my_map :created :updated :deleted))

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
  [text_in]
  (java.util.UUID/fromString text_in))

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
