(ns y-video-back.routes.service-handlers.utils.role-utils
  (:require [y-video-back.config :refer [env]]
            [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.routes.service-handlers.utils.utils :as utils]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]
            [y-video-back.db.permissions :as permissions]
            [y-video-back.db.auth-tokens :as auth-tokens]
            [y-video-back.routes.service-handlers.utils.db-utils :as dbu]
            [y-video-back.utils.account-permissions :as ac]))
            ;[y-video-back.config :refer [env]]))

(defn bypass-uri
  [uri]
  (or (clojure.string/starts-with? uri "/api/get-session-id/")
      (clojure.string/starts-with? uri "/api/docs")
      (clojure.string/starts-with? uri "/api/swagger")
      (clojure.string/starts-with? uri "/api/video")
      (clojure.string/starts-with? uri "/api/get-video-url");temporary
      (clojure.string/starts-with? uri "/api/media/stream-media/");temporary
      (clojure.string/starts-with? uri "/api/upload");temporary))
      (clojure.string/starts-with? uri "/api/ping")));temporary))

(defn token-to-user-id
  "Returns userID associated with token. Returns false if token invalid."
  [token]
  ; DEVELOPMENT ONLY - token is actually the userID, so just return it
  (let [res (auth-tokens/READ-UNEXPIRED token)]
    ;(println "token-to-user-id token=" token)
    ;(println "token-to-user-id res=" res)
    (:user-id res)))
  ;(:user-id (auth-tokens/READ token)))

(defn check-user-role
  "Checks if user has sufficient role in collection for id."
  [user-id obj-id role]
  (let [int-role (ac/to-int-role role)
        object-collections (set (map #(:collection-id %) (permissions/READ-ALL-BY-OBJ-ID obj-id [:collection-id])))
        user-collections (set (map #(:collection-id %) (filter #(<= (:role %) int-role) (permissions/READ-BY-USER user-id [:collection-id :role]))))]
    ;(println "in check-user-role with: " user-id obj-id int-role)
    ;(println "object-collections: " object-collections)
    ;(println "user-collections: " user-collections)
    ;(println "intersection: " (clojure.set/intersection object-collections user-collections))
    (> (count (clojure.set/intersection object-collections user-collections))
       0)))

(defn get-user-type
  "Returns user type from DB. Returns ##Inf if user not in DB."
  [user-id]
  (let [account-type (db/READ :users user-id [:account-type])]
    (first (remove nil? [account-type ##Inf]))))

(defn get-user-role-coll
  "Returns user role for collection from DB"
  [user-id collection-id]
  (let [user-role (db/read-where-and :user-collections-assoc
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


(defn is-child?
  "Returns true if target-id is child of user-id and user-id has at least
  'role' permissions for relevant collections. If 'role' is omitted, all
  children regardless of role are checked."
  ([target-id user-id role]
   (contains? (dbu/get-all-child-ids user-id role) target-id))
  ([target-id user-id]
   (is-child? target-id user-id ##Inf)))

(defn get-new-session-id
  "Generate new session-id, associated with same user as given session-id. Invalidate old session-id."
  [session-id]
  (let [new-session-id (:id (auth-tokens/CREATE {:user-id (:user-id (auth-tokens/READ-UNEXPIRED session-id))}))]
    ;(println "deleting auth-token: " session-id)
    (auth-tokens/DELETE session-id)
    new-session-id))

(defn has-permission-free-for-all
  "Placeholder for real has-permission function. Checks for session-id-bypass or (any) user-id."
  [token route args]
  ;(println "in has-permission")
  (if (= token (utils/to-uuid (:session-id-bypass env)))
    true
    (let [user-id (token-to-user-id token)]
      ;(println "user-id: " user-id)
      (not (nil? user-id)))))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "https://www.cheatsheet.com/wp-content/uploads/2020/02/anakin_council_ROTS.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
