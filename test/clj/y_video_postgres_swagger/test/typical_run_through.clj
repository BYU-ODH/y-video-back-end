(ns y-video-postgres-swagger.test.typical_run_through
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-postgres-swagger.handler :refer :all]
    [y-video-postgres-swagger.middleware.formats :as formats]
    [y-video-postgres-swagger.test.test_model_generator :as model-generator]
    [y-video-postgres-swagger.dbaccess.access :as db-access]
    [y-video-postgres-swagger.test.utils :as utils]
    [muuntaja.core :as m]
    [mount.core :as mount]
    [y-video-postgres-swagger.test.routes_proxy :as rp]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))


(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-postgres-swagger.config/env
                 #'y-video-postgres-swagger.handler/app-routes)
    (f)))
(def delete_password "17e6b095-c7a7-471f-8f89-58e0a57f89f3")


(deftest test-crud2
  (testing "user2"
    (is (= 1 1))))
