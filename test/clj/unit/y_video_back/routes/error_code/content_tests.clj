(ns y-video-back.routes.error-code.content-tests
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
      [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.content-files-assoc :as content-files-assoc]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
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
  ;(def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  ;(def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (mount.core/start #'y-video-back.handler/app))

; The only error thrown for content-post is request body coercion (400)

(deftest content-id-get
  (testing "read nonexistent content"
    (let [res (rp/content-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-id-patch
  (testing "update nonexistent content"
    (let [res (rp/content-id-patch (java.util.UUID/randomUUID) (g/get-random-content-without-id))]
      (is (= 404 (:status res))))))

(deftest content-id-delete
  (testing "delete nonexistent content"
    (let [res (rp/content-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-get-all-collections
  (testing "get collections for nonexistent content"
    (let [res (rp/content-id-collections (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get collections for content with no collections"
    (let [new-content (g/get-random-content-without-id)
          add-cont-res (contents/CREATE new-content)
          res (rp/content-id-collections (:id add-cont-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest content-get-all-files
  (testing "get files for nonexistent content"
    (let [res (rp/content-id-files (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get files for content with no files"
    (let [new-content (g/get-random-content-without-id)
          add-cont-res (contents/CREATE new-content)
          res (rp/content-id-files (:id add-cont-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest content-add-view
  (testing "add view to nonexistent content"
    (let [res (rp/content-id-add-view (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-add-file
  (testing "add nonexistent file to content"
    (let [res (rp/content-id-add-file (:id test-cont-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "add file to nonexistent content"
    (let [res (rp/content-id-add-file (java.util.UUID/randomUUID) (:id test-file-one))]
      (is (= 404 (:status res)))))
  (testing "add file to content, already connected"
    (let [new-file (files/CREATE (g/get-random-file-without-id))
          connect-file-res (content-files-assoc/CREATE {:file-id (:id new-file)
                                                        :content-id (:id test-cont-one)})
          res (rp/content-id-add-file (:id test-cont-one) (:id new-file))]
      (is (= 500 (:status res))))))

(deftest content-remove-file
  (testing "remove nonexistent file from content"
    (let [res (rp/content-id-remove-file (:id test-cont-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "remove file from nonexistent content"
    (let [res (rp/content-id-remove-file (java.util.UUID/randomUUID) (:id test-file-one))]
      (is (= 404 (:status res)))))
  (testing "remove file from content, not connected"
    (let [new-file (files/CREATE (g/get-random-file-without-id))
          res (rp/content-id-remove-file (:id test-cont-one) (:id new-file))]
      (is (= 500 (:status res))))))
