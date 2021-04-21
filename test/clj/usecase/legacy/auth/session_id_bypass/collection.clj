(ns legacy.auth.session-id-bypass.collection
  {:mock-prod true}
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer [use-fixtures deftest testing is]]
    [legacy.db.test-util :as tcore]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [legacy.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
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

(deftest temp-test
  (testing "temp"
    (is true)))

; post:  /api/collection
(deftest collection-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-post (db-pop/get-collection))]
        (is (= 401 (:status res)))))))

; get:  /api/collection/{id}
(deftest collection-get-by-id
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-get (:id (db-pop/add-collection)))]
        (is (= 401 (:status res)))))))

; delete:  /api/collection/{id}
(deftest collection-delete-by-id
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-delete (:id (db-pop/add-collection)))]
        (is (= 401 (:status res)))))))

; patch:  /api/collection/{id}
(deftest collection-patch-by-id
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-patch (:id (db-pop/add-collection))
                                        (db-pop/get-collection))]
        (is (= 401 (:status res)))))))

; post:  /api/collection/{id}/add-course
(deftest collection-id-add-course
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-add-course (:id (db-pop/add-collection))
                                             "test" "test" "test")]
        (is (= 401 (:status res)))))))

; post:  /api/collection/{id}/add-user
(deftest collection-id-add-user
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-add-user (:id (db-pop/add-collection))
                                           (:username (db-pop/add-user))
                                           0)]
        (is (= 401 (:status res)))))))

; get:  /api/collection/{id}/contents
(deftest collection-id-contents
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-contents (:id (db-pop/add-collection)))]
        (is (= 401 (:status res)))))))

; get:  /api/collection/{id}/courses
(deftest collection-id-courses
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-courses (:id (db-pop/add-collection)))]
        (is (= 401 (:status res)))))))

; post:  /api/collection/{id}/remove-course
(deftest collection-id-remove-course
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-remove-course (:id (db-pop/add-collection))
                                                (:id (db-pop/add-course)))]
        (is (= 401 (:status res)))))))

; post:  /api/collection/{id}/remove-user
(deftest collection-id-remove-user
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-remove-user (:id (db-pop/add-collection))
                                              (:username (db-pop/add-user)))]
        (is (= 401 (:status res)))))))

; get:  /api/collection/{id}/users
(deftest collection-id-users
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collection-id-users (:id (db-pop/add-collection)))]
        (is (= 401 (:status res)))))))

; get:  /api/collections
(deftest collections-by-logged-in
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/collections-by-logged-in (:session-id-bypass env))]
        (is (= 401 (:status res)))))))
