(ns y-video-back.routes.test-echo
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.utils.route_proxy :as rp]
      ;[y-video-postgres-swagger.middleware.formats :as formats]
      ;[y-video-postgres-swagger.test.test_model_generator :as model-generator]
      ;[y-video-postgres-swagger.dbaccess.access :as db-access]
      ;[y-video-postgres-swagger.test.utils :as utils]
      [muuntaja.core :as m]
      [mount.core :as mount]))
      ;[y-video-postgres-swagger.test.routes_proxy :as rp]))


(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "not-found route 2"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))


  (testing "services"

    (testing "echo"
      (let [test_string "hello there!"]
       (let [response (rp/echo-post test_string)]
         (is (= 200 (:status response)))
         (is (= {:echo test_string} (m/decode-response-body response))))))))
