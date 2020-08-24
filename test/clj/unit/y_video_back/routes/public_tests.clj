(ns y-video-back.routes.public-tests
    (:require
      [y-video-back.config :refer [env]]
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
      [y-video-back.db.collections :as collections]
      [y-video-back.db.users :as users]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]))

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

(comment
  ; read public collection
  (deftest read-public-collection-by-id
    (testing "read public collection by id"
      (let [coll-one (db-pop/add-public-collection)
            res (rp/public-collection-id-get (:id coll-one))]
        (is (= 200 (:status res)))
        (is (= (-> coll-one
                   (ut/remove-db-only)
                   (update :id str)
                   (update :owner str))
               (ut/remove-db-only (m/decode-response-body res)))))))
  ; read all public collections
  (deftest read-public-collections
    (testing "read all public collections"
      (let [coll-one (db-pop/add-public-collection)
            coll-two (db-pop/add-public-collection)
            res (rp/public-collection-get-all)]
        (is (= 200 (:status res)))
        (is (= (map #(-> %
                         (update :id str)
                         (update :owner str)
                         (ut/remove-db-only))
                    [coll-one coll-two])
               (map ut/remove-db-only (m/decode-response-body res)))))))

  ; read public content
  (deftest read-public-content-by-id
    (testing "read public content by id"
      (let [cont-one (db-pop/add-public-content)
            res (rp/public-content-id-get (:id cont-one))]
        (is (= 200 (:status res)))
        (is (= (-> cont-one
                   (ut/remove-db-only)
                   (update :id str)
                   (update :collection-id str)
                   (update :resource-id str))
               (ut/remove-db-only (m/decode-response-body res)))))))

  ; read all public contents
  (deftest read-public-contents
    (testing "read all public contents"
      (let [cont-one (db-pop/add-public-content)
            cont-two (db-pop/add-public-content)
            res (rp/public-content-get-all)]
        (is (= 200 (:status res)))
        (is (= (map #(-> %
                         (update :id str)
                         (update :collection-id str)
                         (update :resource-id str))
                    [cont-one cont-two])
               (map ut/remove-db-only (m/decode-response-body res)))))))

  ; read public resource
  (deftest read-public-resource-by-id
    (testing "read public resource by id"
      (let [rsrc-one (db-pop/add-public-resource)
            res (rp/public-resource-id-get (:id rsrc-one))]
        (is (= 200 (:status res)))
        (is (= (-> rsrc-one
                   (ut/remove-db-only)
                   (update :id str))
               (ut/remove-db-only (m/decode-response-body res)))))))

  ; read all public resources
  (deftest read-public-resources
    (testing "read all public resources"
      (let [rsrc-one (db-pop/add-public-resource)
            rsrc-two (db-pop/add-public-resource)
            res (rp/public-resource-get-all)]
        (is (= 200 (:status res)))
        (is (= (map #(-> %
                         (update :id str))
                    [rsrc-one rsrc-two])
               (m/decode-response-body res))))))

  ; stream public media
  (deftest stream-public-resource
    (testing "get file-key for file of public resource, then stream"
      (let [rsrc-one (db-pop/add-public-resource)
            lang-one (db-pop/add-language)
            file-one (files/CREATE {:resource-id (:id rsrc-one)
                                    :filepath "persistent/test_kitten.mp4" ; move this into github repository?
                                    :file_version (:id lang-one)
                                    :metadata "text"})
            res (rp/get-file-key "00000000-0000-0000-0000-000000000000" (:id file-one))
            res-body (m/decode-response-body res)]
        (is (= 200 (:status res)))
        (is (contains? res-body :file-key))
        (let [file-key (:file-key res-body)
              res (rp/stream-media file-key)]
          (is (= 200 (:status res)))
          (is (= java.io.File (type (:body res))))
          (is (= (str (-> env :FILES :media-url) (:filepath file-one)) (.getAbsolutePath (:body res)))))))))
