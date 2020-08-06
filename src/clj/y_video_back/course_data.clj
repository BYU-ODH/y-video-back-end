(ns y-video-back.course-data
  (:require
    [y-video-back.config :refer [env]]
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.auth-tokens :as auth-tokens]
    [y-video-back.models :as models]
    [y-video-back.front-end-models :as fmodels]
    [y-video-back.model-specs :as sp]
    [y-video-back.routes.service-handlers.utils.utils :as utils]
    [y-video-back.routes.service-handlers.utils.role-utils :as ru]
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
        (:year_term)))
  "20201") ; temporary, for testing


(defn get-oauth-token
  "Gets oauth token from api"
  []
  (let [url "https://api.byu.edu/token"
        auth (str (:CONSUMER_KEY env) ":" (:CONSUMER_SECRET env))
        tokenRes (client/post url {:body "grant_type=client_credentials"
                                   :basic-auth auth
                                   :content-type "application/x-www-form-urlencoded"})]
    (get (json/read-str (:body tokenRes)) "access_token")))

(defn get-class-sections
  "Gets sections offered for a given department, curriculum-id, and title code from AcademicRegistrationOfferings"
  [dpt cur-id title-code sem]
  (try
    (let [url (str "https://api.byu.edu:443/domains/legacy/academic/registration/offerings/v1/" sem "/" dpt "/" cur-id "/" title-code)
          res (client/get url {:oauth-token (get-oauth-token)})]
      (let [json-res (json/read-str (:body res))
            sec-map (first (get-in json-res ["RegOfferingsService" "response" "course_catalog_information"]))
            ; Get course_sections
            course-sections (map #(get % "section") (get sec-map "course_sections"))
            ; Get slc_sections
            slc-sections (map #(get % "section") (get sec-map "slc_sections"))]
        (set (concat course-sections slc-sections))))
    (catch Exception e (println (str "caught exception: " (.getMessage e))
                                "\n" dpt " " cur-id " " title-code))))

(defn get-department-classes
  "Gets courses offered by department from AcademicRegistrationOfferings"
  [dpt sem]
  (let [url (str "https://api.byu.edu:443/domains/legacy/academic/registration/offerings/v1/" sem "/" dpt)
        res (client/get url {:oauth-token (get-oauth-token)})]
    (let [json-res (json/read-str (:body res))
          course-list (get-in json-res ["RegOfferingsService" "response" "courses "])] ; the trailing space in courses is necessary
      course-list)))


(defn get-teaching-areas
  "Gets data for all courses currently offered from AcademicRegistrationOfferings"
  [sem]
  (let [url (str "https://api.byu.edu:443/domains/legacy/academic/registration/offerings/v1/" sem)
        res (client/get url {:oauth-token (get-oauth-token)})]
    (let [json-res (json/read-str (:body res))
          teaching-areas (get-in json-res ["RegOfferingsService" "response" "teaching_areas "])] ; the trailing space in teaching_areas is necessary
      teaching-areas)))
