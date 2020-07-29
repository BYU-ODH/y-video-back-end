(ns y-video-back.routes.file
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
      [y-video-back.db.files :as files]
      [clojure.java.io :as io]))

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
    (let [filecontent {:tempfile (ut/create-temp-file "test_kitten.mp4")
                       :content-type "application/octet-stream"
                       :filename "test_kitten.mp4"}
          file-one (db-pop/get-file)]
      (let [res (rp/file-post [file-one filecontent])]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))
              db-file (ut/remove-db-only (files/READ id))]
          (is (= (assoc (dissoc (into file-one {:id id}) :filepath) :filepath (str "test_kitten-" id ".mp4"))
                 db-file))
          (is (.exists (io/as-file (str (-> env :FILES :media-url) (:filepath db-file)))))))))

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
