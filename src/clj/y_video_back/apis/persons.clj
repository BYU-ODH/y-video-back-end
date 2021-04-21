(ns y-video-back.apis.persons
  (:require
    [y-video-back.config :refer [env]]
    [y-video-back.db.user-type-exceptions :as user-type-exceptions]
    [y-video-back.apis.utils :as ut]
    [clj-http.client :as client]
    [clojure.data.json :as json]
    [clojure.string :as str]))

(defn get-cats-from-json
  [js]
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

(defn get-person-id-from-json
  [js]
  (get-in (get-cats-from-json js) ["basic" "person_id" "value"]))

(defn get-user-data
  "Gets data from AcademicRecordsStudentStatusInfo"
  [netid]
  (if (= (:front-end-netid env) netid)
    {:full-name (str netid " no_name")
     :byu-id nil
     :email (str netid "@yvideobeta.byu.edu")
     :account-type 0
     :person-id "000000000"}
    (try
      (let [url (str "https://api.byu.edu:443/byuapi/persons/v3/?net_ids=" netid "&field_sets=basic%2Cemployee_summary%2Cstudent_summary%2Cemail_addresses")
            res (try (client/get url {:oauth-token ut/oauth-token})
                     (catch Exception e
                       (client/get url {:oauth-token (ut/get-oauth-token-new)})))
            json-res (json/read-str (:body res))
            full-name (get-in (get-cats-from-json json-res) ["basic" "preferred_name" "value"])
            byu-id (get-in (get-cats-from-json json-res) ["basic" "byu_id" "value"])
            email (first (filter #(not (nil? %)) [(get-email-from-json json-res), "none"]))
            account-type (get-account-type netid json-res)
            person-id (get-person-id-from-json json-res)]
        {:full-name full-name
         :byu-id byu-id
         :email email
         :account-type account-type
         :person-id person-id})
      (catch Exception e
        {:full-name (str netid " no_name")
         :byu-id nil
         :email (str netid "@yvideobeta.byu.edu")
         :account-type (if (:test env) 3 4)
         :person-id "000000000"}))))
