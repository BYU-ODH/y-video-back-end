(ns y-video-back.routes.service_handlers.utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]))
(defn remove-db-only
  "Compares 2 maps, not counting created, updated, and deleted fields"
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

(defn token-to-user-id
  "Returns userID associated with token. Returns false if token invalid."
  [token]
  ; DEVELOPMENT ONLY - token is actually the userID, so just return it
  token)

(defn get-user-type
  "Returns user type from DB"
  [user-id]
  (let [account-type (db/READ :users user-id [:account-type])]
    (println "account type: - - - - - - - - - - - - -")
    (println account-type)
    (println " - - - - - - - - - - - - - - - - - - -")
    account-type))

(defn get-user-role
  "Returns user role for collection from DB"
  [user-id collection-id]
  (let [user-role (db/read-where-and :user_collections_assoc
                                     [:user-id :collection-id]
                                     [user-id collection-id]
                                     [:account-role])]
    (println "account role: - - - - - - - - - - - - -")
    (println (str "collection-id " collection-id))
    (println user-role)
    (println (type user-role))
    (println "- - - - - - - - - - - - - - - - - - - -")
    (if-not (empty? user-role)
      (:account-role (first user-role))
      ##Inf)))

(defn has-permission
  "Returns true if user has permission for route, else false"
  [token route args]
  (let [user-id (token-to-user-id token)
        user-type (get-user-type user-id)]
    (case route
      "user-create" true
      "collection-create" (<= user-type 2)
      "collection-add-user" (or (<= user-type 1) (<= (get-user-role user-id (:collection-id args)) 3))
      false)))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
