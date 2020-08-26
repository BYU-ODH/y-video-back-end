(ns stories.schedule-api
  "Interactions with api to get user schedules"
  (:require [y-video-back.config :refer [env]]
            [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [y-video-back.handler :refer :all]
            [y-video-back.db.test-util :as tcore]
            [muuntaja.core :as m]
            [clojure.java.jdbc :as jdbc]
            [mount.core :as mount]
            [y-video-back.config :refer [env]]
            [y-video-back.course-creator :as cc]
            [y-video-back.apis.student-schedule :as schedule-api]
            [y-video-back.utils.db-populator :as db-pop]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]
            [y-video-back.utils.utils :as ut]))


(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))


(defn remove-course-db-fields
  [course]
  (-> course
      (ut/remove-db-only)
      (dissoc :id :account-role :user-id)))


(deftest api-hiccup-test
  (testing "api returns no courses randomly"
    (let [user-one (-> (db-pop/get-user)
                       (dissoc :username)
                       (assoc :username (get-in env [:test-user :username]))
                       (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
          user-one-add (users/CREATE user-one)
          user-id (:id user-one-add)]
      (is (= [] (user-courses-assoc/READ-COURSES-BY-USER user-id)))
      ; successful api query
      (cc/check-courses-with-api (:username user-one) true)
      (is (= (frequencies (get-in env [:test-user
                                       :courses]))
             (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER user-id)))))
      ; unsuccessful api query (returns empty schedule)
      (with-redefs-fn {#'schedule-api/get-api-courses (fn [arg] [])}
        #(cc/check-courses-with-api (:username user-one) true))
      (is (= []
             (user-courses-assoc/READ-COURSES-BY-USER user-id)))
      ; another successful api query
      (cc/check-courses-with-api (:username user-one) true)
      (is (= (frequencies (get-in env [:test-user
                                       :courses]))
             (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER user-id))))))))
