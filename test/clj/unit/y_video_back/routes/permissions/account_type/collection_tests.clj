(ns y-video-back.routes.permissions.account-type.collection-tests
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

;get: /api/collections
(deftest collections-get-by-logged-in
  (testing "admin collections-get-by-logged-in"
    (let [user-one (db-pop/add-user "admin")
          res (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "lab assistant collections-get-by-logged-in"
    (let [user-one (db-pop/add-user "lab-assistant")
          res (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "instructor collections-get-by-logged-in"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "student collections-get-by-logged-in"
    (let [user-one (db-pop/add-user "student")
          res (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res))))))

;post: /api/collection
(deftest collection-post
  (testing "admin - not owner, collection-post"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-two))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - not owner, collection-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-two))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - not owner, collection-post"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-two))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 403 (:status res)))))
  (testing "student - not owner, collection-post"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-two))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 403 (:status res))))))

;get: /api/collection/{id}
(deftest collection-get-by-id
  (testing "admin - no connection, collection-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-get-by-id"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 403 (:status res))))))

;delete: /api/collection/{id}
(deftest collection-delete-by-id
  (testing "admin - no connection, collection-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, collection-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 403 (:status res))))))


;patch: /api/collection/{id}
(deftest collection-patch-by-id
  (testing "admin - no connection, collection-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      (dissoc coll-one :id))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      (dissoc coll-one :id))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      (dissoc coll-one :id))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      (dissoc coll-one :id))]
      (is (= 403 (:status res))))))

;post: /api/collection/{id}/add-user
(deftest collection-add-user
  (testing "admin - no connection, collection-add-user"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-add-user"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-add-user"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-add-user"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)]
      (is (= 403 (:status res))))))

;post: /api/collection/{id}/remove-user
(deftest collection-remove-user
  (testing "admin - no connection, collection-remove-user"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-add-res (db-pop/add-user-coll-assoc (:username user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-remove-user"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-add-res (db-pop/add-user-coll-assoc (:username user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-remove-user"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-add-res (db-pop/add-user-coll-assoc (:username user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-remove-user"
    (let [user-one (db-pop/add-user "student")
          user-two (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-add-res (db-pop/add-user-coll-assoc (:username user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two))]
      (is (= 403 (:status res))))))

;post: /api/collection/{id}/add-course
(deftest collection-add-course
  (testing "admin - no connection, collection-add-course"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/get-course)
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:department crse-one)
                                           (:catalog-number crse-one)
                                           (:section-number crse-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-add-course"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/get-course)
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:department crse-one)
                                           (:catalog-number crse-one)
                                           (:section-number crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-add-course"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/get-course)
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:department crse-one)
                                           (:catalog-number crse-one)
                                           (:section-number crse-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-add-course"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/get-course)
          coll-one (db-pop/add-collection)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:department crse-one)
                                           (:catalog-number crse-one)
                                           (:section-number crse-one))]
      (is (= 403 (:status res))))))

;post: /api/collection/{id}/remove-course
(deftest collection-remove-course
  (testing "admin - no connection, collection-remove-course"
    (let [user-one (db-pop/add-user "admin")
          crse-one (db-pop/add-course)
          coll-one (db-pop/add-collection)
          crse-add-res (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-remove-course"
    (let [user-one (db-pop/add-user "lab-assistant")
          crse-one (db-pop/add-course)
          coll-one (db-pop/add-collection)
          crse-add-res (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-remove-course"
    (let [user-one (db-pop/add-user "instructor")
          crse-one (db-pop/add-course)
          coll-one (db-pop/add-collection)
          crse-add-res (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-remove-course"
    (let [user-one (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          coll-one (db-pop/add-collection)
          crse-add-res (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-one))]
      (is (= 403 (:status res))))))

;get: /api/collection/{id}/contents
(deftest collection-id-contents
  (testing "admin - no connection, collection-id-contents"
    (let [user-one (db-pop/add-user "admin")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-id-contents"
    (let [user-one (db-pop/add-user "lab-assistant")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-id-contents"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-id-contents"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 403 (:status res))))))

;get: /api/collection/{id}/courses
(deftest collection-id-courses
  (testing "admin - no connection, collection-id-courses"
    (let [user-one (db-pop/add-user "admin")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-id-courses"
    (let [user-one (db-pop/add-user "lab-assistant")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-id-courses"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-id-courses"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 403 (:status res))))))


;get: /api/collection/{id}/users
(deftest collection-id-users
  (testing "admin - no connection, collection-id-users"
    (let [user-one (db-pop/add-user "admin")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, collection-id-users"
    (let [user-one (db-pop/add-user "lab-assistant")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, collection-id-users"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, collection-id-users"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 403 (:status res))))))
