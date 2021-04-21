(ns legacy.auth.session-id-bypass.word
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

; post:  /api/word
(deftest word-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/word-post (db-pop/get-word))]
        (is (= 401 (:status res)))))))

; get:  /api/word/{id}
(deftest word-id-get
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/word-id-get (:id (db-pop/add-word)))]
        (is (= 401 (:status res)))))))

; delete:  /api/word/{id}
(deftest word-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/word-id-delete (:id (db-pop/add-word)))]
        (is (= 401 (:status res)))))))

; patch:  /api/word/{id}
(deftest word-id-patch
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/word-id-patch (:id (db-pop/add-word))
                                  (db-pop/get-word))]
        (is (= 401 (:status res)))))))
