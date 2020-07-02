(ns y-video-back.routes.resource
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
      [y-video-back.db.resources :as resources]))

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

(deftest test-rsrc
  (testing "rsrc CREATE"
    (let [rsrc-one (db-pop/get-resource)]
      (let [res (rp/resource-post rsrc-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into rsrc-one {:id id})
                 (ut/remove-db-only (resources/READ id))))))))
  (testing "rsrc READ"
    (let [rsrc-one (ut/under-to-hyphen (resources/CREATE (db-pop/get-resource)))
          res (rp/resource-id-get (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (-> rsrc-one
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "resource UPDATE"
    (let [rsrc-one (resources/CREATE (db-pop/get-resource))
          rsrc-two (db-pop/get-resource)]
      (let [res (rp/resource-id-patch (:id rsrc-one) rsrc-two)]
        (is (= 200 (:status res)))
        (is (= (into rsrc-two {:id (:id rsrc-one)}) (ut/remove-db-only (resources/READ (:id rsrc-one))))))))
  (testing "resource DELETE"
    (let [rsrc-one (resources/CREATE (db-pop/get-resource))
          res (rp/resource-id-delete (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (= nil (resources/READ (:id rsrc-one)))))))

(deftest rsrc-all-colls
  (testing "find all collections by resource"
    (let [rsrc-one (db-pop/add-resource)
          coll-one (db-pop/add-collection)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (-> coll-one
                 (update :id str)
                 (update :owner str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest rsrc-all-conts
  (testing "find all contents by resource"
    (let [cont-one (db-pop/add-content)
          rsrc-id (:resource-id cont-one)
          res (rp/resource-id-contents rsrc-id)]
      (is (= 200 (:status res)))
      (is (= (-> cont-one
                 (update :id str)
                 (update :collection-id str)
                 (update :resource-id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest rsrc-all-files
  (testing "find all files by resource"
    (let [file-one (db-pop/add-file)
          rsrc-id (:resource-id file-one)
          res (rp/resource-id-files rsrc-id)]
      (is (= 200 (:status res)))
      (is (= (-> file-one
                 (update :id str)
                 (update :resource-id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))
