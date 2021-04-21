(ns y-video-back.apis.student-schedule
  (:require
    [y-video-back.apis.utils :as ut]
    [clj-http.client :as client]
    [clojure.data.json :as json]))

(defn get-courses-from-json
  [js]
  (get-in js ["WeeklySchedService" "response" "schedule_table"]))

(defn course-to-dep-and-cat
  "Returns vector. 0 is course, 1 is department, 2 is catalog number"
  [course]
  (re-find (re-matcher #"(.*) ([0-9]{3}\w{0,1})" course)))

(defn extract-course-data
  [course]
  (let [dep-cat-vector (course-to-dep-and-cat (get-in course ["course"]))]
    {:department (get dep-cat-vector 1)
     :catalog-number (get dep-cat-vector 2)
     :section-number (get-in course ["section"])}))

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
