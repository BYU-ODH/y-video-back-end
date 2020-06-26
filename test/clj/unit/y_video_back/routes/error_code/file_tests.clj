(ns y-video-back.routes.error-code.file-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.annotations :as annotations]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resource-files-assoc :as resource-files-assoc]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  ;(def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  ;(def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  ;(def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  ;(def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  ;(def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (mount.core/start #'y-video-back.handler/app))

(deftest file-post
  (testing "add duplicated file"
    (let [new-file (g/get-random-file-without-id)
          add-file-res (files/CREATE new-file)
          res (rp/file-post new-file)]
      (is (= 500 (:status res))))))

(deftest file-id-get
  (testing "read nonexistent file"
    (let [res (rp/file-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest file-id-patch
  (testing "update nonexistent file"
    (let [res (rp/file-id-patch (java.util.UUID/randomUUID) (g/get-random-file-without-id))]
      (is (= 404 (:status res)))))
  (testing "update file to taken filepath (all fields)"
    (let [crse-one (g/get-random-file-without-id)
          crse-two (g/get-random-file-without-id)
          crse-one-res (files/CREATE crse-one)
          crse-two-res (files/CREATE crse-two)
          res (rp/file-id-patch (:id crse-two-res) crse-one)]
      (is (= 500 (:status res)))))
  (testing "update file to taken filepath (filepath)"
    (let [crse-one (g/get-random-file-without-id)
          crse-two (g/get-random-file-without-id)
          crse-one-res (files/CREATE crse-one)
          crse-two-res (files/CREATE crse-two)
          res (rp/file-id-patch (:id crse-two-res) {:filepath (:filepath crse-one)})]
      (is (= 500 (:status res))))))

(deftest file-id-delete
  (testing "delete nonexistent file"
    (let [res (rp/file-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest file-get-all-resources
  (testing "get all resources for nonexistent file"
    (let [res (rp/file-id-resources (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get all resources for file with no resources"
    (let [new-file (files/CREATE (g/get-random-file-without-id))
          res (rp/file-id-resources (:id new-file))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))
