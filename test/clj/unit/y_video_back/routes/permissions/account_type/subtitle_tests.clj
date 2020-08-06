(ns y-video-back.routes.permissions.account-type.subtitle-tests
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

;post: /api/subtitle
(deftest subtitle-post
  (testing "admin - no connection, subtitle-post"
    (let [user-one (db-pop/add-user (:admin env))
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-post"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, subtitle-post"
    (let [user-one (db-pop/add-user (:instructor env))
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 401 (:status res)))))
  (testing "student - no connection, subtitle-post"
    (let [user-one (db-pop/add-user (:student env))
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 401 (:status res))))))

;get: /api/subtitle/{id}
(deftest subtitle-id-get
  (testing "admin - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user (:admin env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user (:instructor env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 401 (:status res)))))
  (testing "student - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user (:student env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 401 (:status res))))))

;delete: /api/subtitle/{id}
(deftest subtitle-id-delete
  (testing "admin - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user (:admin env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 401 (:status res)))))
  (testing "instructor - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user (:instructor env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 401 (:status res)))))
  (testing "student - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user (:student env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 401 (:status res))))))

;patch: /api/subtitle/{id}
(deftest subtitle-id-patch
  (testing "admin - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user (:admin env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user (:lab-assistant env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user (:instructor env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 401 (:status res)))))
  (testing "student - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user (:student env))
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 401 (:status res))))))
