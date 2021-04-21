(ns legacy.routes.error-code.user-tests
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
      [y-video-back.db.users :as users]
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]))

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

(deftest user-post
  (testing "add duplicated user"
    (let [new-user (g/get-random-user-without-id)
          add-user-res (users/CREATE new-user)
          res (rp/user-post new-user)]
      (is (= 500 (:status res))))))

(deftest user-id-get
  (testing "read nonexistent user"
    (let [res (rp/user-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "nil instead of id"
    (db-pop/add-user)
    (let [res (rp/user-id-get nil)]
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
      (is (= 401 (:status res))))))

(deftest user-get-logged-in
  (testing "invalid session-id"
    (let [res (rp/get-current-user (java.util.UUID/randomUUID))]
      (is (= 401 (:status res))))))










