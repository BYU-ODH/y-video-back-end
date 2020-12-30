(ns legacy.routes.permissions.account-type.user-tests
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
    [legacy.utils.utils :as ut]
    [legacy.utils.db-populator :as db-pop]
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

;get: /api/user
(deftest user-get-by-logged-in
  (testing "admin user-get-by-logged-in"
    (let [user-one (db-pop/add-user "admin")
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "lab assistant user-get-by-logged-in"
    (let [user-one (db-pop/add-user "lab-assistant")
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "instructor user-get-by-logged-in"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "student user-get-by-logged-in"
    (let [user-one (db-pop/add-user "student")
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res))))))

;post: /api/user
(deftest user-post
  (testing "admin user-post"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/get-user)
          res (rp/user-post (uc/user-id-to-session-id (:id user-one))
                            user-two)]
      (is (= 200 (:status res)))))
  (testing "lab assistant user-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/get-user)
          res (rp/user-post (uc/user-id-to-session-id (:id user-one))
                            user-two)]
      (is (= 403 (:status res)))))
  (testing "instructor user-post"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/get-user)
          res (rp/user-post (uc/user-id-to-session-id (:id user-one))
                            user-two)]
      (is (= 403 (:status res)))))
  (testing "student user-post"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/get-user)
          res (rp/user-post (uc/user-id-to-session-id (:id user-one))
                            user-two)]
      (is (= 403 (:status res))))))

;get: /api/user/{id}
(deftest user-get-by-id
  (testing "admin - no connection, user-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          res (rp/user-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, user-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          res (rp/user-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, user-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          res (rp/user-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "student - no connection, user-get-by-id"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          res (rp/user-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id user-two))]
      (is (= 403 (:status res))))))

;delete: /api/user/{id}
(deftest user-delete-by-id
  (testing "admin - no connection, user-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, user-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, user-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, user-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          res (rp/user-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id user-two))]
      (is (= 403 (:status res))))))

;patch: /api/user/{id}
(deftest user-patch-by-id
  (testing "admin - no connection, user-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-two)
                                user-two)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, user-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-two)
                                user-two)]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, user-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-two)
                                user-two)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, user-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          res (rp/user-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id user-two)
                                user-two)]
      (is (= 403 (:status res))))))

;get: /api/user/{id}/collections
(deftest user-id-collections
  (testing "admin - no connection, user-id-collections"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          res (rp/user-id-collections (uc/user-id-to-session-id (:id user-one))
                                      (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, user-id-collections"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          res (rp/user-id-collections (uc/user-id-to-session-id (:id user-one))
                                      (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, user-id-collections"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          res (rp/user-id-collections (uc/user-id-to-session-id (:id user-one))
                                      (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, user-id-collections"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          res (rp/user-id-collections (uc/user-id-to-session-id (:id user-one))
                                      (:id user-two))]
      (is (= 403 (:status res))))))

;get: /api/user/{id}/courses
(deftest user-id-courses
  (testing "admin - no connection, user-id-courses"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          res (rp/user-id-courses (uc/user-id-to-session-id (:id user-one))
                                  (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, user-id-courses"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          res (rp/user-id-courses (uc/user-id-to-session-id (:id user-one))
                                  (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, user-id-courses"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          res (rp/user-id-courses (uc/user-id-to-session-id (:id user-one))
                                  (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, user-id-courses"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          res (rp/user-id-courses (uc/user-id-to-session-id (:id user-one))
                                  (:id user-two))]
      (is (= 403 (:status res))))))

;get: /api/user/{id}/words
(deftest user-id-words
  (testing "admin - no connection, user-id-words"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          res (rp/user-id-get-words (uc/user-id-to-session-id (:id user-one))
                                    (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, user-id-words"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          res (rp/user-id-get-words (uc/user-id-to-session-id (:id user-one))
                                    (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, user-id-words"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          res (rp/user-id-get-words (uc/user-id-to-session-id (:id user-one))
                                    (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, user-id-words"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          res (rp/user-id-get-words (uc/user-id-to-session-id (:id user-one))
                                    (:id user-two))]
      (is (= 403 (:status res))))))
