(ns legacy.routes.word
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.db.words :as words]))

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
  (mount.core/start #'y-video-back.handler/app))

(deftest test-word
  (testing "word CREATE"
    (let [word-one (db-pop/get-word)]
      (let [res (rp/word-post word-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into word-one {:id id})
                 (ut/remove-db-only (words/READ id))))))))
  (testing "word READ"
    (let [word-one (ut/under-to-hyphen (words/CREATE (db-pop/get-word)))
          res (rp/word-id-get (:id word-one))]
      (is (= 200 (:status res)))
      (is (= (-> word-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :user-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "word UPDATE"
    (let [word-one (words/CREATE (db-pop/get-word))
          word-two (db-pop/get-word)]
      (let [res (rp/word-id-patch (:id word-one) word-two)]
        (is (= 200 (:status res)))
        (is (= (into word-two {:id (:id word-one)}) (ut/remove-db-only (words/READ (:id word-one))))))))
  (testing "word DELETE"
    (let [word-one (words/CREATE (db-pop/get-word))
          res (rp/word-id-delete (:id word-one))]
      (is (= 200 (:status res)))
      (is (= nil (words/READ (:id word-one)))))))


