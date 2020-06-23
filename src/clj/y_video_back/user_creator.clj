(ns y-video-back.user-creator
  (:require
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.models :as models]
    [y-video-back.front-end-models :as fmodels]
    [y-video-back.model-specs :as sp]
    [y-video-back.routes.service-handlers.utils :as utils]
    [y-video-back.routes.service-handlers.role-utils :as ru]
    [clj-http.client :as client]
    [java-time :as t]
    [cheshire.core :as cheshire]
    [clojure.data.json :as json]))

(defn get-current-sem
  "Returns current semester in format YYYYT (Y=year, T=term)"
  []
  (let [time-map (vec (t/as (t/zoned-date-time) :year :month-of-year :day-of-month))
        year (str (get time-map 0))
        raw-month (int (get time-map 1))
        raw-day (int (get time-map 2))
        month (if (< raw-month 10)
                (str "0" raw-month)
                (str raw-month))
        day (if (< raw-day 10)
              (str "0" raw-day)
              (str raw-day))
        date-string (str year month day)]
    (-> (str "https://ws.byu.edu/rest/v1/academic/controls/controldatesws/asofdate/" date-string "/semester.json")
        (client/get)
        (:body)
        (json/read-str)
        (clojure.walk/keywordize-keys)
        (:ControldateswsService)
        (:response)
        (:date_list)
        (first)
        (:year_term))))


;; TODO talk to Peter about public collections

; Everyone is a student unless they are/were an instructor within the past year (6 terms)
; instructor_class_count > 0 => instructor
; Only check on create account - will probably change this to check periodically
; (maybe beginning of each term?)
(defn create-user
  "Creates user with data from BYU api"
  [username])


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
                                           :account-name (str "Mr. " username)})] ; Replace with api to get user info
        (get-current-sem)
        (:id user-create-res)))))
