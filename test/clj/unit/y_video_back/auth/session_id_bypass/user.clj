(ns y-video-back.auth.session-id-bypass.user
  {:mock-prod true}
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer [use-fixtures deftest testing is]]
    ;[ring.mock.request :refer :all]
    ;[y-video-back.handler :refer :all]
    [y-video-back.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [y-video-back.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
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

(deftest temp-test
  (testing "temp"
    (is true)))

; post:  /api/refresh-courses
(deftest refresh-courses
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/refresh-courses (:session-id-bypass env))]
        (is (= 401 (:status res)))))))

; get:  /api/user
(deftest user-get-by-logged-in
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/get-current-user (:session-id-bypass env))]
        (is (= 401 (:status res)))))))

; post:  /api/user
(deftest user-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-post (db-pop/get-user))]
        (is (= 401 (:status res)))))))

; get:  /api/user/{id}
(deftest user-id-get
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-id-get (:id (db-pop/add-user)))]
        (is (= 401 (:status res)))))))

; delete:  /api/user/{id}
(deftest user-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-id-delete (:id (db-pop/add-user)))]
        (is (= 401 (:status res)))))))

; patch:  /api/user/{id}
(deftest user-id-patch
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-id-patch (:id (db-pop/add-user))
                                  (db-pop/get-user))]
        (is (= 401 (:status res)))))))

; get:  /api/user/{id}/collections
(deftest user-id-collections
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-id-collections (:id (db-pop/add-user)))]
        (is (= 401 (:status res)))))))

; get:  /api/user/{id}/courses
(deftest user-id-courses
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-id-courses (:id (db-pop/add-user)))]
        (is (= 401 (:status res)))))))

; get:  /api/user/{id}/words
(deftest user-id-words
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/user-id-get-words (:id (db-pop/add-user)))]
        (is (= 401 (:status res)))))))
