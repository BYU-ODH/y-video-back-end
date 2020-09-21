(ns y-video-back.auth.auth-token
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
    (ut/renew-db)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

; auth-ping with valid auth token
(deftest auth-token-valid
  (testing "valid auth token"
    (let [user-one (db-pop/add-user)
          token (:id (auth-tokens/CREATE {:user-id (:id user-one)}))
          res (rp/auth-ping token)]
      (is (= 200 (:status res)))
      ; Check new auth-token is valid
      (let [new-auth-token (auth-tokens/READ (get-in res [:headers "session-id"]))]
        (is (= (:id user-one) (:user-id new-auth-token))))
      ; Check old auth-token has been deleted
      (let [old-auth-token (auth-tokens/READ token)]
        (is (nil? old-auth-token))))))

; auth-ping with invalid auth token
(deftest auth-token-invalid
  (testing "random uuid as auth token"
    (let [user-one (db-pop/add-user)
          token (java.util.UUID/randomUUID)
          res (rp/auth-ping token)]
      (is (= 401 (:status res)))
      ; Check old auth token provided in response headers
      (is (= token (get-in res [:headers "session-id"]))))))

; auth-ping with already used valid auth token
(deftest auth-token-use-twice
  (testing "use auth token twice"
    (let [user-one (db-pop/add-user "admin")
          token (:id (auth-tokens/CREATE {:user-id (:id user-one)}))
          res-one (rp/auth-ping token)
          res-two (rp/auth-ping token)]
      (is (= 200 (:status res-one)))
      (is (= 401 (:status res-two)))
      ; Check old auth token provided in response headers
      (is (= token (get-in res-two [:headers "session-id"]))))))

; auth-ping with expired valid auth token
(deftest auth-token-valid-expired
  (testing "expired auth token"
    (let [user-one (db-pop/add-user 0)
          token (:id (auth-tokens/CREATE {:user-id (:id user-one)}))]
      (Thread/sleep (-> env :auth :timeout))
      (let [res (rp/auth-ping token)]
        (is (= 401 (:status res)))
        ; Check old auth token provided in response headers
        (is (= token (get-in res [:headers "session-id"])))))))
