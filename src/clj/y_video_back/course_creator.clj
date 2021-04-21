(ns y-video-back.course-creator
  (:require
    [y-video-back.config :refer [env]]
    [y-video-back.db.users :as users]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.apis.student-schedule :as schedule-api]
    [y-video-back.utils.account-permissions :as ac]
    [y-video-back.routes.service-handlers.utils.utils :as ut]))

(defn remove-course-db-fields
  [course]
  (-> course
      (ut/remove-db-only)
      (dissoc :id :account-role :user-id)))

(defn lazy-contains? [col key]
  (some #{key} col))

(defn get-db-courses
  [user-id]
  (user-courses-assoc/READ-COURSES-BY-USER-AND-ROLE [user-id (ac/to-int-role "student")]))

(defn delete-course-assoc
  [user-id course]
  (user-courses-assoc/DELETE-BY-IDS [(:id course) user-id]))

(defn add-course-assoc
  [user-id course]
  (if-not (courses/EXISTS-DEP-CAT-SEC? (:department course) (:catalog-number course) (:section-number course))
    (courses/CREATE course))
  (let [crse-one (first (courses/READ-ALL-BY-DEP-CAT-SEC [(:department course) (:catalog-number course) (:section-number course)]))]
    (user-courses-assoc/CREATE {:user-id user-id
                                :course-id (:id crse-one)
                                :account-role (ac/to-int-role "student")})))

(defn refresh-courses
  "Queries AcademicRegistrationStudentSchedule api, makes db reflect results"
  [user-id person-id]
  (let [api-courses (schedule-api/get-api-courses person-id)
        db-courses (get-db-courses user-id)
        to-delete (set (filter #(not (lazy-contains? api-courses (remove-course-db-fields %))) db-courses))
        to-add (set (filter #(not (lazy-contains? (map remove-course-db-fields db-courses) %)) api-courses))]
    (doall (map #(delete-course-assoc user-id %) to-delete))
    (doall (map #(add-course-assoc user-id %) to-add))))


(defn check-last-course-api
  [user-res]
  (< (inst-ms (:last-course-api user-res))
     (- (System/currentTimeMillis) (* 3600000 (-> env :user-courses-refresh-after)))))

(defn check-courses-with-api
  "If enough time has passed, checks user's course enrollments with api"
  ([username force-api]
   (let [user-res (first (users/READ-BY-USERNAME [username]))]
     (if (or force-api (check-last-course-api user-res))
       (do
         (refresh-courses (:id user-res) (:byu-person-id user-res))
         (users/UPDATE (:id user-res) {:last-course-api (java.sql.Timestamp. (System/currentTimeMillis))})))))
  ([username]
   (check-courses-with-api username false)))
