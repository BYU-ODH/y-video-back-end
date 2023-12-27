(ns y-video-back.apis.student-schedule-test
  (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer [deftest is testing use-fixtures]]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [mount.core :as mount]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]
      [y-video-back.semesters :as semesters]))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (ut/renew-db)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(deftest get-api-courses
  (testing "Get courses"
    (let [person-id "NNN"
          next-sem ]
      (is false)
          )))
