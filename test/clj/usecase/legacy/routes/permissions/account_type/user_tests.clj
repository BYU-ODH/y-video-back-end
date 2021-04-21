(ns legacy.routes.permissions.account-type.user-tests
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
