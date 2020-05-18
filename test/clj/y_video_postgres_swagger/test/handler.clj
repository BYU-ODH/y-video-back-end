(ns y-video-postgres-swagger.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-postgres-swagger.handler :refer :all]
    [y-video-postgres-swagger.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-postgres-swagger.config/env
                 #'y-video-postgres-swagger.handler/app-routes)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "not-found route 2"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))


  (testing "services"

    (comment (testing "success"
              (let [response ((app) (-> (request :post "/api/math/plus")
                                        (json-body {:x 10, :y 6})))]
                (is (= 200 (:status response)))
                (is (= {:total 16} (m/decode-response-body response))))))

    (testing "echo"
      (let [test_string "hello there!"]
       (let [response ((app) (-> (request :post "/api/echo")
                                 (json-body {:echo test_string})))]
         (is (= 200 (:status response)))
         (is (= {:echo test_string} (m/decode-response-body response))))))

    (comment (testing "collections"
              (let [id "8675309" name "jenny" published false archived false]
               (let [response ((app) (-> (request :post "/api/collections")
                                         (json-body {:id id, :name name, :published published, :archived published})))]
                 (is (= 200 (:status response)))
                 (is (= {:message "1 collection added"} (m/decode-response-body response)))))))



    (comment (testing "parameter coercion error"
              (let [response ((app) (-> (request :post "/api/math/plus")
                                        (json-body {:x 10, :y "invalid"})))]
                (is (= 400 (:status response))))))

    (comment (testing "response coercion error"
              (let [response ((app) (-> (request :post "/api/math/plus")
                                        (json-body {:x -10, :y 6})))]
                (is (= 500 (:status response)))))

     (testing "content negotiation"
       (let [response ((app) (-> (request :post "/api/math/plus")
                                 (body (pr-str {:x 10, :y 6}))
                                 (content-type "application/edn")
                                 (header "accept" "application/transit+json")))]
         (is (= 200 (:status response)))
         (is (= {:total 16} (m/decode-response-body response))))))))
