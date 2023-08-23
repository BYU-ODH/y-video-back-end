(ns y-video-back.routes.service-handlers.handlers.user-handlers-test
  (:require [clojure.test :refer [use-fixtures deftest is testing]]
            [mount.core :as mount]
            [y-video-back.config :refer [env]]
            [y-video-back.handler :refer :all]
            [y-video-back.db.core :refer [*db*] :as db]
            [y-video-back.db.users :as users]
            [y-video-back.routes.service-handlers.handlers.user-handlers :as subj]
            [legacy.db.test-util :as tcore]
            [legacy.utils.utils :as ut]
            [taoensso.timbre :as log]))

(tcore/basic-transaction-fixtures
 (mount/start #'y-video-back.handler/env)
 (mount/start #'y-video-back.handler/db) 
 (mount/start #'y-video-back.handler/app)
 (ut/renew-db))

(deftest _user-create-from-byu
  (let [get-user-count (fn [] (count (users/READ-ALL)))
        pre-user-count (atom (get-user-count))
        private-user-id "a0315200" ;; TODO these usernames need to be disassociated with any particular user
        public-user-id "torysa"]
    (log/info {:pre-user-count @pre-user-count})
    (testing "Initializing"
       (is (= (get-user-count) @pre-user-count)))
    (testing "user created for new public users"
      (subj/_user-create-from-byu public-user-id)
      (swap! pre-user-count inc)
      (is (= @pre-user-count (get-user-count)) "One new user created for public"))
    (testing "user created for new private users"
      (subj/_user-create-from-byu private-user-id)
      (swap! pre-user-count inc)
      (is (= @pre-user-count (get-user-count)) "one new user created for private"))
    (testing "Does nothing for existing users"
      (subj/_user-create-from-byu private-user-id)
      (subj/_user-create-from-byu public-user-id)
      (is (= @pre-user-count (get-user-count)) "no new user created with duplicate public or private"))))
