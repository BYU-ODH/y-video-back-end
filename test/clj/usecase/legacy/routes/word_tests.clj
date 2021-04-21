(ns legacy.routes.word-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
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
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (mount.core/start #'y-video-back.handler/app))

; test-user-one has 1 word (test-word-one)
; test-user-two has 2 words (test-word-two and -thr)
; test-user-thr has 0 words

(deftest test-user-get-words
  (testing "user with one word"
    (let [res (rp/user-id-get-words (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= [(-> test-word-one
                  (ut/remove-db-only)
                  (update :id str)
                  (update :user-id str))]
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "user with two words"
    (let [res (rp/user-id-get-words (:id test-user-two))]
      (is (= 200 (:status res)))
      (is (= (frequencies [(-> test-word-two
                               (ut/remove-db-only)
                               (update :id str)
                               (update :user-id str))
                           (-> test-word-thr
                                       (ut/remove-db-only)
                                       (update :id str)
                                       (update :user-id str))])
             (frequencies (map ut/remove-db-only (m/decode-response-body res)))))))
  (testing "user with no words"
    (let [res (rp/user-id-get-words (:id test-user-thr))]
      (is (= 200 (:status res)))
      (is (= []
             (map ut/remove-db-only (m/decode-response-body res)))))))









