(ns y-video-back.apis.utils
  (:require
    [y-video-back.config :refer [env]]
    [clj-http.client :as client]
    [java-time :as t]
    [clojure.data.json :as json]
    [clojure.walk :as walk]))

;; new function that uses BDP - Ben Rencher 9/5/2024
(defn get-current-sem-real-new
  "Returns the yearterm corresponding to today's date in format YYYYT (Y=year, T=term)"
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
    (-> (str "https://api.byu.edu/bdp/student_academics/academic_control_dates/v1?start_date_time=" date-string)
        ;; (client/get {:debug true})
        (client/get) ;; makes the call?
        (:body) ;; gets the body? more like assigns the entire response to the value "body"
        (json/read-str) ;; parses the json?
        (walk/keywordize-keys) ;; parses the keys?
        (:data) ;; name of attribute we want
        (first) ;; get first value in list from :data
        (:year_term) ;; get yearterm attribute inside first value of :data
    )
  )
)

(defn get-current-sem-real
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
        (walk/keywordize-keys)
        (:ControldateswsService)
        (:response)
        (:date_list)
        (first)
        (:year_term))))

(defn get-current-sem
  "Placeholder for development purposes"
  []
  (if (:test env)
    "20201"
    (get-current-sem-real-new)))

(defn get-oauth-token-new  ; TODO - store token locally, only query new one when needed
  "Gets oauth token from api"
  []
  (let [url "https://api.byu.edu/token"
        auth (str (:CONSUMER_KEY env) ":" (:CONSUMER_SECRET env))
        tokenRes (client/post url {:body "grant_type=client_credentials"
                                   :basic-auth auth
                                   :content-type "application/x-www-form-urlencoded"})
        new-token (get (json/read-str (:body tokenRes)) "access_token")]
    (def oauth-token new-token)
    new-token))

(def oauth-token "")
