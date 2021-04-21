(ns legacy.routes.permissions.account-type.file-tests
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
    (f)
    (ut/delete-all-files (-> env :FILES :media-url))
    (ut/delete-all-files (-> env :FILES :test-temp))))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

;post: /api/file
(deftest file-post
  (testing "admin - no connection, file-post"
    (let [user-one (db-pop/add-user "admin")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res (rp/file-post (uc/user-id-to-session-id (:id user-one))
                            file-one
                            filecontent)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, file-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res (rp/file-post (uc/user-id-to-session-id (:id user-one))
                            file-one
                            filecontent)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, file-post"
    (let [user-one (db-pop/add-user "instructor")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res (rp/file-post (uc/user-id-to-session-id (:id user-one))
                            file-one
                            filecontent)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, file-post"
    (let [user-one (db-pop/add-user "student")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res (rp/file-post (uc/user-id-to-session-id (:id user-one))
                            file-one
                            filecontent)]
      (is (= 403 (:status res))))))

;get: /api/file/{id}
(deftest file-get-by-id
  (testing "admin - no connection, file-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          file-one (db-pop/add-file)
          res (rp/file-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, file-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          file-one (db-pop/add-file)
          res (rp/file-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, file-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          file-one (db-pop/add-file)
          res (rp/file-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student - no connection, file-get-by-id"
    (let [user-one (db-pop/add-user "student")
          file-one (db-pop/add-file)
          res (rp/file-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id file-one))]
      (is (= 403 (:status res))))))

;delete: /api/file/{id}
(deftest file-delete-by-id
  (testing "admin - no connection, file-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          file-one (db-pop/add-file)
          res (rp/file-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, file-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          file-one (db-pop/add-file)
          res (rp/file-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, file-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          file-one (db-pop/add-file)
          res (rp/file-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id file-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, file-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          file-one (db-pop/add-file)
          res (rp/file-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id file-one))]
      (is (= 403 (:status res))))))

;patch: /api/file/{id}
(deftest file-patch-by-id
  (testing "admin - no connection, file-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          file-one (db-pop/add-file)
          res (rp/file-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id file-one)
                                file-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, file-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          file-one (db-pop/add-file)
          res (rp/file-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id file-one)
                                file-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, file-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          file-one (db-pop/add-file)
          res (rp/file-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id file-one)
                                file-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, file-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          file-one (db-pop/add-file)
          res (rp/file-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id file-one)
                                file-one)]
      (is (= 403 (:status res))))))
