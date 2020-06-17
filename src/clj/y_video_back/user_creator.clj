(ns y-video-back.user-creator
  (:require
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))

(defn get-session-id
  "Generates session id for user with given username. If user does not exist, first creates user."
  [username]
  (let [user-res (users/READ-BY-USERNAME [username])]
    ;(println "getting session id - - - - - - - - - - - ")
    ;(println username)
    ;(println user-res)
    (if-not (= 0 (count user-res))
      (:id (first user-res))
      (let [user-create-res (users/CREATE {:username username
                                           :email (str username "@byu.edu")
                                           :last-login "today"
                                           :account-type 0
                                           :account-name "Ed"})] ; Replace with api to get user info
        (:id user-create-res)))))
