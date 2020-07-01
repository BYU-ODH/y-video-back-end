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
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.contents :as contents]
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
  ;(def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  ;(def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-one)))))
  (mount.core/start #'y-video-back.handler/app))

(deftest file-post
  (testing "add duplicated file (i.e. duplicate filepath)"
    (let [new-file (g/get-random-file-without-id (:id test-rsrc-one))
          add-file-res (files/CREATE new-file)
          res (rp/file-post new-file)]
      (is (= 500 (:status res)))))
  (testing "add file to nonexistent resource"
    (let [new-file (g/get-random-file-without-id (java.util.UUID/randomUUID))
          res(rp/file-post new-file)]
      (is (= 500 (:status res))))))

(deftest file-id-get
  (testing "read nonexistent file"
    (let [res (rp/file-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest file-id-patch
  (testing "update nonexistent file"
    (let [res (rp/file-id-patch (java.util.UUID/randomUUID) (g/get-random-file-without-id (:id test-rsrc-one)))]
      (is (= 404 (:status res)))))
  (testing "update file to taken filepath (all fields)"
    (let [file-one (g/get-random-file-without-id (:id test-rsrc-one))
          file-two (g/get-random-file-without-id (:id test-rsrc-one))
          file-one-res (files/CREATE file-one)
          file-two-res (files/CREATE file-two)
          res (rp/file-id-patch (:id file-two-res) file-one)]
      (is (= 500 (:status res)))))
  (testing "update file to taken filepath (filepath)"
    (let [file-one (g/get-random-file-without-id (:id test-rsrc-one))
          file-two (g/get-random-file-without-id (:id test-rsrc-one))
          file-one-res (files/CREATE file-one)
          file-two-res (files/CREATE file-two)
          res (rp/file-id-patch (:id file-two-res) {:filepath (:filepath file-one)})]
      (is (= 500 (:status res)))))
  (testing "update file to nonexistent resource (all fields)"
    (let [file-one (g/get-random-file-without-id (:id test-rsrc-one))
          file-two (g/get-random-file-without-id (java.util.UUID/randomUUID))
          file-one-res (files/CREATE file-one)
          res (rp/file-id-patch (:id file-one-res) file-two)]
      (is (= 500 (:status res)))))
  (testing "update file to nonexistent resource (resource-id only)"
    (let [file-one (g/get-random-file-without-id (:id test-rsrc-one))
          file-one-res (files/CREATE file-one)
          res (rp/file-id-patch (:id file-one-res) {:resource-id (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res))))))

(deftest file-id-delete
  (testing "delete nonexistent file"
    (let [res (rp/file-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))
