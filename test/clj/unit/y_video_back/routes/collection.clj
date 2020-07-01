(ns y-video-back.routes.collection
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.collections :as collections]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(deftest test-coll
  (testing "coll CREATE"
    (let [coll-one (db-pop/get-collection)]
      (let [res (rp/collection-post coll-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into coll-one {:id id})
                 (ut/remove-db-only (collections/READ id))))))))
  (testing "coll READ"
    (let [coll-one (ut/under-to-hyphen (collections/CREATE (db-pop/get-collection)))
          res (rp/collection-id-get (:id coll-one))]
      (is (= 200 (:status res)))
      (is (= (-> coll-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :owner str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "collection UPDATE"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          coll-two (db-pop/get-collection)]
      (let [res (rp/collection-id-patch (:id coll-one) coll-two)]
        (is (= 200 (:status res)))
        (is (= (into coll-two {:id (:id coll-one)}) (ut/remove-db-only (collections/READ (:id coll-one))))))))
  (testing "collection DELETE"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          res (rp/collection-id-delete (:id coll-one))]
      (is (= 200 (:status res)))
      (is (= nil (collections/READ (:id coll-one)))))))
