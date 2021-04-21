(ns legacy.routes.course
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
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]))

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

(deftest crse-add-user
  (testing "add user to course"
    (let [crse-one (db-pop/add-course)
          user-one (db-pop/add-user)]
      (is (= '() (user-courses-assoc/READ-BY-IDS [(:id crse-one) (:id user-one)])))
      (let [res (rp/course-id-add-user (:id crse-one)
                                       (:id user-one)
                                       0)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list {:id id
                        :course-id (:id crse-one)
                        :user-id (:id user-one)
                        :account-role 0})
                 (map ut/remove-db-only (user-courses-assoc/READ-BY-IDS [(:id crse-one) (:id user-one)])))))))))

(deftest crse-remove-user
  (testing "remove user from course"
    (let [crse-one (db-pop/add-course)
          user-one (db-pop/add-user)
          user-crse (user-courses-assoc/CREATE (g/get-random-user-courses-assoc-without-id (:id user-one) (:id crse-one)))]
      (is (not (= '() (user-courses-assoc/READ-BY-IDS [(:id crse-one) (:id user-one)]))))
      (let [res (rp/course-id-remove-user (:id crse-one) (:id user-one))]
        (is (= 200 (:status res)))
        (is (= '() (user-courses-assoc/READ-BY-IDS [(:id crse-one) (:id user-one)])))))))

(deftest crse-all-users
  (testing "find all users by course"
    (let [user-one (db-pop/add-user)
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          crse-two (db-pop/add-course)
          ; Connect one-one, two-two
          user-crse-one (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one))
          user-crse-two (db-pop/add-user-crse-assoc (:id user-two) (:id crse-two))
          res-one (rp/course-id-users (:id crse-one))
          res-two (rp/course-id-users (:id crse-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> user-one
                 (update :id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> user-two
                 (update :id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))

(deftest crse-all-colls
  (testing "find all collections by course"
    (let [coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          crse-one (db-pop/add-course)
          crse-two (db-pop/add-course)
          ; Connect one-one, two-two
          coll-crse-one (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          coll-crse-two (db-pop/add-coll-crse-assoc (:id coll-two) (:id crse-two))
          res-one (rp/course-id-collections (:id crse-one))
          res-two (rp/course-id-collections (:id crse-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> coll-one
                 (update :id str)
                 (update :owner str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> coll-two
                 (update :id str)
                 (update :owner str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))

