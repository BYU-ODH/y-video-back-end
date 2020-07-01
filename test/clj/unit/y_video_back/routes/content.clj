(ns y-video-back.routes.content
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.contents :as contents]))

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

(deftest test-cont
  (testing "cont CREATE"
    (let [cont-one (db-pop/get-content)]
      (let [res (rp/content-post cont-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into cont-one {:id id})
                 (ut/remove-db-only (contents/READ id))))))))
  (testing "cont READ"
    (let [cont-one (ut/under-to-hyphen (contents/CREATE (db-pop/get-content)))
          res (rp/content-id-get (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= (-> cont-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :collection-id str)
                 (update :resource-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "content UPDATE"
    (let [cont-one (contents/CREATE (db-pop/get-content))
          cont-two (db-pop/get-content)]
      (let [res (rp/content-id-patch (:id cont-one) cont-two)]
        (is (= 200 (:status res)))
        (is (= (into cont-two {:id (:id cont-one)}) (ut/remove-db-only (contents/READ (:id cont-one))))))))
  (testing "content DELETE"
    (let [cont-one (contents/CREATE (db-pop/get-content))
          res (rp/content-id-delete (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id cont-one)))))))
