(ns legacy.routes.permissions.account-type.subtitle-tests
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.test :refer :all]
    [legacy.db.test-util :as tcore]
    [legacy.utils.db-populator :as db-pop]
    [legacy.utils.route-proxy.proxy :as rp]
    [legacy.utils.utils :as ut]
    [mount.core :as mount]
    [y-video-back.config :refer [env]]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.handler :refer :all]
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

;post: /api/subtitle
(deftest subtitle-post
  (testing "admin - no connection, subtitle-post"
    (let [user-one (db-pop/add-user "admin")
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, subtitle-post"
    (let [user-one (db-pop/add-user "instructor")
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, subtitle-post"
    (let [user-one (db-pop/add-user "student")
          sbtl-one (db-pop/get-subtitle)
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 403 (:status res))))))

;get: /api/subtitle/{id}
(deftest subtitle-id-get
  (testing "admin - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user "admin")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user "lab-assistant")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user "instructor")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, subtitle-id-get"
    (let [user-one (db-pop/add-user "student")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 403 (:status res))))))

;delete: /api/subtitle/{id}
(deftest subtitle-id-delete
  (testing "admin - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user "admin")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user "lab-assistant")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user "instructor")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, subtitle-id-delete"
    (let [user-one (db-pop/add-user "student")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res))))))

;patch: /api/subtitle/{id}
(deftest subtitle-id-patch
  (testing "admin - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user "admin")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user "lab-assistant")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user "instructor")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, subtitle-id-patch"
    (let [user-one (db-pop/add-user "student")
          sbtl-one (db-pop/add-subtitle)
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 403 (:status res))))))
