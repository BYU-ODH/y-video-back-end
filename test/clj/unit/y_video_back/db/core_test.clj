(ns y-video-back.db.core-test
  "Entry database testing namespace, testing basic functions and providing functions for testing"
  (:require [y-video-back.db.core :refer [*db*] :as db]
            [y-video-back.db.test-util :as tcore]
            [y-video-back.handler :refer [app]]
            [mount.core]
            [y-video-back.db.test-data :as td]
            [clojure.test :refer [deftest is testing]]
            [y-video-back.utils.model_generator :as g]
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

(tcore/basic-transaction-fixtures
    (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
    (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
    (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
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
    (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-one) (:id test-cont-one)))))
    (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-two) (:id test-cont-two)))))
    (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-thr) (:id test-cont-thr)))))

 (mount.core/start #'y-video-back.handler/app))

(deftest users
  (testing "create user (valid)"
    (let [res (users/CREATE (g/get_random_user_without_id))]
      (is (contains? res :id))))
  (testing "create user (duplicate)"
    (let [user-one (g/get_random_user_without_id)
          user-two (g/get_random_user_without_id)]
      (println (str "user-one email: " (:email user-one)))
      (println (str "user-two email: " (:email user-two)))
      (is (not (= (:email user-one) (:email user-two))))
      (let [res (users/CREATE user-one)]
        (is (contains? res :id)))
      ;(try
      ;  (let [res (users/CREATE user-one)]
      ;    (is false))
      ;  (catch org.postgresql.util.PSQLException e
      ;    (do (is true)
      ;        (str "test " "complete"))))
      ;(try
      ;  (let [res (users/CREATE user-one)]
      ;    (println "res - - - - - - - - - - - - - - - -")
      ;    (println res))
      ;  (catch Exception e
      ;    (do
      ;      (println "i caught it!! - - - - - - - - - - - - - - - -")
      ;      4))
      (let [res (users/CREATE user-two)]
        (is (contains? res :id)))))
  (testing "read user (valid)"
    (let [res (users/READ (:id test-user-one))]
      (is (= test-user-one res))))
  (testing "read user (invalid)"
    (let [invalid-id (java.util.UUID/randomUUID)
          res (users/READ invalid-id)]
      (is (nil? res)))
    (try
      (let [res (users/READ "not-a-uuid!")]
        (is false))
      (catch org.postgresql.util.PSQLException e
        (is true)))))
