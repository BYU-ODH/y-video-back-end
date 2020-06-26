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
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resource-files-assoc :as resource-files-assoc]
      [y-video-back.db.resources :as resources]
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
  (mount.core/start #'y-video-back.handler/app))

(deftest user-post
  (testing "add duplicated user"
    (let [new-user (g/get-random-user-without-id)
          add-user-res (users/CREATE new-user)
          res (rp/user-post new-user)]
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

(deftest user-get-collections-logged-in
  (testing "invalid session-id"
    (let [res (rp/collections-by-logged-in (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest user-get-logged-in
  (testing "invalid session-id"
    (let [res (rp/get-current-user (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))
