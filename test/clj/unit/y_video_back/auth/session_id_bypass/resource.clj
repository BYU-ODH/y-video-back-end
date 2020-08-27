(ns y-video-back.auth.session-id-bypass.resource
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

; post:  /api/resource
(deftest resource-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-post (db-pop/get-resource))]
        (is (= 401 (:status res)))))))

; get:  /api/resource/{id}
(deftest resource-id-get
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-get (:id (db-pop/add-resource)))]
        (is (= 401 (:status res)))))))

; delete:  /api/resource/{id}
(deftest resource-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-delete (:id (db-pop/add-resource)))]
        (is (= 401 (:status res)))))))

; patch:  /api/resource/{id}
(deftest resource-id-patch
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-patch (:id (db-pop/add-resource))
                                      (db-pop/get-resource))]
        (is (= 401 (:status res)))))))

; get:  /api/resource/{id}/collections
(deftest resource-id-collections
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-collections (:id (db-pop/add-resource)))]
        (is (= 401 (:status res)))))))

; get:  /api/resource/{id}/contents
(deftest resource-id-contents
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-contents (:id (db-pop/add-resource)))]
        (is (= 401 (:status res)))))))

; get:  /api/resource/{id}/files
(deftest resource-id-files
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-files (:id (db-pop/add-resource)))]
        (is (= 401 (:status res)))))))

; get:  /api/resource/{id}/subtitles
(deftest resource-id-subtitles
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/resource-id-subtitles (:id (db-pop/add-resource)))]
        (is (= 401 (:status res)))))))
