(ns legacy.routes.subtitle
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
      [y-video-back.db.subtitles :as subtitles]))

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

(deftest test-sbtl
  (testing "sbtl CREATE"
    (let [sbtl-one (db-pop/get-subtitle)]
      (let [res (rp/subtitle-post sbtl-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into sbtl-one {:id id})
                 (ut/remove-db-only (subtitles/READ id))))))))
  (testing "sbtl READ"
    (let [sbtl-one (ut/under-to-hyphen (subtitles/CREATE (db-pop/get-subtitle)))
          res (rp/subtitle-id-get (:id sbtl-one))]
      (is (= 200 (:status res)))
      (is (= (-> sbtl-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :content-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "subtitle UPDATE"
    (let [sbtl-one (subtitles/CREATE (db-pop/get-subtitle))
          sbtl-two (db-pop/get-subtitle)]
      (let [res (rp/subtitle-id-patch (:id sbtl-one) sbtl-two)]
        (is (= 200 (:status res)))
        (is (= (into sbtl-two {:id (:id sbtl-one)}) (ut/remove-db-only (subtitles/READ (:id sbtl-one))))))))
  (testing "subtitle DELETE"
    (let [sbtl-one (subtitles/CREATE (db-pop/get-subtitle))
          res (rp/subtitle-id-delete (:id sbtl-one))]
      (is (= 200 (:status res)))
      (is (= nil (subtitles/READ (:id sbtl-one)))))))


