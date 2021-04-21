(ns legacy.auth.session-id-bypass.course
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

; post:  /api/course
(deftest course-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-post (db-pop/get-course))]
        (is (= 401 (:status res)))))))

; get:  /api/course/{id}
(deftest course-id-get
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-get (:id (db-pop/add-course)))]
        (is (= 401 (:status res)))))))

; delete:  /api/course/{id}
(deftest course-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-delete (:id (db-pop/add-course)))]
        (is (= 401 (:status res)))))))

; patch:  /api/course/{id}
(deftest course-id-patch
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-patch (:id (db-pop/add-course))
                                    (db-pop/get-course))]
        (is (= 401 (:status res)))))))

; post:  /api/course/{id}/add-user
(deftest course-id-add-user
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-add-user (:id (db-pop/add-course))
                                       (:id (db-pop/add-user))
                                       0)]
        (is (= 401 (:status res)))))))

; get:  /api/course/{id}/collections
(deftest course-id-collections
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-collections (:id (db-pop/add-course)))]
        (is (= 401 (:status res)))))))

; post:  /api/course/{id}/remove-user
(deftest course-id-remove-user
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-remove-user (:id (db-pop/add-course))
                                          (:id (db-pop/add-user)))]
        (is (= 401 (:status res)))))))

; get:  /api/course/{id}/users
(deftest course-id-users
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/course-id-users (:id (db-pop/add-course)))]
        (is (= 401 (:status res)))))))
