(ns y-video-back.email.mail-tests
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.email.mail :as mail]
      [y-video-back.db.email-logs :as email-logs]))

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

(deftest log-message-test
  (testing "Log valid email message"
    (let [valid-id "valid-id"]
      (with-redefs-fn {#'email-logs/CREATE (fn [email] valid-id)}
        #(is (= valid-id (mail/log-message {})))))))

(deftest send-email-test
  (testing "Send email in test env"
    (let [valid-res "valid-res"]
      (with-redefs [env (assoc (dissoc env :test) :test true)]
        (with-redefs-fn {#'mail/log-message (fn [email] valid-res)}
          #(is (= valid-res (mail/send-email {})))))))
  (testing "Send email in dev env"
    (let [valid-res "valid-res"]
      (with-redefs [env (assoc (dissoc env :test) :dev true)]
        (with-redefs-fn {#'mail/log-message (fn [email] valid-res)}
          #(is (= valid-res (mail/send-email {})))))))
  (testing "Send email in prod env"
    (let [valid-res "valid-res"]
      (with-redefs [env (assoc (dissoc env :test) :prod true)]
        (with-redefs-fn {#'mail/log-message (fn [email] "")
                         #'mail/send-message (fn [email] valid-res)}
          #(is (= valid-res (mail/send-email {}))))))))
