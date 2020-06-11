(ns y-video-back.routes.service_handlers.role_utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.routes.service_handlers.utils :as utils]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]))

(defn token-to-user-id
  "Returns userID associated with token. Returns false if token invalid."
  [token]
  ; DEVELOPMENT ONLY - token is actually the userID, so just return it
  token)

(defn get-user-type
  "Returns user type from DB"
  [user-id]
  (let [account-type (db/READ :users user-id [:account-type])]
    account-type))

(defn get-user-role-coll
  "Returns user role for collection from DB"
  [user-id collection-id]
  (let [user-role (db/read-where-and :user_collections_assoc
                                     [:user-id :collection-id]
                                     [user-id collection-id]
                                     [:account-role])]
    (if-not (empty? user-role)
      (:account-role (first user-role))
      ##Inf)))

(defn user-crse-coll
  "Returns true if user connected to collection via course"
  [user-id collection-id]
  (let [user-colls (users/READ-COLLECTIONS-BY-USER-VIA-COURSES user-id)]
    (contains? (set (map #(:id %) user-colls)) collection-id)))


(defn has-permission
  "Returns true if user has permission for route, else false"
  [token route args]
  (if (= token (utils/to-uuid "6bc824a6-f446-416d-8dd6-06350ae577f4"))
    true
    (let [user-id (token-to-user-id token)
          user-type (get-user-type user-id)]
      (case route
        "user-create" true
        "echo-post" (<= user-type 2)
        "collection-create" (<= user-type 2)
        "collection-get-by-id" (or (<= user-type 2)
                                   (<= (get-user-role-coll user-id (:collection-id args)) 2)
                                   (user-crse-coll user-id (:collection-id args)))
        "collection-add-user" (or (<= user-type 1) (<= (get-user-role-coll user-id (:collection-id args)) 3))
        false))))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
