(ns y-video-back.apis.student-schedule
  (:require
    [y-video-back.apis.utils :as ut]
    [clj-http.client :as client]
    [clojure.data.json :as json]
    [clojure.walk :as walk]))

;; new data extractor to use with new api call - BDR 9/5/2024
(defn extract-course-data-new
  [course]
  {
    :department (course :teaching_area)
    :catalog-number (str (course :catalog_number) (course :catalog_suffix))
    :section-number (course :section_number)
  }
)



;; new api for getting student's courses - Ben Rencher 9/5/2024. may or may not use this since the other API isn't being retired
(defn get-api-courses-new
  [netid]
  (def response (client/get (str "https://api.byu.edu/bdp/student_academics/records/current_student_class_enrollments/v1/?net_id=" netid "&year_term=" (ut/get-current-sem-real))
                {:headers {"Authorization" (ut/get-oauth-token-new)}}
  ))
  (def body (response :body))
  (def json (json/read-str body))
  (def walk-result (walk/keywordize-keys json))
  (def data (walk-result :data))
  (map extract-course-data-new data)
)

;; we shouldn't need this anymore because the data returned by the bdp endpoint is arranged differently - BDR 9/5/2024
;; (defn course-to-dep-and-cat
;;   "Returns vector. 0 is course, 1 is department, 2 is catalog number"
;;   [course]
;;   (re-find (re-matcher #"(.*) ([0-9]{3}\w{0,1})" course)))

;; (defn extract-course-data
;;   [course]
;;   (let [dep-cat-vector (course-to-dep-and-cat (get-in course ["course"]))]
;;     {:department (get dep-cat-vector 1)
;;      :catalog-number (get dep-cat-vector 2)
;;      :section-number (get-in course ["section"])}))

;; (defn get-courses-from-json
;;   [js]
;;   (get-in js ["WeeklySchedService" "response" "schedule_table"]))

;; (defn get-api-courses
;;   [person-id]
;;   (if (= "000000000" person-id)
;;     []
;;     (try
;;     ;; this api isn't going away, and we need to keep it for the time being. BDR 10/14/2024
;;       (let [url (str "https://api.byu.edu:443/domains/legacy/academic/registration/studentschedule/v1/" person-id "/" (ut/get-current-sem-real))
;;             res (try (client/get url {:oauth-token ut/oauth-token})
;;                      (catch Exception e
;;                        (client/get url {:oauth-token (ut/get-oauth-token-new)})))
;;             json-res (json/read-str (:body res))]
;;         (map extract-course-data (get-courses-from-json json-res)))
;;       (catch Exception e
;;         (println e)
;;         []))))
