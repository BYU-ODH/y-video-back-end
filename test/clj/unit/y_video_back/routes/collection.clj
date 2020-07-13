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
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.users :as users]
      [y-video-back.db.courses :as courses]
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

(deftest test-session-id-header
  (testing "coll CREATE - session id header"
    (let [coll-one (db-pop/get-collection)]
      (let [res (rp/collection-post coll-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
              ;header (m/decode-response-body res)]
          (is (= (into coll-one {:id id})
                 (ut/remove-db-only (collections/READ id)))))))))

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

(deftest coll-add-user
  (testing "add user to collection"
    (let [coll-one (db-pop/add-collection)
          user-one (db-pop/add-user)]
      (is (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:id user-one)])))
      (let [res (rp/collection-id-add-user (:id coll-one)
                                           (:username user-one)
                                           0)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list {:id id
                        :collection-id (:id coll-one)
                        :user-id (:id user-one)
                        :account-role 0})
                 (map ut/remove-db-only (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:id user-one)])))))))))

(deftest coll-remove-user
  (testing "remove user from collection"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          user-one (users/CREATE (db-pop/get-user))
          user-coll (user-collections-assoc/CREATE (g/get-random-user-collections-assoc-without-id (:id user-one) (:id coll-one)))]
      (is (not (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:id user-one)]))))
      (let [res (rp/collection-id-remove-user (:id coll-one) (:username user-one))]
        (is (= 200 (:status res)))
        (is (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:id user-one)])))))))

(deftest coll-add-crse
  (testing "add course to collection"
    (let [coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)]
      (is (= '() (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)])))
      (let [res (rp/collection-id-add-course (:id coll-one) (:id crse-one))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list {:id id
                        :collection-id (:id coll-one)
                        :course-id (:id crse-one)})
                 (map ut/remove-db-only (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)])))))))))

(deftest coll-remove-crse
  (testing "remove course from collection"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          crse-one (courses/CREATE (db-pop/get-course))
          crse-coll (collection-courses-assoc/CREATE (g/get-random-collection-courses-assoc-without-id (:id coll-one) (:id crse-one)))]
      (is (not (= '() (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)]))))
      (let [res (rp/collection-id-remove-course (:id coll-one) (:id crse-one))]
        (is (= 200 (:status res)))
        (is (= '() (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)])))))))


(deftest coll-all-users
  (testing "find all users by collection"
    (let [user-one (db-pop/add-user)
          user-two (db-pop/add-user)
          coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          ; Connect one-one, two-two
          user-coll-one (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one))
          user-coll-two (db-pop/add-user-coll-assoc (:id user-two) (:id coll-two))
          res-one (rp/collection-id-users (:id coll-one))
          res-two (rp/collection-id-users (:id coll-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> user-one
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-one)) :account-role (:account-role user-coll-one)})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> user-two
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-two)) :account-role (:account-role user-coll-two)})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))

(deftest coll-all-conts
  (testing "find all contents by collection"
    (let [cont-one (db-pop/add-content)
          coll-id (:collection-id cont-one)
          res (rp/collection-id-contents coll-id)]
      (is (= 200 (:status res)))
      (is (= (-> cont-one
                 (update :id str)
                 (update :collection-id str)
                 (update :resource-id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest coll-all-crses
  (testing "find all courses by collection"
    (let [crse-one (db-pop/add-course)
          crse-two (db-pop/add-course)
          coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          ; Connect one-one, two-two
          crse-coll-one (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-coll-two (db-pop/add-coll-crse-assoc (:id coll-two) (:id crse-two))
          res-one (rp/collection-id-courses (:id coll-one))
          res-two (rp/collection-id-courses (:id coll-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> crse-one
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-one))})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> crse-two
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-two))})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))
