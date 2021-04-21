(ns legacy.routes.error-code.word-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [legacy.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (ut/renew-db)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (mount.core/start #'y-video-back.handler/app))

(deftest word-post
  (testing "add word with invalid user id"
    (let [new-word (g/get-random-word-without-id (java.util.UUID/randomUUID))
          res (rp/word-post new-word)]
      (is (= 500 (:status res)))))
  (testing "add duplicated word"
    (let [new-word (g/get-random-word-without-id (:id test-user-one))
          add-word-res (words/CREATE new-word)
          res (rp/word-post new-word)]
      (is (= 500 (:status res))))))

(deftest word-id-get
  (testing "read nonexistent word"
    (let [res (rp/word-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest word-id-patch
  (testing "update nonexistent word"
    (let [res (rp/word-id-patch (java.util.UUID/randomUUID) (g/get-random-word-without-id))]
      (is (= 404 (:status res)))))
  (testing "update word to nonexistent user id"
    (let [word-one (g/get-random-word-without-id (:id test-user-one))
          add-word-res (words/CREATE word-one)
          res (rp/word-id-patch (:id add-word-res) {:user-id (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res)))))
  (testing "update word to taken field combo (all fields)"
    (let [word-one (g/get-random-word-without-id (:id test-user-one))
          word-two (g/get-random-word-without-id (:id test-user-two))
          word-one-res (words/CREATE word-one)
          word-two-res (words/CREATE word-two)
          res (rp/word-id-patch (:id word-two-res) word-one)]
      (is (= 500 (:status res)))))
  (testing "update word to taken field combo (user-id)"
    (let [word-one (g/get-random-word-without-id (:id test-user-one))
          word-two (g/get-random-word-without-id (:id test-user-two))
          word-one-res (words/CREATE word-one)
          word-two-res (words/CREATE word-two)
          res-setup (rp/word-id-patch (:id word-two-res) {:word (:word word-one)
                                                          :src-lang (:src-lang word-one)
                                                          :dest-lang (:dest-lang word-one)})
          res (rp/word-id-patch (:id word-two-res) {:user-id (:user-id word-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res)))))
  (testing "update word to taken field combo (word)"
    (let [word-one (g/get-random-word-without-id (:id test-user-one))
          word-two (g/get-random-word-without-id (:id test-user-two))
          word-one-res (words/CREATE word-one)
          word-two-res (words/CREATE word-two)
          res-setup (rp/word-id-patch (:id word-two-res) {:user-id (:user-id word-one)
                                                          :src-lang (:src-lang word-one)
                                                          :dest-lang (:dest-lang word-one)})
          res (rp/word-id-patch (:id word-two-res) {:word (:word word-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res)))))
  (testing "update word to taken field combo (src_lang)"
    (let [word-one (g/get-random-word-without-id (:id test-user-one))
          word-two (g/get-random-word-without-id (:id test-user-two))
          word-one-res (words/CREATE word-one)
          word-two-res (words/CREATE word-two)
          res-setup (rp/word-id-patch (:id word-two-res) {:word (:word word-one)
                                                          :user-id (:user-id word-one)
                                                          :dest-lang (:dest-lang word-one)})
          res (rp/word-id-patch (:id word-two-res) {:src-lang (:src-lang word-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res)))))
  (testing "update word to taken field combo (dest_lang)"
    (let [word-one (g/get-random-word-without-id (:id test-user-one))
          word-two (g/get-random-word-without-id (:id test-user-two))
          word-one-res (words/CREATE word-one)
          word-two-res (words/CREATE word-two)
          res-setup (rp/word-id-patch (:id word-two-res) {:word (:word word-one)
                                                          :src-lang (:src-lang word-one)
                                                          :user-id (:user-id word-one)})
          res (rp/word-id-patch (:id word-two-res) {:dest-lang (:dest-lang word-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res))))))

(deftest word-id-delete
  (testing "delete nonexistent word"
    (let [res (rp/word-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))








