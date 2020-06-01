(ns y-video-back.routes.test-user
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      ;[y-video-postgres-swagger.middleware.formats :as formats]
      ;[y-video-postgres-swagger.test.test_model_generator :as model-generator]
      ;[y-video-postgres-swagger.dbaccess.access :as db-access]
      ;[y-video-postgres-swagger.test.utils :as utils]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model_generator :as g]
      [y-video-back.utils.route_proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]))
      ;[y-video-postgres-swagger.test.routes_proxy :as rp]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    ;(jdbc/db-set-rollback-only! *db*)
    ;(do ~@forms)
    ;(f#)
    (f)))

(use-fixtures
  :each
  (fn [f]
    (jdbc/with-db-transaction
      [transaction *db*]
      (jdbc/db-set-rollback-only! transaction)
      (binding [*txn* transaction] (f)))))


(deftest test-app
  (testing "user"
    (let [user_one (g/get_random_user_without_id)]
     (let [response (rp/user-post user_one)]
       (is (= 200 (:status response)))))))
