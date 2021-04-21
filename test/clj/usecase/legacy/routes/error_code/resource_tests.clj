(ns legacy.routes.error-code.resource-tests
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
      [y-video-back.db.resources :as resources]
      [legacy.utils.db-populator :as db-pop]
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
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-file-one (db-pop/add-file (:id test-rsrc-one)))
  (mount.core/start #'y-video-back.handler/app))

; The only error thrown for resource-post is request body coercion (400)

(deftest resource-id-get
  (testing "read nonexistent resource"
    (let [res (rp/resource-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest resource-id-patch
  (testing "update nonexistent resource"
    (let [res (rp/resource-id-patch (java.util.UUID/randomUUID) (g/get-random-resource-without-id))]
      (is (= 404 (:status res))))))

(deftest resource-id-delete
  (testing "delete nonexistent resource"
    (let [res (rp/resource-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest resource-get-all-collections
  (testing "get collections for nonexistent resource"
    (let [res (rp/resource-id-collections (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get collections for resource with no collections"
    (let [new-resource (g/get-random-resource-without-id)
          add-cont-res (resources/CREATE new-resource)
          res (rp/resource-id-collections (:id add-cont-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest resource-get-all-files
  (testing "get files for nonexistent resource"
    (let [res (rp/resource-id-files (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get files for resource with no files"
    (let [new-resource (g/get-random-resource-without-id)
          add-cont-res (resources/CREATE new-resource)
          res (rp/resource-id-files (:id add-cont-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest resource-get-all-contents
  (testing "get contents for nonexistent resource"
    (let [res (rp/resource-id-contents (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get contents for resource with no contents"
    (let [new-resource (g/get-random-resource-without-id)
          add-rsrc-res (resources/CREATE new-resource)
          res (rp/resource-id-contents (:id add-rsrc-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))










