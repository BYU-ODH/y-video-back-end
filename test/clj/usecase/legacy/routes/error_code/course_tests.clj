(ns legacy.routes.error-code.course-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.db.users :as users]
      [legacy.utils.db-populator :as db-pop]
      [legacy.utils.utils :as ut]))

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
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (db-pop/add-file (:id test-rsrc-one)))
  (mount.core/start #'y-video-back.handler/app))

(deftest course-post
  (testing "add duplicated course"
    (let [new-course (g/get-random-course-without-id)
          add-course-res (courses/CREATE new-course)
          res (rp/course-post new-course)]
      (is (= 500 (:status res))))))

(deftest course-id-get
  (testing "read nonexistent course"
    (let [res (rp/course-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest course-id-patch
  (testing "update nonexistent course"
    (let [res (rp/course-id-patch (java.util.UUID/randomUUID) (g/get-random-course-without-id))]
      (is (= 404 (:status res)))))
  (testing "update course to taken department-catalog-section (all fields)"
    (let [crse-one (g/get-random-course-without-id)
          crse-two (g/get-random-course-without-id)
          crse-one-res (courses/CREATE crse-one)
          crse-two-res (courses/CREATE crse-two)
          res (rp/course-id-patch (:id crse-two-res) crse-one)]
      (is (= 500 (:status res)))))
  (testing "update course to taken department-catalog-section (department)"
    (let [crse-one (g/get-random-course-without-id)
          crse-two (g/get-random-course-without-id)
          crse-one-res (courses/CREATE crse-one)
          crse-two-res (courses/CREATE crse-two)
          res-setup (rp/course-id-patch (:id crse-two-res) {:catalog-number (:catalog-number crse-one)
                                                            :section-number (:section-number crse-one)})
          res (rp/course-id-patch (:id crse-two-res) {:department (:department crse-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res)))))
  (testing "update course to taken department-catalog-section (catalog)"
    (let [crse-one (g/get-random-course-without-id)
          crse-two (g/get-random-course-without-id)
          crse-one-res (courses/CREATE crse-one)
          crse-two-res (courses/CREATE crse-two)
          res-setup (rp/course-id-patch (:id crse-two-res) {:department (:department crse-one)
                                                            :section-number (:section-number crse-one)})
          res (rp/course-id-patch (:id crse-two-res) {:catalog-number (:catalog-number crse-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res)))))
  (testing "update course to taken department-catalog-section (section)"
    (let [crse-one (g/get-random-course-without-id)
          crse-two (g/get-random-course-without-id)
          crse-one-res (courses/CREATE crse-one)
          crse-two-res (courses/CREATE crse-two)
          res-setup (rp/course-id-patch (:id crse-two-res) {:catalog-number (:catalog-number crse-one)
                                                            :department (:department crse-one)})
          res (rp/course-id-patch (:id crse-two-res) {:section-number (:section-number crse-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res))))))
(deftest course-id-delete
  (testing "delete nonexistent course"
    (let [res (rp/course-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest course-add-user
  (testing "add nonexistent user to course"
    (let [res (rp/course-id-add-user (:id test-crse-one) (java.util.UUID/randomUUID) 0)]
      (is (= 500 (:status res)))))
  (testing "add user to nonexistent course"
    (let [res (rp/course-id-add-user (java.util.UUID/randomUUID) (:id test-user-one) 0)]
      (is (= 404 (:status res)))))
  (testing "add user to course, already connected"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          connect-user-res (user-courses-assoc/CREATE {:user-id (:id new-user)
                                                       :course-id (:id test-crse-one)})
          res (rp/course-id-add-user (:id test-crse-one) (:id new-user) 0)]
      (is (= 500 (:status res))))))

(deftest course-remove-user
  (testing "remove nonexistent user from course"
    (let [res (rp/course-id-remove-user (:id test-crse-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "remove user from nonexistent course"
    (let [res (rp/course-id-remove-user (java.util.UUID/randomUUID) (:id test-user-one))]
      (is (= 404 (:status res)))))
  (testing "remove user from course, not connected"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          res (rp/course-id-remove-user (:id test-crse-one) (:id new-user))]
      (is (= 500 (:status res))))))

(deftest course-get-all-collections
  (testing "get all collections for nonexistent course"
    (let [res (rp/course-id-collections (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get all collections for course with no collections"
    (let [new-crse (courses/CREATE (g/get-random-course-without-id))
          res (rp/course-id-collections (:id new-crse))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest course-get-all-users
  (testing "get all users for nonexistent course"
    (let [res (rp/course-id-users (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get all users for course with no users"
    (let [new-crse (courses/CREATE (g/get-random-course-without-id))
          res (rp/course-id-users (:id new-crse))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))







