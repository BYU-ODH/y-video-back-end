(ns legacy.auth.session-id-bypass.file
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
    (f)
    (ut/delete-all-files (-> env :FILES :media-url))
    (ut/delete-all-files (-> env :FILES :test-temp))))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(deftest temp-test
  (testing "temp"
    (is true)))

; post:  /api/file
(deftest file-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [filecontent (ut/get-filecontent)
            file-one (dissoc (db-pop/get-file) :filepath)]
        (let [res (rp/file-post file-one filecontent)]
          (is (= 401 (:status res))))))))

; get:  /api/file/{id}
(deftest file-id-get
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/file-id-get (:id (db-pop/add-file)))]
        (is (= 401 (:status res)))))))

; delete:  /api/file/{id}
(deftest file-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/file-id-delete (:id (db-pop/add-file)))]
        (is (= 401 (:status res)))))))

; patch:  /api/file/{id}
(deftest file-id-patch
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/file-id-patch (:id (db-pop/add-file))
                                  (db-pop/get-file))]
        (is (= 401 (:status res)))))))
