(ns y-video-back.auth.get-session-id
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer [use-fixtures deftest testing is]]
    ;[ring.mock.request :refer :all]
    ;[y-video-back.handler :refer :all]
    [y-video-back.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [y-video-back.utils.model-generator :as g]
    [y-video-back.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.utils.utils :as ut]
    [y-video-back.utils.db-populator :as db-pop]
    [y-video-back.db.auth-tokens :as auth-tokens]
    [y-video-back.user-creator :as uc]))

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

(deftest get-session-id-endpoint
  (testing "with password in env"
    (let [res (rp/login-current-user "test")]
      (is (= 200 (:status res)))))
  (testing "without password in env"
    (with-redefs [env (dissoc env :NEW-USER-PASSWORD)]
      (let [res (rp/login-current-user "test" "password")]
        (is (= 401 (:status res)))))))
