(ns y-video-back.routes.refresh-courses-on-login
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
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]
      [y-video-back.db.migratus :as migratus]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (migratus/renew)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(defn remove-course-db-fields
  [course]
  (-> course
      (ut/remove-db-only)
      (dissoc :id :account-role :user-id)))
;
; (defn check-against-test-user
;   [user-id]
;   (is (= (frequencies (get-in env [:test-user
;                                    :courses]))
;          (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER user-id))))))
;
;
; (deftest refresh-courses-for-new-user
;   (testing "new user"
;     (let [res (rp/login-current-user (get-in env [:test-user :username]))
;           user-one (users/READ-BY-USERNAME (get-in env [:test-user :username]))]
;       (check-against-test-user (:id user-one)))))
;
; (deftest login-before-refresh-time-passes
;   (testing "new user"
;     (let [res (rp/login-current-user (get-in env [:test-user :username]))
;           user-one (users/READ-BY-USERNAME (get-in env [:test-user :username]))
;           crse-one (db-pop/add-course)
;           user-crse (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
;           res-two (rp/login-current-user (get-in env [:test-user :username]))]
;       (is (= (frequencies (into (get-in env [:test-user
;                                              :courses])
;                                 [(remove-course-db-fields crse-one)]))
;              (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER (:id user-one)))))))))
;
; (deftest login-check-last-course-api-field
;   (testing "login, wait, login then last-course-api field"
;     (let [res-one (rp/login-current-user (get-in env [:test-user :username]))
;           pause (Thread/sleep (* 3600000 (-> env :user-courses-refresh-after)))
;           pre-log (System/currentTimeMillis)
;           res-two (rp/login-current-user (get-in env [:test-user :username]))
;           post-log (System/currentTimeMillis)
;           user-res (first (users/READ-BY-USERNAME [(get-in env [:test-user :username])]))]
;       (is (< pre-log (inst-ms (:last-course-api user-res))))
;       (is (> post-log (inst-ms (:last-course-api user-res)))))))
