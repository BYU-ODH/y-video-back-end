(ns y-video-back.user-creator
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.users :as users]
   [y-video-back.db.core :as db]
   [y-video-back.db.auth-tokens :as auth-tokens]
   [y-video-back.apis.persons :as persons-api]
   [y-video-back.course-creator :as cc]))

(defn create-user
  "Creates user with data from BYU api"
  [username]
  (let [user-data (persons-api/get-user-data username)
        create-res (users/CREATE {:username username
                                  :email (:email user-data)
                                  :last-login "na"
                                  :account-type (:account-type user-data)
                                  :account-name (:full-name user-data)
                                  :last-person-api (java.sql.Timestamp. (System/currentTimeMillis))
                                  :byu-person-id (:person-id user-data)})]
    (cc/check-courses-with-api username true)
    create-res))

(defn update-user
  "Updates user with data from BYU api"
  [username user-id]
  (let [user-data (persons-api/get-user-data username)
        current-data (first (db/read-all-where :users-undeleted username))
        role (if (or (= (:account-type current-data) 0) (= (:account-type current-data) 1))
               (:account-type current-data)
               (:account-type user-data))]
    (users/UPDATE user-id
                  {:email (:email user-data)
                   :account-type role
                   :account-name (:full-name user-data)
                   :last-person-api (java.sql.Timestamp. (System/currentTimeMillis))
                   :byu-person-id (:person-id user-data)})))


(defn get-auth-token
  "Adds auth token to database and returns it"
  [user-id]
  (:id (auth-tokens/CREATE {:user-id user-id})))

(defn get-session-id
  "Generates session id for user with given username. If user does not exist, first creates user."
  [username]
  (let [user-res (users/READ-BY-USERNAME [username])]
    (if-not (= 0 (count user-res))
      (do
        (if (< (inst-ms (:last-person-api (first user-res)))
               (- (System/currentTimeMillis) (* 3600000 (-> env :user-data-refresh-after))))
          (update-user username (:id (first user-res))))
        (cc/check-courses-with-api username)
        (get-auth-token (:id (first user-res))))
      (let [user-create-res (create-user username)]
        (cc/check-courses-with-api username)
        (get-auth-token (:id user-create-res))))))

(defn user-id-to-session-id
  "Generates session id for user with given id. If user does not exist, returns nil."
  [user-id]
  (let [user-res (users/READ user-id)]
    (if-not (nil? user-res)
      (get-auth-token user-id)
      nil)))
