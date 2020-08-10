(ns y-video-back.routes.permissions.account-role.user-tests
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
    [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
    [y-video-back.db.users-by-collection :as users-by-collection]
    [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.resources :as resources]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.words :as words]
    [y-video-back.utils.utils :as ut]
    [y-video-back.utils.db-populator :as db-pop]
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

;get: /api/user
(deftest user-get-by-logged-in
  (testing "admin user-get-by-logged-in"
    (let [user-one (db-pop/add-user (:admin env))
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "lab assistant user-get-by-logged-in"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "instructor user-get-by-logged-in"
    (let [user-one (db-pop/add-user (:instructor env))
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "student user-get-by-logged-in"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res))))))

;get: /api/user
; n/a

;post: /api/user
; n/a

;get: /api/user/{id}
(deftest user-get-by-id
  (testing "student, user-get-by-id, get self"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/user-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id user-one))]
      (is (= 401 (:status res))))))

;delete: /api/user/{id}
(deftest user-delete-by-id
  (testing "lab assistant, user-delete-by-id, delete self"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-one))]
      (is (= 401 (:status res)))))
  (testing "instructor, user-delete-by-id, delete self"
    (let [user-one (db-pop/add-user (:instructor env))
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-one))]
      (is (= 401 (:status res)))))
  (testing "student, user-delete-by-id, delete self"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-one))]
      (is (= 401 (:status res))))))

;patch: /api/user/{id}
(deftest user-patch-by-id
  (testing "lab assistant, user-patch-by-id, patch self"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-one)
                                user-one)]
      (is (= 401 (:status res)))))
  (testing "instructor, user-patch-by-id, patch self"
    (let [user-one (db-pop/add-user (:instructor env))
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-one)
                                user-one)]
      (is (= 401 (:status res)))))
  (testing "student, user-patch-by-id, patch self"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-one)
                                user-one)]
      (is (= 401 (:status res))))))

;get: /api/user/{id}/collections
(deftest user-id-collections
  (testing "student, user-id-collections, user is self"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/user-id-collections (uc/user-id-to-session-id (:id user-one))
                                      (:id user-one))]
      (is (= 200 (:status res))))))

;get: /api/user/{id}/courses
(deftest user-id-courses
  (testing "student, user-id-courses, user is self"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/user-id-courses (uc/user-id-to-session-id (:id user-one))
                                  (:id user-one))]
      (is (= 200 (:status res))))))

;get: /api/user/{id}/words
(deftest user-id-words
  (testing "student, user-id-words, user is self"
    (let [user-one (db-pop/add-user (:student env))
          res (rp/user-id-get-words (uc/user-id-to-session-id (:id user-one))
                                    (:id user-one))]
      (is (= 200 (:status res))))))
