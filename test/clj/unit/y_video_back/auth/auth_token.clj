(ns y-video-back.auth.auth-token
    (:require
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
      [y-video-back.db.auth-tokens :as auth-tokens]))

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

; auth-ping with valid auth token
(deftest auth-token
  (testing "valid auth token"
    (let [user-one (db-pop/add-user)
          token (auth-tokens/CREATE {:user-id (:id user-one)})
          res (rp/auth-ping token)]
      (is (= 200 (:status res))))))

; auth-ping with invalid auth token
; auth-ping with already used valid auth token
; auth-ping with expired valid auth token
