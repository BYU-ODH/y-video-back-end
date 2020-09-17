(ns y-video-back.auth.home-login
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
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
    (ut/renew-db)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(deftest cas-redirect
  (testing "no login, access home page"
    (let [res (app (request :get "/"))]
      (is (= 302 (:status res)))
      (is (= (str "https://cas.byu.edu/cas/login?service=" (:host env) "/")
             (get-in res [:headers "Location"])))))
  (testing "no login, access api/ping"
    (let [res (app (request :get "/api/ping"))]
      (is (= 200 (:status res)))
      (is (= {:message "pong"}
             (m/decode-response-body res)))))
  (testing "no login, access admin"
    (let [res (app (request :get "/admin"))]
      (is (= 302 (:status res)))
      (is (= (str "https://cas.byu.edu/cas/login?service=" (:host env) "/admin")
             (get-in res [:headers "Location"]))))))
