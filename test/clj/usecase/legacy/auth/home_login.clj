(ns legacy.auth.home-login
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]
      [clojure.string :refer [includes?]]))

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

(deftest cas-redirect
  (testing "no login, access home page"
    (let [res (app (request :get "/"))]
      (is (= 200 (:status res)))
      (is (includes? (:body res) "clj_session_id=\"\""))))
  (testing "no login, access api/ping"
    (let [res (app (request :get "/api/ping"))]
      (is (= 200 (:status res)))
      (is (contains? (m/decode-response-body res) :message))))
  (testing "no login, access admin"
    (let [res (app (request :get "/admin"))]
      (is (= 200 (:status res)))
      (is (includes? (:body res) "clj_session_id=\"\"")))))





