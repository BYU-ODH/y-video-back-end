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
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.annotations :as annotations]
      [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.content-files-assoc :as content_files_assoc]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user_collections_assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]))
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

(defn under-to-hyphen
  "Converts all underscores to hypens in map keywords"
  [m]
  (into {}
    (map (fn [val]
           {
            (keyword
              (subs
                (clojure.string/replace
                  (str
                    (get val 0))
                  "_"
                  "-")
                1))
            (get val 1)})
      m)))

(tcore/basic-transaction-fixtures
  (def test-user-one (under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-user-two (under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-user-thr (under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-coll-one (under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-coll-two (under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-coll-thr (under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-user-coll-one (under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-one)
                                                                           :collection_id (:id test-coll-one)
                                                                           :account_role 0})))
  (def test-user-coll-two (under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-two)
                                                                           :collection_id (:id test-coll-two)
                                                                           :account_role 0})))
  (def test-user-coll-thr (under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-thr)
                                                                           :collection_id (:id test-coll-thr)
                                                                           :account_role 0})))
  (def test-cont-one (under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-cont-two (under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-cont-thr (under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-crse-one (under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-crse-two (under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-crse-thr (under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (mount.core/start #'y-video-back.handler/app))

(defn get-id
  "Gets id from raw response body"
  [res]
  (:id (m/decode-response-body res)))

(defn remove-db-only
  "Compares 2 maps, not counting created, updated, and deleted fields"
  [my_map]
  (dissoc my_map :created :updated :deleted))

(defn to-uuid
  [text_in]
  (java.util.UUID/fromString text_in))

(deftest test-user
  (testing "user CREATE"
    (let [new_user (g/get_random_user_without_id)]
      (let [res (rp/user-post new_user)]
        (is (= 200 (:status res)))
        (let [id (to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_user {:id id}) (remove-db-only (users/READ id))))))))
  (testing "user READ"
    (let [res (rp/user-id-get (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= (update (remove-db-only test-user-one) :id str)
             (remove-db-only (m/decode-response-body res))))))
  (testing "user UPDATE"
    (let [new_user (g/get_random_user_without_id)]
      (let [res (rp/user-id-patch (:id test-user-one) new_user)]
        (is (= 200 (:status res)))
        (is (= (into new_user {:id (:id test-user-one)}) (remove-db-only (users/READ (:id test-user-one))))))))
  (testing "user DELETE"
    (let [res (rp/user-id-delete (:id test-user-two))]
      (is (= 200 (:status res)))
      (is (= nil (users/READ (:id test-user-two)))))))

(deftest test-coll
  (testing "coll CREATE"
    (let [new_coll (g/get_random_collection_without_id)]
      (let [res (rp/collection-post new_coll (:id test-user-one))]
        (is (= 200 (:status res)))
        (let [id (to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_coll {:id id}) (remove-db-only (collections/READ id))))
          (is (= (list {:user-id (:id test-user-one) :account-role 0 :collection-id id})
                 (map #(remove-db-only (dissoc % :id)) (user_collections_assoc/READ-BY-COLLECTION id))))))))
  (testing "coll READ"
    (let [res (rp/collection-id-get (:id test-coll-one))]
      (is (= 200 (:status res)))
      (is (= (update (remove-db-only test-coll-one) :id str)
             (remove-db-only (m/decode-response-body res))))))
  (testing "coll UPDATE"
    (let [new_coll (g/get_random_collection_without_id)]
      (let [res (rp/collection-id-patch (:id test-coll-one) new_coll)]
        (is (= 200 (:status res)))
        (is (= (into new_coll {:id (:id test-coll-one)}) (remove-db-only (collections/READ (:id test-coll-one))))))))
  (testing "coll DELETE"
    (let [res (rp/collection-id-delete (:id test-coll-two))]
      (is (= 200 (:status res)))
      (is (= nil (collections/READ (:id test-coll-two)))))))

(deftest test-cont
  (testing "cont CREATE"
    (let [new_cont (g/get_random_content_without_id)]
      (let [res (rp/content-post new_cont)]
        (is (= 200 (:status res)))
        (let [id (to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_cont {:id id}) (remove-db-only (contents/READ id))))))))
  (testing "cont READ"
    (let [res (rp/content-id-get (:id test-cont-one))]
      (is (= 200 (:status res)))
      (is (= (update (remove-db-only test-cont-one) :id str)
             (remove-db-only (m/decode-response-body res))))))
  (testing "cont UPDATE"
    (let [new_cont (g/get_random_content_without_id)]
      (let [res (rp/content-id-patch (:id test-cont-one) new_cont)]
        (is (= 200 (:status res)))
        (is (= (into new_cont {:id (:id test-cont-one)}) (remove-db-only (contents/READ (:id test-cont-one))))))))
  (testing "cont DELETE"
    (let [res (rp/content-id-delete (:id test-cont-two))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id test-cont-two)))))))

(deftest test-crse
  (testing "crse CREATE"
    (let [new_crse (g/get_random_course_without_id)]
      (let [res (rp/course-post new_crse)]
        (is (= 200 (:status res)))
        (let [id (to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_crse {:id id}) (remove-db-only (courses/READ id))))))))
  (testing "crse READ"
    (let [res (rp/course-id-get (:id test-crse-one))]
      (is (= 200 (:status res)))
      (is (= (update (remove-db-only test-crse-one) :id str)
             (remove-db-only (m/decode-response-body res))))))
  (testing "crse UPDATE"
    (let [new_crse (g/get_random_course_without_id)]
      (let [res (rp/course-id-patch (:id test-crse-one) new_crse)]
        (is (= 200 (:status res)))
        (is (= (into new_crse {:id (:id test-crse-one)}) (remove-db-only (courses/READ (:id test-crse-one))))))))
  (testing "crse DELETE"
    (let [res (rp/course-id-delete (:id test-crse-two))]
      (is (= 200 (:status res)))
      (is (= nil (courses/READ (:id test-crse-two)))))))











(comment (testing "user CRUD")
    (is (= a_user a_user))
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
           (is (= 404 (:status response))))))))


(comment (deftest test-collection)
  (testing "collection CRUD"
    ; Create user and collection
    (let [collection_one (g/get_random_collection_without_id)
          collection_two (g/get_random_collection_without_id)
          user_one (g/get_random_user_without_id)]
     (let [response (rp/user-post user_one)]
       (is (= 200 (:status response)))
       (let [user_one_id (get-id response)]
         (let [response (rp/collectionection-post collection_one user_one_id)]
           ; Verify collection created
           (is (= 200 (:status response)))
           (let [collection_one_id (get-id response)]
             ; Get collection
             (let [response (rp/collectionection-id-get collection_one_id)]
               ; Verify correct get
               (is (= 200 (:status response)))
               (is (= (into collection_one {:id collection_one_id}) (remove-db-only (m/decode-response-body response)))))
             ; Update collection - all fields
             (let [response (rp/collectionection-id-patch collection_one_id collection_two)]
               (is (= 200 (:status response))))
             ; Verify info changed
             (let [response (rp/collectionection-id-get collection_one_id)]
               (is (= 200 (:status response)))
               (is (= (into collection_two {:id collection_one_id}) (remove-db-only (m/decode-response-body response)))))
             ; Delete collection
             (let [response (rp/collectionection-id-delete collection_one_id)]
               (is (= 200 (:status response))))
             ; Verify deleted
             (let [response (rp/collectionection-id-get collection_one_id)]
               (is (= 404 (:status response)))))))))))
