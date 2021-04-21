(ns legacy.routes.permissions.account-type.admin-tests
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


;get: /api/admin/user/{term}
(deftest admin-search-users
  (testing "admin, admin-search-users"
    (let [user-one (db-pop/add-user "admin")
          res (rp/search-by-user (uc/user-id-to-session-id (:id user-one))
                                 "test")]
      (is (= 200 (:status res)))))
  (testing "lab assistant, admin-search-users"
    (let [user-one (db-pop/add-user "lab-assistant")
          res (rp/search-by-user (uc/user-id-to-session-id (:id user-one))
                                 "test")]
      (is (= 200 (:status res)))))
  (testing "instructor, admin-search-users"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/search-by-user (uc/user-id-to-session-id (:id user-one))
                                 "test")]
      (is (= 200 (:status res)))))
  (testing "student, admin-search-users"
    (let [user-one (db-pop/add-user "student")
          res (rp/search-by-user (uc/user-id-to-session-id (:id user-one))
                                 "test")]
      (is (= 403 (:status res))))))


;get: /api/admin/collection/{term}
(deftest admin-search-collections
  (testing "admin, admin-search-collections"
    (let [user-one (db-pop/add-user "admin")
          res (rp/search-by-collection (uc/user-id-to-session-id (:id user-one))
                                       "test")]
      (is (= 200 (:status res)))))
  (testing "lab assistant, admin-search-collections"
    (let [user-one (db-pop/add-user "lab-assistant")
          res (rp/search-by-collection (uc/user-id-to-session-id (:id user-one))
                                       "test")]
      (is (= 200 (:status res)))))
  (testing "instructor, admin-search-collections"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/search-by-collection (uc/user-id-to-session-id (:id user-one))
                                       "test")]
      (is (= 403 (:status res)))))
  (testing "student, admin-search-collections"
    (let [user-one (db-pop/add-user "student")
          res (rp/search-by-collection (uc/user-id-to-session-id (:id user-one))
                                       "test")]
      (is (= 403 (:status res))))))

;get: /api/admin/content/{term}
(deftest admin-search-contents
  (testing "admin, admin-search-contents"
    (let [user-one (db-pop/add-user "admin")
          res (rp/search-by-content (uc/user-id-to-session-id (:id user-one))
                                    "test")]
      (is (= 200 (:status res)))))
  (testing "lab assistant, admin-search-contents"
    (let [user-one (db-pop/add-user "lab-assistant")
          res (rp/search-by-content (uc/user-id-to-session-id (:id user-one))
                                    "test")]
      (is (= 200 (:status res)))))
  (testing "instructor, admin-search-contents"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/search-by-content (uc/user-id-to-session-id (:id user-one))
                                    "test")]
      (is (= 403 (:status res)))))
  (testing "student, admin-search-contents"
    (let [user-one (db-pop/add-user "student")
          res (rp/search-by-content (uc/user-id-to-session-id (:id user-one))
                                    "test")]
      (is (= 403 (:status res))))))

;get: /api/admin/resource/{term}
(deftest admin-search-resources
  (testing "admin, admin-search-resources"
    (let [user-one (db-pop/add-user "admin")
          res (rp/search-by-resource (uc/user-id-to-session-id (:id user-one))
                                     "test")]
      (is (= 200 (:status res)))))
  (testing "lab assistant, admin-search-resources"
    (let [user-one (db-pop/add-user "lab-assistant")
          res (rp/search-by-resource (uc/user-id-to-session-id (:id user-one))
                                     "test")]
      (is (= 200 (:status res)))))
  (testing "instructor, admin-search-resources"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/search-by-resource (uc/user-id-to-session-id (:id user-one))
                                     "test")]
      (is (= 200 (:status res)))))
  (testing "student, admin-search-resources"
    (let [user-one (db-pop/add-user "student")
          res (rp/search-by-resource (uc/user-id-to-session-id (:id user-one))
                                     "test")]
      (is (= 403 (:status res))))))
