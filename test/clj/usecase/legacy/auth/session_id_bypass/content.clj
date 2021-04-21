(ns legacy.auth.session-id-bypass.content
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

; post:  /api/content
(deftest content-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-post (db-pop/get-content))]
        (is (= 401 (:status res)))))))

; get:  /api/content/{id}
(deftest content-id-get
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-id-get (:id (db-pop/add-content)))]
        (is (= 401 (:status res)))))))

; delete:  /api/content/{id}
(deftest content-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-id-delete (:id (db-pop/add-content)))]
        (is (= 401 (:status res)))))))

; patch:  /api/content/{id}
(deftest content-id-patch
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-id-patch (:id (db-pop/add-content))
                                     (db-pop/get-content))]
        (is (= 401 (:status res)))))))

; post:  /api/content/{id}/add-view
(deftest content-id-add-view
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-id-add-view (:id (db-pop/add-content)))]
        (is (= 401 (:status res)))))))

; post:  /api/content/{id}/clone-subtitle
(deftest content-id-clone-subtitle
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-id-clone-subtitle (:id (db-pop/add-content))
                                              (:id (db-pop/add-subtitle)))]
        (is (= 401 (:status res)))))))

; get:  /api/content/{id}/subtitles
(deftest content-id-subtitles
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/content-id-subtitles (:id (db-pop/add-content)))]
        (is (= 401 (:status res)))))))
