(ns legacy.auth.session-id-bypass.media
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

; get:  /api/media/get-file-key/{file-id}
(deftest media-get-file-key
  (testing "with session-id-bypass"
    (with-redefs [env (assoc (dissoc env :test) :prod true)]
      (let [res (rp/get-file-key (:id (db-pop/add-file)))]
        (is (= 401 (:status res)))))))

; get:  /api/media/stream-media/{file-key}
; not applicable
