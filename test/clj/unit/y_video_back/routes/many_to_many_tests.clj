; Operations testing in this file:
; 1. connect x and y
; 2. disconnect x and y
; 3. find all x connected to y
; 4. find all y connected to x

(ns y-video-back.routes.many-to-many-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
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
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get_random_file_without_id))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get_random_file_without_id))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get_random_file_without_id))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get_random_word_without_id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get_random_word_without_id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get_random_word_without_id (:id test-user-thr)))))

  (def test-user-coll-one (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-one)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-two)
                                                                              :collection_id (:id test-coll-two)
                                                                              :account_role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-thr)
                                                                              :collection_id (:id test-coll-thr)
                                                                              :account_role 0})))





  (mount.core/start #'y-video-back.handler/app))

(deftest test-user-collection-assoc
  (testing "connect user and collection"
    (let [new_user_coll_assoc (g/get_random_user_collections_assoc_without_id (:id test-user-one) (:id test-coll-two))]
      (let [res (rp/collection-id-add-user (:collection-id new_user_coll_assoc) (dissoc new_user_coll_assoc :collection-id))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_user_coll_assoc {:id id}) (ut/remove-db-only (user_collections_assoc/READ id))))))))
  (testing "connect user and collection TEMP"
    (let [new_user_coll_assoc (g/get_random_user_collections_assoc_without_id (:id test-user-two) (:id test-coll-one))]
      (let [res (rp/collection-id-add-user (:collection-id new_user_coll_assoc) (dissoc new_user_coll_assoc :collection-id))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_user_coll_assoc {:id id}) (ut/remove-db-only (user_collections_assoc/READ id)))))))
    (testing "temp"
      (is (= "test-coll-one id" (:id test-coll-one)))
      (is (= "test-user-one id" (:id test-user-one)))
      (is (= "" (user_collections_assoc/READ-BY-IDS [(:id test-coll-one) (:id test-user-one)])))))
  (comment (testing "disconnect user and collection")
    (let [new_user_coll_assoc (g/get_random_user_collections_assoc_without_id (:id test-user-one) (:id test-coll-two))]
      (let [res (rp/collection-id-add-user (:collection-id new_user_coll_assoc) (dissoc new_user_coll_assoc :collection-id))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_user_coll_assoc {:id id}) (ut/remove-db-only (user_collections_assoc/READ id)))))))






    (comment (let [res (rp/collection-id-add-user (:id test-coll-one) (:id test-user-two) 1)])
      (is (= 200 (:status res)))
      (let [assoc-id (ut/get-id res)]
        (is (= {:collection-id (:id test-coll-one)
                :user-id (:id test-user-two)
                :account-role 1
                :id (ut/to-uuid assoc-id)}
               (ut/remove-db-only (user_collections_assoc/READ (ut/to-uuid assoc-id)))))))))
