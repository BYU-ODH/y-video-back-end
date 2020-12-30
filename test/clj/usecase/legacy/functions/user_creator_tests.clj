(ns legacy.functions.user-creator-tests
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.user-type-exceptions :as user-type-exceptions]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [legacy.utils.utils :as ut]
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
  (mount.core/start #'y-video-back.handler/app)

 (deftest test-create-user-on-get-session-id
;   (testing "test-user username"
;     (let [res (uc/get-session-id (get-in env [:test-user :username]))
;           user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))]
;       (is (= {:username (get-in env [:test-user :username])
;               :email (get-in env [:test-user :email])
;               :account-name (get-in env [:test-user :full-name])
;               :account-type (get-in env [:test-user :account-type])}
;              {:username (get-in user-res [:username])
;               :email (get-in user-res [:email])
;               :account-name (get-in user-res [:account-name])
;               :account-type (get-in user-res [:account-type])}))))
  (testing "hopefully invalid username"
    (let [bad-username "cig4sowe"
          res (uc/get-session-id bad-username)
          user-res (first (users/READ-BY-USERNAME [bad-username]))]
      (is (= {:username bad-username
              :email (str bad-username "@yvideobeta.byu.edu")
              :account-name (str bad-username " no_name")
              :account-type 3}
             {:username (get-in user-res [:username])
              :email (get-in user-res [:email])
              :account-name (get-in user-res [:account-name])
              :account-type (get-in user-res [:account-type])}))))
  (testing "too-long invalid username" ; BYU-CAS limit is 8 characters in netid
    (let [bad-username "1234567890"
          res (uc/get-session-id bad-username)
          user-res (first (users/READ-BY-USERNAME [bad-username]))]
      (is (= {:username bad-username
              :email (str bad-username "@yvideobeta.byu.edu")
              :account-name (str bad-username " no_name")
              :account-type 3}
             {:username (get-in user-res [:username])
              :email (get-in user-res [:email])
              :account-name (get-in user-res [:account-name])
              :account-type (get-in user-res [:account-type])}))))))

; (deftest test-create-user-on-get-session-id-with-type-exception
;   (testing "test-user username"
;     (user-type-exceptions/CREATE {:username (get-in env [:test-user :username])
;                                   :account-type (+ 1 (int (get-in env [:test-user :account-type])))})
;     (let [res (uc/get-session-id (get-in env [:test-user :username]))
;           user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))]
;       (is (= {:username (get-in env [:test-user :username])
;               :email (get-in env [:test-user :email])
;               :account-name (get-in env [:test-user :full-name])
;               :account-type (+ 1 (int (get-in env [:test-user :account-type])))}
;              {:username (get-in user-res [:username])
;               :email (get-in user-res [:email])
;               :account-name (get-in user-res [:account-name])
;               :account-type (get-in user-res [:account-type])})))))
