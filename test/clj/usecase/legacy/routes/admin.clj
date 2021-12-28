(ns legacy.routes.admin
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]))

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

(deftest temp
  (testing "temp"
    (is true)))

(deftest temp2
  (testing "temp2"
    (is true)))




