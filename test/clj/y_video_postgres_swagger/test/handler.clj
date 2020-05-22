(ns y-video-postgres-swagger.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-postgres-swagger.handler :refer :all]
    [y-video-postgres-swagger.middleware.formats :as formats]
    [y-video-postgres-swagger.test.test_model_generator :as model-generator]
    [y-video-postgres-swagger.dbaccess.access :as db-access]
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
(def delete_password "17e6b095-c7a7-471f-8f89-58e0a57f89f3")

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
      (db-access/clear_database delete_password)
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
    (testing "collection"
      (db-access/clear_database delete_password)
      (let [test_user (model-generator/get_random_user_without_id)
            test_collection (model-generator/get_random_collection_without_id)]
        ; Add user
        (let [add_user_res ((app) (-> (request :post "/api/user")
                                      (json-body test_user)))]
          ; Check success
          (is (= 200 (:status add_user_res)))
          (let [user_id (:id (m/decode-response-body add_user_res))]
           ; Add collection
           (let [add_collection_res ((app) (-> (request :post "/api/collection")
                                               (json-body {:name (:collection_name test_collection) :user_id user_id})))]
             ; Check add collection success
             (is (= 200 (:status add_collection_res)))
             (let [collection_res_body (m/decode-response-body add_collection_res)]
               (is (= "1 collection created" (:message collection_res_body)))
               ; Get collection
               (let [get_collection_res ((app) (-> (request :get (str "/api/collection/" (:id collection_res_body)))))]
                 ; Check successful get collection
                 (is (= 200 (:status get_collection_res)))
                 (is (= {:id (:id collection_res_body) :collection_name (:collection_name test_collection)
                         :published false :archived false}
                        (m/decode-response-body get_collection_res))))))))))

    (testing "content"
      (db-access/clear_database delete_password)
      (let [test_user (model-generator/get_random_user_without_id)
            test_collection (model-generator/get_random_collection_without_id)
            test_content (model-generator/get_random_content_without_id_or_collection_id)]
        ; Add user
        (let [add_user_res ((app) (-> (request :post "/api/user")
                                      (json-body test_user)))]
          ; Check success
          (is (= 200 (:status add_user_res)))
          (let [user_id (:id (m/decode-response-body add_user_res))]
           ; Add collection
           (let [add_collection_res ((app) (-> (request :post "/api/collection")
                                               (json-body {:name (:collection_name test_collection) :user_id user_id})))]
             ; Check add collection success
             (is (= 200 (:status add_collection_res)))
             (let [collection_res_body (m/decode-response-body add_collection_res)]
               (is (= "1 collection created" (:message collection_res_body)))
               ; Add content
               (let [add_content_res ((app) (-> (request :post "/api/content")
                                                (json-body (into test_content {:collection_id (str (:id collection_res_body))}))))]
                 (is (= 200 (:status add_content_res)))
                 (let [content_res_body (m/decode-response-body add_content_res)]
                   (is (= "1 content created" (:message content_res_body)))
                   (let [get_content_res ((app) (-> (request :get (str "/api/content/" (:id content_res_body)))))]
                     (is (= (into test_content {:collection_id (str (:id collection_res_body)) :id (:id content_res_body)})
                            (m/decode-response-body get_content_res))))))))))))

    (comment (testing "collections")
       (let [id "8675309" name "jenny" published false archived false]
        (let [response ((app) (-> (request :post "/api/collections")
                                  (json-body {:id id, :name name, :published published, :archived published})))]
          (is (= 200 (:status response)))
          (is (= {:message "1 collection added"} (m/decode-response-body response))))))



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
