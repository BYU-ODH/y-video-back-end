(ns legacy.routes.permissions.account-type.course-tests
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

;post: /api/course
(deftest course-post
  (testing "admin, course-post"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/get-course)
          res (rp/course-post (uc/user-id-to-session-id (:id user-one))
                              crse-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant, course-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/get-course)
          res (rp/course-post (uc/user-id-to-session-id (:id user-one))
                              crse-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, course-post"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/get-course)
          res (rp/course-post (uc/user-id-to-session-id (:id user-one))
                              crse-one)]
      (is (= 200 (:status res)))))
  (testing "student, course-post"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/get-course)
          res (rp/course-post (uc/user-id-to-session-id (:id user-one))
                              crse-one)]
      (is (= 403 (:status res))))))

;get: /api/course/{id}
(deftest course-get-by-id
  (testing "admin - no connection, course-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/add-course)
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, course-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/add-course)
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, course-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/add-course)
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student - no connection, course-get-by-id"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 403 (:status res))))))

;delete: /api/course/{id}
(deftest course-delete-by-id
  (testing "admin - no connection, course-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/add-course)
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, course-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/add-course)
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, course-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/add-course)
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, course-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res))))))

;patch: /api/course/{id}
(deftest course-patch-by-id
  (testing "admin - no connection, course-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/add-course)
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, course-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/add-course)
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, course-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/add-course)
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, course-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res))))))

;get: /api/course/{id}/collections
(deftest course-id-collections
  (testing "admin - no connection, course-id-collections"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/add-course)
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, course-id-collections"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/add-course)
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, course-id-collections"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/add-course)
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, course-id-collections"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 403 (:status res))))))

;post: /api/course/{id}/add-user
(deftest course-id-add-user
  (testing "admin- no connection, course-id-add-user"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)]
      (is (= 200 (:status res)))))
  (testing "lab assistant- no connection, course-id-add-user"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)]
      (is (= 200 (:status res)))))
  (testing "instructor- no connection, course-id-add-user"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)]
      (is (= 403 (:status res)))))
  (testing "student- no connection, course-id-add-user"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)]
      (is (= 403 (:status res))))))

;post: /api/course/{id}/remove-user
(deftest course-id-remove-user
  (testing "admin- no connection, course-id-remove-user"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          user-add-res (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant- no connection, course-id-remove-user"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          user-add-res (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor- no connection, course-id-remove-user"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          user-add-res (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student- no connection, course-id-remove-user"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          user-add-res (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res))))))

;get: /api/course/{id}/users
(deftest course-id-users
  (testing "admin - no connection, course-id-users"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/add-course)
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, course-id-users"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/add-course)
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, course-id-users"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/add-course)
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, course-id-users"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res))))))
