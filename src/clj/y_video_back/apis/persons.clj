(ns y-video-back.apis.persons
  (:require
    [y-video-back.config :refer [env]]
    [y-video-back.db.user-type-exceptions :as user-type-exceptions]
    [y-video-back.apis.utils :as ut]
    [clj-http.client :as client]
    [clojure.data.json :as json]
    [clojure.string :as str]
    [clojure.walk :as walk]))

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
  [json-res]
  (let [categories (get-cats-from-json json-res)
        account-type (get-in categories ["employee_summary" "employee_classification_code" "value"])]
    (if (and account-type
             (= "FAC" (str/upper-case account-type)))
      2
      3)))

(defn get-account-type
  "If username in user-type-exceptions, returns that value. Else, returns value from json."
  [netid json-res]
  (let [exception-result (user-type-exceptions/READ-BY-USERNAME [netid])]
    (if (empty? exception-result)
      (get-account-type-from-json json-res)
      (:account-type (first exception-result)))))

(defn get-person-id-from-json
  [js]
  (get-in (get-cats-from-json js) ["basic" "person_id" "value"]))

(defn get-user-data
  "Gets data from AcademicRecordsStudentStatusInfo"
  [netid] ;; (def netid "nbown16") (def netid "rjr45")
  (if (= (:front-end-netid env) netid)
    {:full-name netid
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
        {:full-name netid
         :byu-id nil
         :email (str netid "@yvideobeta.byu.edu")
         :account-type (if (:test env) 3 4)
         :person-id "000000000"}))))

(defn get-worker-id
  "Attempts to get a worker-id, if there is none, the user is not an employee"
  [byuid]
  (def url (str "https://api.byu.edu/bdp/iam/byuid_workerid/v1/?byu_id=" byuid))
  (def response (client/get url {:headers {"Authorization" (ut/get-oauth-token-new)}}))
  (def json-res (json/read-str (:body response)))
  (def data (get-in json-res ["data"]))
  (get-in data ["worker-id"])
)

(defn is-worker-id-empty
  "Determines if the worker id is empty"
  [workerid]
  (empty? workerid)
)

(defn get-employee-type
  "determines if the employee is faculty or something else"
  [positions]
  (loop [entries positions]
   (let [entry (first entries)]
    (if (entry :is_active_position)
      {
        :worker_type_id (entry :employee_or_contingent_worker_type_reference_id)
        :worker_type (entry :employee_or_contingent_worker_type)
      } ;; istrue
      (recur (rest entries)) ;; is false
    )
   ) 
  )
)

(defn assign-account-type
  "determines what the appropriate account type is for the user"
  [employee-type-data]
  (def employee-type-id (employee-type-data :worker_type_id))
  (def account-type-map {
    "STF" 3
    "STU" 3
    "FAC" 2
    "LA" 1 ;; I've no clue what lab assistant will come up as, if it comes up as anything... It may not ever be assigned. BDR
  })
  (if (contains? account-type-map employee-type-id)
    (get account-type-map employee-type-id) ;; get id since it is present in the map
    4 ;; else
  )
)

(defn get-employee-summary
  "gets information about the employee which can be used for other queries"
  [workerid byuid personid]
  (def response (client/get (str "https://api.byu.edu/bdp/human_resources/worker_summary/v1/?worker_id=" workerid)
                            {:headers {"Authorization" (ut/get-oauth-token-new)}}))
  (def body (response :body))
  (def json (json/read-str body))
  (def walk-result (walk/keywordize-keys json)) ;; remember to include walk library
  (def data_array (walk-result :data))
  (def data (first data_array))
  (def employee_type_data (get-employee-type (data :positions)))
  {
    :full-name (str (data :preferred_first_name) " " (data :preferred_last_name))
    :byu-id byuid
    :email (data :work_email_address)
    :account-type (assign-account-type employee_type_data)
    :person-id personid
  }
)

(defn get-student-summary
  "gets information about the student that can be used in other queries"
  [netid personid]
  (def response (client/get (str "https://api.byu.edu/bdp/student_academics/records/active_student_contact/v1/?net_id=" netid)
                            {:headers {"Authorization" (ut/get-oauth-token-new)}}))
  (def body (response :body))
  (def json (json/read-str body))
  (def walk-result (walk/keywordize-keys json)) ;; remember to include walk library
  (def data_array (walk-result :data))
  (def data (first data_array))
  (if (= nil data)
    (do {
      :full-name "unknown"
      :byu-id "unknown"
      :email "unknown"
      :account-type 5
      :person-id "unknown"
    })
    (do {
      :full-name (data :preferred_name)
      :byu-id (data :byu_id)
      :email (data :student_email_address)
      :account-type 3
      :person-id personid
    })
  )
)

(defn get-user-data-new
  "Gets data from AcademicRecordsStudentStatusInfo"
  [netid byuid personid] ;; (def netid "nbown16") (def netid "rjr45")
  (if (= (:front-end-netid env) netid)
    {:full-name netid
     :byu-id nil
     :email (str netid "@yvideobeta.byu.edu")
     :account-type 0
     :person-id "000000000"}
    ;; see if the user is an employee
    ;; if so, get employee summary
    ;; otherwise get student summary
    (try
      (def workerid (get-worker-id byuid))
      (if (is-worker-id-empty workerid)
        (get-student-summary netid personid)
        (get-employee-summary workerid byuid personid)
      )
      (catch Exception e
        {
         :full-name netid
         :byu-id nil
         :email (str netid "@yvideobeta.byu.edu")
         :account-type (if (:test env) 3 4)
         :person-id "000000000"
        }
      )
    )))