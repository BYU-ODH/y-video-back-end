(ns y-video-back.auth.session-id-bypass.language
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

; get:  /api/language
(deftest language-get-all
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/language-get-all)]
        (is (= 401 (:status res)))))))

; post:  /api/language
(deftest language-post
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/language-post (db-pop/get-language))]
        (is (= 401 (:status res)))))))

; delete:  /api/language/{id}
(deftest language-id-delete
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/language-id-delete (:id (db-pop/add-language)))]
        (is (= 401 (:status res)))))))
