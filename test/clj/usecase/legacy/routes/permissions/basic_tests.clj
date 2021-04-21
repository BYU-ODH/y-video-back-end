(ns legacy.routes.permissions.basic-tests
  (:require
    [clojure.test :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [legacy.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]))

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


(deftest dummy
  (is (= 0 0)))

(deftest no-session-id-for-testing
  (testing "no session id"
    (let [res (rp/echo-post "6bc824a6-f446-416d-8dd6-06350ae577f4" "hey")]
      (is (= 200 (:status res)))
      (is (= {:echo "hey"} (m/decode-response-body res))))
    (let [res (rp/echo-post "there")]
      (is (= 200 (:status res)))
      (is (= {:echo "there"} (m/decode-response-body res))))))
