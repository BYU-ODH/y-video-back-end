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
  ;; [username byu-person-id] will need byu id for new bdp call
  [username byuid personid]
  ;; (let [user-data (persons-api/get-user-data-new byu-person-id) will need byu id for new bdp call
  (let [user-data (persons-api/get-user-data-new username byuid personid)
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
  ;; [username user-id byu-person-id] will need byu id for new bdp api calls
  [username user-id byuid personid]
  ;; (let [user-data (persons-api/get-user-data-new byu-person-id) will use byu id for new bdp call
  (let [user-data (persons-api/get-user-data-new username byuid personid)
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
  [username byuid personid]
  (let [user-res (users/READ-BY-USERNAME [username])]
    (if-not (= 0 (count user-res))
      (do
        (if (or
              ;; If account hasn't been updated within the last hour,
              ;; or account-name, email, person-id, or account-type is unknown or invalid value (5),
              ;; update the user data by calling update-user function
               (< (inst-ms (:last-person-api (first user-res)))
               (- (System/currentTimeMillis) (* 3600000 (-> env :user-data-refresh-after))))
               (= (:account-name (first user-res)) "unknown")
               (= (:email (first user-res)) "unknown")
               (= (:byu-person-id (first user-res)) "unknown")
               (= (:account-type (first user-res)) 5)
            )
          (update-user username (:id (first user-res)) byuid personid))
        (cc/check-courses-with-api username)
        (get-auth-token (:id (first user-res))))
      (let [user-create-res (create-user username byuid personid)]
        (cc/check-courses-with-api username)
        (get-auth-token (:id user-create-res))))))

(defn create-potentially-empty-user
  "Checks if the user has data via student data api. If not, creates a user devoid of all meaningful
  data except for username (netid). Intended to be used when associated new users with a collection
  before that user created an account by logging in"
  [username]
  (def student-data (persons-api/get-student-summary username "unknown"))
  (let [create-res (users/CREATE {:username username
                                  :email (:email student-data)
                                  :last-login "na"
                                  :account-type (:account-type student-data)
                                  :account-name (:full-name student-data)
                                  :last-person-api (java.sql.Timestamp. (System/currentTimeMillis))
                                  :byu-person-id (:person-id student-data)})]
    (cc/check-courses-with-api username true)
    create-res
  )
)

(defn user-id-to-session-id
  "Generates session id for user with given id. If user does not exist, returns nil."
  [user-id]
  (let [user-res (users/READ user-id)]
    (if-not (nil? user-res)
      (get-auth-token user-id)
      nil)))
