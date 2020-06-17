(ns y-video-back.routes.misc-tests
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
      [y-video-back.db.user-courses-assoc :as user_courses_assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]
      [y-video-back.routes.service_handlers.db-utils :as dbu]))

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
  (def test-user-fou (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get_random_collection_without_id_or_owner) {:owner (:id test-user-one)}))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get_random_collection_without_id_or_owner) {:owner (:id test-user-two)}))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get_random_collection_without_id_or_owner) {:owner (:id test-user-thr)}))))
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
  (def test-user-crse-one (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id test-user-one)
                                                                          :course_id (:id test-crse-one)
                                                                          :account_role 0})))
  (def test-user-crse-two (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id test-user-two)
                                                                          :course_id (:id test-crse-two)
                                                                          :account_role 0})))
  (def test-user-crse-thr (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id test-user-thr)
                                                                          :course_id (:id test-crse-thr)
                                                                          :account_role 0})))
  (def test-user-crse-fou (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id test-user-fou)
                                                                          :course_id (:id test-crse-one)
                                                                          :account_role 2})))
  (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-one) (:id test-cont-one)))))
  (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-two) (:id test-cont-two)))))
  (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-thr) (:id test-cont-thr)))))
  (def test-coll-crse-one (ut/under-to-hyphen (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                                                                 :course_id (:id test-crse-one)})))
  (def test-coll-crse-two (ut/under-to-hyphen (collection_courses_assoc/CREATE {:collection_id (:id test-coll-two)
                                                                                 :course_id (:id test-crse-two)})))
  (def test-coll-crse-thr (ut/under-to-hyphen (collection_courses_assoc/CREATE {:collection_id (:id test-coll-thr)
                                                                                 :course_id (:id test-crse-thr)})))

  (def test-cont-file-one (ut/under-to-hyphen (content_files_assoc/CREATE {:content_id (:id test-cont-one)
                                                                           :file_id (:id test-file-one)})))
  (def test-cont-file-two (ut/under-to-hyphen (content_files_assoc/CREATE {:content_id (:id test-cont-two)
                                                                           :file_id (:id test-file-two)})))
  (def test-cont-file-thr (ut/under-to-hyphen (content_files_assoc/CREATE {:content_id (:id test-cont-thr)
                                                                           :file_id (:id test-file-thr)})))





  (mount.core/start #'y-video-back.handler/app))

(deftest test-content-add-view
  (testing "content add view"
    (let [res (rp/content-id-add-view (:id test-cont-one))]
      (is (= 200 (:status res)))
      (let [new-content (contents/READ (:id test-cont-one))]
        (is (= (+ 1 (:views test-cont-one)) (:views new-content)))))))
