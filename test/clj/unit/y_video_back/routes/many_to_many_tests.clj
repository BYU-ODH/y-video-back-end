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
  (def test-coll-cont-one (ut/under-to-hyphen (collection_contents_assoc/CREATE {:collection_id (:id test-coll-one)
                                                                                 :content_id (:id test-cont-one)})))
  (def test-coll-cont-two (ut/under-to-hyphen (collection_contents_assoc/CREATE {:collection_id (:id test-coll-two)
                                                                                 :content_id (:id test-cont-two)})))
  (def test-coll-cont-thr (ut/under-to-hyphen (collection_contents_assoc/CREATE {:collection_id (:id test-coll-thr)
                                                                                 :content_id (:id test-cont-thr)})))





  (mount.core/start #'y-video-back.handler/app))

(deftest test-user-collection-assoc
  (testing "connect user and collection"
    (let [new_user_coll_assoc (g/get_random_user_collections_assoc_without_id (:id test-user-one) (:id test-coll-two))]
      (let [res (rp/collection-id-add-user (:collection-id new_user_coll_assoc) (:id test-user-one) (:account-role new_user_coll_assoc))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list (into new_user_coll_assoc {:id id}))
                 (map ut/remove-db-only (user_collections_assoc/READ-BY-IDS [(:collection-id new_user_coll_assoc) (:user-id new_user_coll_assoc)]))))))))
  (testing "disconnect user and collection"
    (let [res (rp/collection-id-remove-user (:id test-coll-one) (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= '()
             (user_collections_assoc/READ-BY-IDS [(:id test-coll-one) (:id test-user-one)])))))
  (testing "find all users by collection"
    (let [res (rp/collection-id-users (:id test-coll-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-user-thr
                 (update :id str)
                 (into {:collection-id (str (:id test-coll-thr)) :account-role 0})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all collections by user"
    (let [res (rp/user-id-collections (:id test-user-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-coll-thr
                 (update :id str)
                 (into {:user-id (str (:id test-user-thr)) :account-role 0})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest test-coll-content-assoc
  (testing "connect collection and content"
    (let [new_collection_content_assoc (g/get_random_collection_contents_assoc_without_id (:id test-coll-one) (:id test-cont-two))]
      (let [res (rp/collection-id-add-content (:collection-id new_collection_content_assoc) (:content-id new_collection_content_assoc))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list (into new_collection_content_assoc {:id id}))
                 (map ut/remove-db-only (collection_contents_assoc/READ-BY-IDS [(:collection-id new_collection_content_assoc) (:content-id new_collection_content_assoc)]))))))))
  (testing "disconnect collection and content"
    (let [res (rp/collection-id-remove-content (:id test-coll-one) (:id test-cont-one))]
      (is (= 200 (:status res)))
      (is (= '()
             (collection_contents_assoc/READ-BY-IDS [(:id test-coll-one) (:id test-cont-one)])))))
  (testing "find all collections by content"
    (let [res (rp/content-id-collections (:id test-cont-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-coll-thr
                 (update :id str)
                 (into {:content-id (str (:id test-cont-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all contents by collection"
    (let [res (rp/collection-id-contents (:id test-coll-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-cont-thr
                 (update :id str)
                 (into {:collection-id (str (:id test-coll-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))