(ns y-video-back.user-creator
  (:require
    [y-video-back.config :refer [env]]
    [y-video-back.db.users :as users]
    [y-video-back.db.user-type-exceptions :as user-type-exceptions]
    [y-video-back.db.auth-tokens :as auth-tokens]
    [clj-http.client :as client]
    [java-time :as t]
    [clojure.data.json :as json]
    [clojure.string :as str]
    [clojure.walk :as walk]))

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
        (walk/keywordize-keys)
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

(defn get-oauth-token  ; TODO - store token locally, only query new one when needed
  "Gets oauth token from api"
  []
  (let [url "https://api.byu.edu/token"
        auth (str (:CONSUMER_KEY env) ":" (:CONSUMER_SECRET env))
        tokenRes (client/post url {:body "grant_type=client_credentials"
                                   :basic-auth auth
                                   :content-type "application/x-www-form-urlencoded"})]
    (get (json/read-str (:body tokenRes)) "access_token")))

(defn get-cats-from-json
  [js]
  ;(println "get-cats-js=" js)
  (first (get-in js ["values"])))


(defn get-email-from-json
  [js]
  (if (= 0 (count (get-in (get-cats-from-json js) ["email_addresses" "values"])))
    nil
    (first (filter #(not (nil? %))
                   (map #(get-in % ["email_address" "value"])
                        (get-in (get-cats-from-json js) ["email_addresses" "values"]))))))

(defn get-account-type-from-json
  [js]
  ;(println "get-account-type-js" js)
  (if (= "FAC" (str/upper-case (get-in (get-cats-from-json js) ["employee_summary" "employee_classification_code" "value"])))
    2
    3))

(defn get-account-type
  "If username in user-type-exceptions, returns that value. Else, returns value from json."
  [username js]
  (let [exc-res (user-type-exceptions/READ-BY-USERNAME [username])]
    (if (= 0 (count exc-res))
      (get-account-type-from-json js)
      (:account-type (first exc-res)))))

(defn get-user-data
  "Gets data from AcademicRecordsStudentStatusInfo"
  [netid]
  (if (and false
           (:test env)
           (not (= (get-in env [:test-user :username])
                   netid)))
    {:full-name (str netid " no_name")
     :byu-id nil
     :email (str netid "@yvideobeta.byu.edu")
     :account-type 4}
    (try
      (let [url (str "https://api.byu.edu:443/byuapi/persons/v3/?net_ids=" netid "&field_sets=basic%2Cemployee_summary%2Cstudent_summary%2Cemail_addresses")
            res (client/get url {:oauth-token (get-oauth-token)})
            json-res (json/read-str (:body res))
            ; tra (println "res=" res)
            ; tar (println "json-res=" json-res)
            full-name (get-in (get-cats-from-json json-res) ["basic" "preferred_name" "value"])
            byu-id (get-in (get-cats-from-json json-res) ["basic" "byu_id" "value"])
            email (first (filter #(not (nil? %)) [(get-email-from-json json-res), "none"]))
            account-type (get-account-type netid json-res)]
        {:full-name full-name
         :byu-id byu-id
         :email email
         :account-type account-type})
      (catch Exception e
        {:full-name (str netid " no_name")
         :byu-id nil
         :email (str netid "@yvideobeta.byu.edu")
         :account-type 4}))))

(defn create-user
  "Creates user with data from BYU api"
  [username]
  (let [user-data (get-user-data username)]
    (users/CREATE {:username username
                   :email (:email user-data)
                   :last-login "today"
                   :account-type (:account-type user-data)
                   :account-name (:full-name user-data)
                   :last-person-api (java.sql.Timestamp. (System/currentTimeMillis))})))

(defn update-user
  "Updates user with data from BYU api"
  [username user-id]
  (let [user-data (get-user-data username)]
    (users/UPDATE user-id
                  {:email (:email user-data)
                   :account-type (:account-type user-data)
                   :account-name (:full-name user-data)
                   :last-person-api (java.sql.Timestamp. (System/currentTimeMillis))})))


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
        (get-auth-token (:id (first user-res))))
      (let [user-create-res (create-user username)]
        (get-auth-token (:id user-create-res))))))

(defn user-id-to-session-id
  "Generates session id for user with given id. If user does not exist, returns nil."
  [user-id]
  (let [user-res (users/READ user-id)]
    (if-not (nil? user-res)
      (get-auth-token user-id)
      nil)))
