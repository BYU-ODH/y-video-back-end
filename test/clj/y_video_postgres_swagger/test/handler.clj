(ns y-video-postgres-swagger.test.handler
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
                        (m/decode-response-body get_collection_res)))))))))
      (let [test_user (model-generator/get_random_user_without_id)
            test_collection (model-generator/get_random_collection_without_id)
            test_content_one (model-generator/get_random_content_without_id_or_collection_id)
            test_content_two (model-generator/get_random_content_without_id_or_collection_id)
            test_content_thr (model-generator/get_random_content_without_id_or_collection_id)]
        ; Set up database
        (def user_id (utils/insert_user test_user))
        (def collection_id (utils/insert_collection test_collection user_id))
        (def content_id_one (utils/insert_content test_content_one collection_id))
        (def content_id_two (utils/insert_content test_content_two collection_id))
        (def content_id_thr (utils/insert_content test_content_thr collection_id))

        (let [get_contents_res ((app) (-> (request :get (str "/api/collection/" (str collection_id) "/contents"))))]
          (is (= 200 (:status get_contents_res)))
          (let [get_contents_body (m/decode-response-body get_contents_res)]
            (is (= (frequencies get_contents_body)
                   (frequencies [
                                 (into test_content_one {:id content_id_one :collection_id collection_id})
                                 (into test_content_two {:id content_id_two :collection_id collection_id})
                                 (into test_content_thr {:id content_id_thr :collection_id collection_id})])))))))
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
                            (m/decode-response-body get_content_res))))))))))))))

(deftest test-crud
  (testing "user"
    (let [test_user_one (model-generator/get_random_user_without_id)]
      ; Add user
      (let [result (rp/user-post test_user_one)]
      ; Verify
        (is (= 200 (:status result)))
        (let [user_one_id (:id (m/decode-response-body result))]
          ; Retrieve user
          (let [result (rp/user-id-get user_one_id)]
            ; Verify
            (is (= 200 (:status result)))
            (is (= (into test_user_one {:id user_one_id}) (m/decode-response-body result))))
          ; Update user
          (let [new_user_one (model-generator/get_random_user_without_id)]
            (let [result (rp/user-id-patch user_one_id new_user_one)]
              ; Verify
              (is (= 200 (:status result)))
              (let [result (rp/user-id-get user_one_id)]
                (is (= 200 (:status result)))
                (is (= (into new_user_one {:id user_one_id}) (m/decode-response-body result))))))
          ; Delete user
          (let [result (rp/user-id-delete user_one_id)]
            ; Verify
            (is (= 200 (:status result)))
            (let [result (rp/user-id-get user_one_id)]
              (is (= 404 (:status result)))))))))
  (testing "collection"
    (let [test_collection_one (model-generator/get_random_collection_without_id)
          test_user_one (model-generator/get_random_user_without_id)]
      (let [user_id (:id (m/decode-response-body (rp/user-post test_user_one)))]
      ; Add collection
         (let [result (rp/collection-post (:collection_name test_collection_one) user_id)]
         ; Verify
           (is (= 200 (:status result)))
           (let [collection_one_id (:id (m/decode-response-body result))]
             ; Retrieve collection
             (let [result (rp/collection-id-get collection_one_id)]
               ; Verify
               (is (= 200 (:status result)))
               (is (= (into test_collection_one {:id collection_one_id}) (m/decode-response-body result))))
             ; Update collection
             (let [new_collection_one (model-generator/get_random_collection_without_id)]
               (let [result (rp/collection-id-patch collection_one_id new_collection_one)]
                 ; Verify
                 (is (= 200 (:status result)))
                 (let [result (rp/collection-id-get collection_one_id)]
                   (is (= 200 (:status result)))
                   (is (= (into new_collection_one {:id collection_one_id}) (m/decode-response-body result))))))
             ; Delete collection
             (let [result (rp/collection-id-delete collection_one_id)]
               ; Verify
               (is (= 200 (:status result)))
               (let [result (rp/collection-id-get collection_one_id)]
                 (is (= 404 (:status result))))))))))

  (testing "content"
    (let [test_content_one (model-generator/get_random_content_without_id_or_collection_id)
          test_collection_one (model-generator/get_random_collection_without_id)
          test_collection_two (model-generator/get_random_collection_without_id)
          test_user_one (model-generator/get_random_user_without_id)]
      (let [user_id (:id (m/decode-response-body (rp/user-post test_user_one)))]
        (let [collection_one_id (:id (m/decode-response-body (rp/collection-post (:collection_name test_collection_one) user_id)))]
          ; Add content
          (let [result (rp/content-post (into test_content_one {:collection_id collection_one_id}))]
            ; Verify
            (is (= 200 (:status result)))
            (let [content_one_id (:id (m/decode-response-body result))]
              ; Retrieve content
              (let [result (rp/content-id-get content_one_id)]
                ; Verify
                (is (= 200 (:status result)))
                (is (= (into test_content_one {:id content_one_id :collection_id collection_one_id}) (m/decode-response-body result))))
              ; Update content
              (let [new_content_one (model-generator/get_random_content_without_id_or_collection_id)]
                (let [result (rp/content-id-patch content_one_id (into new_content_one {:collection_id collection_one_id}))]
                  ; Verify
                  (is (= 200 (:status result)))
                  (let [result (rp/content-id-get content_one_id)]
                    (is (= 200 (:status result)))
                    (is (= (into new_content_one {:id content_one_id :collection_id collection_one_id}) (m/decode-response-body result))))))
              ; Delete content
              (let [result (rp/content-id-delete content_one_id)]
                ; Verify
                (is (= 200 (:status result)))
                (let [result (rp/content-id-get content_one_id)]
                  (is (= 404 (:status result)))))))))))

  (testing "course"
    (let [test_course_one (model-generator/get_random_course_without_id)]
      ; Add course
      (let [result (rp/course-post test_course_one)]
      ; Verify
        (is (= 200 (:status result)))
        (let [course_one_id (:id (m/decode-response-body result))]
          ; Retrieve course
          (let [result (rp/course-id-get course_one_id)]
            ; Verify
            (is (= 200 (:status result)))
            (is (= (into test_course_one {:id course_one_id}) (m/decode-response-body result))))
          ; Update course
          (let [new_course_one (model-generator/get_random_course_without_id)]
            (let [result (rp/course-id-patch course_one_id new_course_one)]
              ; Verify
              (is (= 200 (:status result)))
              (let [result (rp/course-id-get course_one_id)]
                (is (= 200 (:status result)))
                (is (= (into new_course_one {:id course_one_id}) (m/decode-response-body result))))))
          ; Delete course
          (let [result (rp/course-id-delete course_one_id)]
            ; Verify
            (is (= 200 (:status result)))
            (let [result (rp/course-id-get course_one_id)]
              (is (= 404 (:status result))))))))))
