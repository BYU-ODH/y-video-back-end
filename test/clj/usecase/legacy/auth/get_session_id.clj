(ns legacy.auth.get-session-id
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer [use-fixtures deftest testing is]]
    [legacy.db.test-util :as tcore]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [legacy.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
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

(deftest get-session-id-endpoint
  (testing "with password in env"
    (let [res (rp/login-current-user "test")]
      (is (= 200 (:status res)))))
  (testing "without password in env"
    (with-redefs [env (dissoc env :NEW-USER-PASSWORD)]
      (let [res (rp/login-current-user "test" "password")]
        (is (= 401 (:status res)))))))
