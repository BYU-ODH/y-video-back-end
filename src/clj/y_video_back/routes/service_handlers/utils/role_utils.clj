(ns y-video-back.routes.service-handlers.utils.role-utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.db.users :as users]
            [y-video-back.config :refer [env]]
            [y-video-back.db.permissions :as permissions]
            [y-video-back.db.auth-tokens :as auth-tokens]
            [y-video-back.utils.account-permissions :as ac]
            [clojure.string :as str]
            [clojure.set :as set]))

(defn bypass-uri
  [uri]
  (or (str/starts-with? uri "/api/get-session-id/")
      (str/starts-with? uri "/api/docs")
      (str/starts-with? uri "/api/swagger")
      (str/starts-with? uri "/api/admin/public-collection/")
      (str/starts-with? uri "/api/media/stream-media/")
      (str/starts-with? uri "/api/partial-media/stream-media/")
      (str/starts-with? uri "/api/ping")))

(defn token-to-user-id
  "Returns user-id associated with token. Returns nil if token invalid."
  [token]
  (let [res (auth-tokens/READ-UNEXPIRED token)]
    (if (nil? res)
      nil
      (:user-id res))))

(defn token-to-user
  "Returns user-id associated with valid token."
  [token]
  (let [res (auth-tokens/READ-UNEXPIRED token)]
    (if (nil? res)
      nil
      (users/READ (:user-id res)))))

(defn token-to-user-id-all
  "Returns user-id associated with token even if token is invalid."
  [token]
  (let [res (auth-tokens/READ-ALL token)]
    (if (nil? res)
      nil
      (:user-id res))))

(defn check-user-role
  "Checks if user has sufficient role in collection for id."
  [user-id obj-id role]
  (let [int-role (ac/to-int-role role)
        object-collections (set (map #(:collection-id %) (permissions/READ-ALL-BY-OBJ-ID obj-id [:collection-id])))
        user-collections (set (map #(:collection-id %) (filter #(<= (:role %) int-role) (permissions/READ-BY-USER user-id [:collection-id :role]))))]
    (> (count (set/intersection object-collections user-collections))
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

(defn get-new-session-id
  ; "Generate new session-id, associated with same user as given session-id. Invalidate old session-id."
  "If the session-id time stamp is still within certain time then we do not renew"
  [session-id]
  (let [current-session (auth-tokens/READ-UNEXPIRED session-id)]
    (if (> (- (inst-ms (java.time.Instant/now)) (inst-ms (get current-session :created))) (-> env :SESSION_TIMEOUT))
      (let [c-id session-id] 
        (auth-tokens/DELETE c-id) "expired")
      (let [c-id session-id] 
        (db/UPDATE :auth-tokens c-id (assoc current-session :created (java.time.Instant/now))) c-id))));add 4 hours to current session or in other words renew the creating date

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "https://www.cheatsheet.com/wp-content/uploads/2020/02/anakin_council_ROTS.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
