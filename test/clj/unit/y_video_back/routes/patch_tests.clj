;; For each model, test:
;; 1) update fields one at a time
;; 2) update some, but not all, fields at once
;; 3) update all fields at once

(ns y-video-back.routes.patch-tests
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
  (def test-user-coll-one (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-one)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-two)
                                                                              :collection_id (:id test-coll-two)
                                                                              :account_role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-thr)
                                                                              :collection_id (:id test-coll-thr)
                                                                              :account_role 0})))
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
  (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-one) (:id test-cont-one)))))
  (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-two) (:id test-cont-two)))))
  (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-thr) (:id test-cont-thr)))))
  (mount.core/start #'y-video-back.handler/app))

(deftest test-user-patch
  (testing "user fields one at a time"
    (let [new_user (g/get_random_user_without_id)
          id (:id test-user-one)]
      (is (= (ut/remove-db-only test-user-one) (ut/remove-db-only (users/READ id))))
      (doseq [val (seq new_user)]
             [(do
                (let [res (rp/user-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new_user) ((get val 0) (users/READ id)))))])
      (is (= (into new_user {:id id}) (ut/remove-db-only (users/READ id))))))
  (testing "user multiple fields at once"
    (let [new_user (g/get_random_user_without_id)
          id (:id test-user-two)]
      (is (= (ut/remove-db-only test-user-two) (ut/remove-db-only (users/READ id))))
      (let [fields-to-change (ut/random-submap new_user)]
        (let [res (rp/user-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-user-two fields-to-change))
               (ut/remove-db-only (users/READ id))))))
    (testing "user all fields at once"
      (let [new_user (g/get_random_user_without_id)
            id (:id test-user-thr)]
        (is (= (ut/remove-db-only test-user-thr) (ut/remove-db-only (users/READ id))))
        (let [res (rp/user-id-patch id new_user)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-user-thr new_user))
               (ut/remove-db-only (users/READ id))))))))

(deftest test-coll-patch
  (testing "coll fields one at a time"
    (let [new_coll (g/get_random_collection_without_id)
          id (:id test-coll-one)]
      (is (= (ut/remove-db-only test-coll-one) (ut/remove-db-only (collections/READ id))))
      (doseq [val (seq new_coll)]
             [(do
                (let [res (rp/collection-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new_coll) ((get val 0) (collections/READ id)))))])
      (is (= (into new_coll {:id id}) (ut/remove-db-only (collections/READ id))))))
  (testing "coll multiple fields at once"
    (let [new_coll (g/get_random_collection_without_id)
          id (:id test-coll-two)]
      (is (= (ut/remove-db-only test-coll-two) (ut/remove-db-only (collections/READ id))))
      (let [fields-to-change (ut/random-submap new_coll)]
        (let [res (rp/collection-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-coll-two fields-to-change))
               (ut/remove-db-only (collections/READ id))))))
    (testing "coll all fields at once"
      (let [new_coll (g/get_random_collection_without_id)
            id (:id test-coll-thr)]
        (is (= (ut/remove-db-only test-coll-thr) (ut/remove-db-only (collections/READ id))))
        (let [res (rp/collection-id-patch id new_coll)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-coll-thr new_coll))
               (ut/remove-db-only (collections/READ id))))))))
