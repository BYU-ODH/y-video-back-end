(ns legacy.auth.auth-token
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer [use-fixtures deftest testing is]]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.db.auth-tokens :as auth-tokens]))

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
    (let [user-one (db-pop/add-user "admin")
          token (:id (auth-tokens/CREATE {:user-id (:id user-one)}))
          res (rp/auth-ping token)]
      (is (= 200 (:status res)))
      ;Check new auth-token is valid. Do not check for old auth because now 1 auth token lasts 4 hrs. So the old auth token will not be deleted
      (let [new-auth-token (auth-tokens/READ (get-in res [:headers "session-id"]))]
        (is (= (:id user-one) (:user-id new-auth-token)))))))

; auth-ping with invalid auth token
(deftest auth-token-invalid
  (testing "random uuid as auth token"
    (let [user-one (db-pop/add-user "admin")
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
      (is (= 200 (:status res-two)))
      ; Check old auth token provided in response headers
      (is (= token (get-in res-two [:headers "session-id"]))))))

; auth-ping with expired valid auth token
(deftest auth-token-valid-expired
  (testing "expired auth token"
    (let [user-one (db-pop/add-user "admin")
          token (:id (auth-tokens/CREATE {:user-id (:id user-one)}))]
      ; sleeping the thread and waiting is not very efficient.
      ; update the current token to expired
      ; (Thread/sleep (* 3 (-> env :auth :timeout)))
      (auth-tokens/DELETE token)
      (let [res (rp/auth-ping token)]
        (is (= 401 (:status res)))
        ; Check old auth token provided in response headers
        (is (= token (get-in res [:headers "session-id"])))))))
