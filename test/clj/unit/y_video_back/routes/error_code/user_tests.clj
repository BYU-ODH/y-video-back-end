(ns y-video-back.routes.error-code.user-tests
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
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-two)}))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-thr)}))))
  (def test-user-coll-one (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                                                              :collection-id (:id test-coll-one)
                                                                              :account-role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-two)
                                                                              :collection-id (:id test-coll-two)
                                                                              :account-role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-thr)
                                                                              :collection-id (:id test-coll-thr)
                                                                              :account-role 0})))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-thr)))))
  (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-one) (:id test-cont-one)))))
  (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-two) (:id test-cont-two)))))
  (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-thr) (:id test-cont-thr)))))
  (mount.core/start #'y-video-back.handler/app))

(deftest user-post
  (testing "add duplicated user"
    (let [res (rp/user-post test-user-one)]
      (is (= 500 (:status res))))))

(deftest user-id-get
  (testing "read nonexistent user"
    (let [res (rp/user-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest user-id-patch
  (testing "update nonexistent user"
    (let [res (rp/user-id-patch (java.util.UUID/randomUUID) (g/get-random-user-without-id))]
      (is (= 404 (:status res))))))

(deftest user-id-delete
  (testing "delete nonexistent user"
    (let [res (rp/user-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest user-id-get-all-collections
  (testing "get all collections for nonexistent user"
    (let [res (rp/user-id-collections (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get all collections for user with no collections"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          res (rp/user-id-collections (:id new-user))]
      (is (= 200 (:status res)))
      (is (= [] (m/decode-response-body res))))))

(deftest user-id-get-all-courses
  (testing "get all courses for nonexistent user"
    (let [res (rp/user-id-courses (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get all courses for user with no courses"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          res (rp/user-id-courses (:id new-user))]
      (is (= 200 (:status res)))
      (is (= [] (m/decode-response-body res))))))

(deftest user-id-get-all-words
  (testing "get all words for nonexistent user"
    (let [res (rp/user-id-get-words (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get all words for user with no words"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          res (rp/user-id-get-words (:id new-user))]
      (is (= 200 (:status res)))
      (is (= [] (m/decode-response-body res))))))
