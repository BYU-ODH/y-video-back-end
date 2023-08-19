(ns y-video-back.routes.service-handlers.handlers.user-handlers-tests
  (:require [clojure.test :refer [use-fixtures deftest is testing]]
            [mount.core :as mount]
            [y-video-back.config :refer [env]]
            [y-video-back.handler :refer :all]
            [y-video-back.db.core :refer [*db*] :as db]
            [y-video-back.db.users :as users]
            [y-video-back.routes.service-handlers.handlers.user-handlers :as subj]
            [legacy.db.test-util :as tcore]
            [legacy.utils.utils :as ut]
            ))

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

(deftest _user-create-from-byu
  (let [get-user-count (fn [] (count (users/READ-ALL)))
        pre-user-count (get-user-count)]
    (testing "Initializing"
       (is (= (get-user-count) pre-user-count)))

    (testing "Works for new public users"
      (is false))
    (testing "works for new private users"
      (is false))
    (testing "Does nothing for existing users"
       (is false))
      ))
