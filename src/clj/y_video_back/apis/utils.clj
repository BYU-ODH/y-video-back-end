(ns y-video-back.apis.utils
  (:require
    [y-video-back.config :refer [env]]
    [clj-http.client :as client]
    [java-time :as t]
    [clojure.data.json :as json]
    [clojure.walk :as walk]))

(defn get-today-date-time
  "Gets today's date as a datetime (YYYYMMDDThh:mm:ss)"
  []
  (def time-map (vec (t/as (t/zoned-date-time) :year :month-of-year :day-of-month)))
  (def year (str (get time-map 0)))
  (def raw-month (int (get time-map 1)))
  (def raw-day (int (get time-map 2)))
  (def month (if (< raw-month 10)
                (str "0" raw-month)
                (str raw-month))
  )
  (def day (if (< raw-day 10)
              (str "0" raw-day)
              (str raw-day))
  )
  (str year "-" month "-" day "T00:00:00");; couldn't figure out how to parse zoned-date-time hours, min, and seconds. Putting 00:00:00 for the time should be fine for now -BDR 9/11/2024
)

;; new function that uses BDP - Ben Rencher 9/5/2024
(defn get-current-sem-real ;; confirmed that this results in the same data as the previous api call - BDR 10/15/2024
  "Returns current semester in format YYYYT (Y=year, T=term)"
  []
  (def date-string (get-today-date-time)) ;; store the current date string in date-string
  ;; call the api we need with the correct token
  (def response (client/get "https://api.byu.edu/bdp/student_academics/academic_control_dates/v1/?control_date_type=CURRICULUM"
              {:headers {"Authorization" (get-oauth-token-new)}} ;; make sure you are referencing the correct function path
  ))
  (def body (response :body)) ;; client/get returns a map with the content stored in "body", so get that
  (def json (json/read-str body)) ;; decode the json string
  (def walk-result (walk/keywordize-keys json)) ;; assign the json attributes to keywords so clojure can work with it
  (def data (walk-result :data)) ;; get the "data" attribute from the response, which contains what we want
  (loop [entries data] ;; assinging "entries" to the full value of "data" initially
    (let [entry (first entries)] ;; getting the first value of "entries" vector and assigning it to "entry"
        ;; check if this entry has the year_term we want
        (if (and (<= (compare (entry :start_date_time) date-string) -1) ;; have to use "compare" operator to compare strings
                (> (compare (entry :end_date_time) date-string) 0))
            (entry :year_term) ;; return the year_term value if it is what we want
            (recur (rest entries)) ;; otherwise, start the loop over with the rest of the "entries" vector (everything minus the first entry)
        )
    )
  )
)

(defn get-oauth-token-new
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

;; Old function useing out of date controldatesws endpoint. Keeping htis here for reference for the time being - BDR 10/15/2024
;; (defn get-current-sem-real
;;   "Returns current semester in format YYYYT (Y=year, T=term)"
;;   []
;;   (let [time-map (vec (t/as (t/zoned-date-time) :year :month-of-year :day-of-month))
;;         year (str (get time-map 0))
;;         raw-month (int (get time-map 1))
;;         raw-day (int (get time-map 2))
;;         month (if (< raw-month 10)
;;                 (str "0" raw-month)
;;                 (str raw-month))
;;         day (if (< raw-day 10)
;;               (str "0" raw-day)
;;               (str raw-day))
;;         date-string (str year month day)]
;;     (-> (str "https://ws.byu.edu/rest/v1/academic/controls/controldatesws/asofdate/" date-string "/semester.json")
;;         (client/get)
;;         (:body)
;;         (json/read-str)
;;         (walk/keywordize-keys)
;;         (:ControldateswsService)
;;         (:response)
;;         (:date_list)
;;         (first)
;;         (:year_term))))

;; (defn get-current-sem
;;   "Placeholder for development purposes"
;;   []
;;   (if (:test env)
;;     "20201"
;;     (get-current-sem-real-new)))
