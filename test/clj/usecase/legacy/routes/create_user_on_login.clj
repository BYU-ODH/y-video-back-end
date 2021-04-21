(ns legacy.routes.create-user-on-login
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.users :as users]
      [legacy.utils.utils :as ut]))

(declare ^:dynamic *txn*)

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

(deftest create-user-on-login
  (testing "create user from empty db"
    (is (= nil (users/READ-BY-USERNAME "bagginses")))
    (let [res (rp/login-current-user "bagginses")]
      (is (= 200 (:status res))))))













