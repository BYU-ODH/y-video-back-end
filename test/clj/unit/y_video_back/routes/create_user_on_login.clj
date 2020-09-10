(ns y-video-back.routes.create-user-on-login
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
      [y-video-back.db.contents :as contents]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

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

(deftest create-user-on-login
  (testing "create user from empty db"
    (is (= nil (users/READ-BY-USERNAME "bagginses")))
    (let [res (rp/login-current-user "bagginses")]
      (is (= 200 (:status res))))))
;
; (deftest login-with-api-refresh
;   (testing "wait for needed refresh time"
;     (let [res (rp/login-current-user (get-in env [:test-user :username]))
;           user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))
;           user-update (users/UPDATE (:id user-res) (db-pop/get-user))]
;       (is (= 200 (:status res)))
;       (Thread/sleep (* 3600000 (-> env :user-data-refresh-after)))
;       (let [res (rp/login-current-user (get-in env [:test-user :username]))
;             user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))]
;         (is (= {:username (get-in env [:test-user :username])
;                 :email (get-in env [:test-user :email])
;                 :account-name (get-in env [:test-user :full-name])
;                 :account-type (get-in env [:test-user :account-type])}
;                {:username (get-in user-res [:username])
;                 :email (get-in user-res [:email])
;                 :account-name (get-in user-res [:account-name])
;                 :account-type (get-in user-res [:account-type])}))))))
;
; (deftest login-without-api
;   (testing "repeat query before refresh time"
;     (let [res (rp/login-current-user (get-in env [:test-user :username]))
;           user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))
;           user-update (users/UPDATE (:id user-res) (dissoc (db-pop/get-user) :username))]
;       (is (= 200 (:status res)))
;       (let [res (rp/login-current-user (get-in env [:test-user :username]))
;             user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))]
;         (is (= {:username (get-in user-update [:username])
;                 :email (get-in user-update [:email])
;                 :account-name (get-in user-update [:account-name])
;                 :account-type (get-in user-update [:account-type])}
;                {:username (get-in user-res [:username])
;                 :email (get-in user-res [:email])
;                 :account-name (get-in user-res [:account-name])
;                 :account-type (get-in user-res [:account-type])}))))))
; (deftest login-check-last-person-api-field
;   (testing "login, wait, login then check last-person-api field"
;     (let [res-one (rp/login-current-user (get-in env [:test-user :username]))
;           pause (Thread/sleep (* 3600000 (-> env :user-data-refresh-after)))
;           pre-log (System/currentTimeMillis)
;           res-two (rp/login-current-user (get-in env [:test-user :username]))
;           post-log (System/currentTimeMillis)
;           user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))]
;       (is (< pre-log (inst-ms (:last-person-api user-res))))
;       (is (> post-log (inst-ms (:last-person-api user-res)))))))
