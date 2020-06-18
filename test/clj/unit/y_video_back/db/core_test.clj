(ns y-video-back.db.core-test
  "Entry database testing namespace, testing basic functions and providing functions for testing"
  (:require [y-video-back.db.core :refer [*db*] :as db]
            [y-video-back.db.test-util :as tcore]
            [y-video-back.handler :refer [app]]
            [mount.core]
            [y-video-back.db.test-data :as td]
            [clojure.test :refer [deftest is testing]]
            [y-video-back.utils.model-generator :as g]
            [y-video-back.db.annotations :as annotations]
            [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
            [y-video-back.db.users-by-collection :as users-by-collection]
            [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
            [y-video-back.db.collections :as collections]
            [y-video-back.db.content-files-assoc :as content-files-assoc]
            [y-video-back.db.contents :as contents]
            [y-video-back.db.courses :as courses]
            [y-video-back.db.files :as files]
            [y-video-back.db.user-collections-assoc :as user-collections-assoc]
            [y-video-back.db.users :as users]
            [y-video-back.db.words :as words]
            [y-video-back.utils.utils :as ut]))

(tcore/basic-transaction-fixtures
    (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
    (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
    (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
    (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
    (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-two)}))))
    (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-thr)}))))
    (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
    (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
    (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
    (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
    (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
    (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
    (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
    (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
    (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
    (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
    (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
    (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-thr)))))
    (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-one) (:id test-cont-one)))))
    (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-two) (:id test-cont-two)))))
    (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-thr) (:id test-cont-thr)))))

 (mount.core/start #'y-video-back.handler/app))
