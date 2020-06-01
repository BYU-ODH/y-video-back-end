(ns y-video-back.routes.crud-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
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

(tcore/basic-transaction-fixtures
 (mount.core/start #'y-video-back.handler/app))

(defn get-id
  "Gets id from raw response body"
  [res]
  (:id (m/decode-response-body res)))

(defn remove-db-only
  "Compares 2 maps, not counting created, updated, and deleted fields"
  [my_map]
  (dissoc my_map :created :updated :deleted))


(deftest test-user
  (testing "user CRUD"
    ; Create user
    (let [user_one (g/get_random_user_without_id)
          user_two (g/get_random_user_without_id)]
     (let [response (rp/user-post user_one)]
       ; Verify user created
       (is (= 200 (:status response)))
       (let [user_one_id (get-id response)]
         ; Get user
         (let [response (rp/user-id-get user_one_id)]
           ; Verify correct get
           (is (= 200 (:status response)))
           (is (= (into user_one {:id user_one_id}) (remove-db-only (m/decode-response-body response)))))
         ; Update user - all fields
         (let [response (rp/user-id-patch user_one_id user_two)]
           (is (= 200 (:status response))))
         ; Verify info changed
         (let [response (rp/user-id-get user_one_id)]
           (is (= 200 (:status response)))
           (is (= (into user_two {:id user_one_id}) (remove-db-only (m/decode-response-body response)))))
         ; Delete user
         (let [response (rp/user-id-delete user_one_id)]
           (is (= 200 (:status response))))
         ; Verify deleted
         (let [response (rp/user-id-get user_one_id)]
           (is (= 404 (:status response)))))))))

(deftest test-collection
  (testing "collection CRUD"
    ; Create user and collection
    (let [collection_one (g/get_random_collection_without_id)
          collection_two (g/get_random_collection_without_id)
          user_one (g/get_random_user_without_id)]
     (let [response (rp/user-post user_one)]
       (is (= 200 (:status response)))
       (let [user_one_id (get-id response)]
         (let [response (rp/collection-post collection_one user_one_id)]
           ; Verify collection created
           (is (= 200 (:status response)))
           (let [collection_one_id (get-id response)]
             ; Get collection
             (let [response (rp/collection-id-get collection_one_id)]
               ; Verify correct get
               (is (= 200 (:status response)))
               (is (= (into collection_one {:id collection_one_id}) (remove-db-only (m/decode-response-body response)))))
             ; Update collection - all fields
             (let [response (rp/collection-id-patch collection_one_id collection_two)]
               (is (= 200 (:status response))))
             ; Verify info changed
             (let [response (rp/collection-id-get collection_one_id)]
               (is (= 200 (:status response)))
               (is (= (into collection_two {:id collection_one_id}) (remove-db-only (m/decode-response-body response)))))
             ; Delete collection
             (let [response (rp/collection-id-delete collection_one_id)]
               (is (= 200 (:status response))))
             ; Verify deleted
             (let [response (rp/collection-id-get collection_one_id)]
               (is (= 404 (:status response)))))))))))
