(ns y-video-back.routes.file
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
      [y-video-back.db.files :as files]))


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

(deftest test-file
  (testing "file CREATE"
    (let [file-one (db-pop/get-file)]
      (let [res (rp/file-post file-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into file-one {:id id})
                 (ut/remove-db-only (files/READ id))))))))
  (testing "file READ"
    (let [file-one (ut/under-to-hyphen (files/CREATE (db-pop/get-file)))
          res (rp/file-id-get (:id file-one))]
      (is (= 200 (:status res)))
      (is (= (-> file-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :resource-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "file UPDATE"
    (let [file-one (files/CREATE (db-pop/get-file))
          file-two (db-pop/get-file)]
      (let [res (rp/file-id-patch (:id file-one) file-two)]
        (is (= 200 (:status res)))
        (is (= (into file-two {:id (:id file-one)}) (ut/remove-db-only (files/READ (:id file-one))))))))
  (testing "file DELETE"
    (let [file-one (files/CREATE (db-pop/get-file))
          res (rp/file-id-delete (:id file-one))]
      (is (= 200 (:status res)))
      (is (= nil (files/READ (:id file-one)))))))
