(ns y-video-back.apis.student-schedule
  (:require
    [y-video-back.apis.utils :as ut]
    [clj-http.client :as client]
    [clojure.data.json :as json]))

;; new json parse for courses
(defn get-courses-from-json-new
  [json-obj]
  (get-in json-obj ["data"]) ;; I believe the highest level attribute is "response" and I know that "data" is where the classes are stored in the response
)

(defn get-courses-from-json
  [js]
  (get-in js ["WeeklySchedService" "response" "schedule_table"]))

;; we shouldn't need this anymore because the data returned by the bdp endpoint is arranged differently - BDR 9/5/2024
(defn course-to-dep-and-cat
  "Returns vector. 0 is course, 1 is department, 2 is catalog number"
  [course]
  (re-find (re-matcher #"(.*) ([0-9]{3}\w{0,1})" course)))

;; new data extractor to use with new api call - BDR 9/5/2024
(defn extract-course-data-new
  [course]
  {
    :department (get-in course ["teaching_area"])
    :catalog-number (get-in course ["catalog_number"])
    :section-number (get-in course ["section_number"])
  }
)

(defn extract-course-data
  [course]
  (let [dep-cat-vector (course-to-dep-and-cat (get-in course ["course"]))]
    {:department (get dep-cat-vector 1)
     :catalog-number (get dep-cat-vector 2)
     :section-number (get-in course ["section"])}))

;; new api for getting student's courses - Ben Rencher 9/5/2024
(defn get-api-courses-new
  [netid]
  (let [url (str "https://api.byu.edu/bdp/student_academics/records/current_student_class_enrollments/v1/?net_id=grs45" "&year_term=" (ut/get-current-sem))
        res (try (client/get url {:oath-token ut/oauth-token})
                 (catch Exception e
                    (client/get url {:oath-token (ut/get-oauth-token-new)})))
        json-res (json/read-str (:body res))]
    (map extract-course-data-new (get-courses-from-json-new json-res))
  )
)

(defn get-api-courses
  [person-id]
  (if (= "000000000" person-id)
    []
    (try
      (let [url (str "https://api.byu.edu:443/domains/legacy/academic/registration/studentschedule/v1/" person-id "/" (ut/get-current-sem))
            res (try (client/get url {:oauth-token ut/oauth-token})
                     (catch Exception e
                       (client/get url {:oauth-token (ut/get-oauth-token-new)})))
            json-res (json/read-str (:body res))]
        (map extract-course-data (get-courses-from-json json-res)))
      (catch Exception e
        (println e)
        []))))
