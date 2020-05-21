(ns y-video-postgres-swagger.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-postgres-swagger.handler :refer :all]
    [y-video-postgres-swagger.middleware.formats :as formats]
    [y-video-postgres-swagger.test.test_model_generator :as model-generator]
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

    (testing "user"
      (let [test_user_one (model-generator/get_random_user_without_id)
            test_user_two (model-generator/get_random_user_without_id)]
        ; Create two users
        (let [res_one ((app) (-> (request :post "/api/user")
                                 (json-body test_user_one)))
              res_two ((app) (-> (request :post "/api/user")
                                 (json-body test_user_two)))]
          ; Check successful creates
          (is (= 200 (:status res_one)))
          (is (= 200 (:status res_two)))
          (let [res_body_one (m/decode-response-body res_one)
                res_body_two (m/decode-response-body res_two)]
            (is (= "1 user created" (:message res_body_one)))
            (is (= "1 user created" (:message res_body_two)))
            (let [res_one ((app) (-> (request :get (str "/api/user/" (:id res_body_one)))))
                  res_two ((app) (-> (request :get (str "/api/user/" (:id res_body_two)))))]
              (is (= 200 (:status res_one)))
              (is (= (into test_user_one {:id (:id res_body_one)}) (m/decode-response-body res_one)))
              (is (= 200 (:status res_two)))
              (is (= (into test_user_two {:id (:id res_body_two)}) (m/decode-response-body res_two))))))))

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
