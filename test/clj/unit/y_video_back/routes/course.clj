(ns y-video-back.routes.course
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
      [y-video-back.db.courses :as courses]))

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

(deftest temp
  (testing "temp"
    (is true)))

(deftest test-crse
  (testing "crse CREATE"
    (let [crse-one (db-pop/get-course)]
      (let [res (rp/course-post crse-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into crse-one {:id id})
                 (ut/remove-db-only (courses/READ id))))))))
  (testing "crse READ"
    (let [crse-one (ut/under-to-hyphen (courses/CREATE (db-pop/get-course)))
          res (rp/course-id-get (:id crse-one))]
      (is (= 200 (:status res)))
      (is (= (-> crse-one
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "course UPDATE"
    (let [crse-one (courses/CREATE (db-pop/get-course))
          crse-two (db-pop/get-course)]
      (let [res (rp/course-id-patch (:id crse-one) crse-two)]
        (is (= 200 (:status res)))
        (is (= (into crse-two {:id (:id crse-one)}) (ut/remove-db-only (courses/READ (:id crse-one))))))))
  (testing "course DELETE"
    (let [crse-one (courses/CREATE (db-pop/get-course))
          res (rp/course-id-delete (:id crse-one))]
      (is (= 200 (:status res)))
      (is (= nil (courses/READ (:id crse-one)))))))
