(ns y-video-back.routes.permissions.account-type.file-tests
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
