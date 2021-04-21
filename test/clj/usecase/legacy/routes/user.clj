(ns legacy.routes.user
    (:require
      [y-video-back.config :refer [env]]
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
      [y-video-back.db.users :as users]
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

(deftest user-crud
  (testing "user CREATE"
    (let [user-one (g/get-random-user-without-id)]
      (let [res (rp/user-post user-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into user-one {:id id})
                 (ut/remove-db-only (users/READ id))))))))
  (testing "user READ"
    (let [user-one (ut/under-to-hyphen (users/CREATE (db-pop/get-user)))
          res (rp/user-id-get (:id user-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only user-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "user UPDATE"
    (let [user-one (users/CREATE (db-pop/get-user))
          user-two (db-pop/get-user)]
      (let [res (rp/user-id-patch (:id user-one) user-two)]
        (is (= 200 (:status res)))
        (is (= (into user-two {:id (:id user-one)}) (ut/remove-db-only (users/READ (:id user-one))))))))
  (testing "user DELETE"
    (let [user-one (users/CREATE (db-pop/get-user))
          res (rp/user-id-delete (:id user-one))]
      (is (= 200 (:status res)))
      (is (= nil (users/READ (:id user-one)))))))

(deftest user-all-colls
  (testing "find all collections by user"
    (let [user-one (db-pop/add-user)
          user-two (db-pop/add-user)
          coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          coll-thr (db-pop/add-collection (:id user-two))
          coll-fou (db-pop/add-collection (:id user-two))
          ; Connect one-one, two-two
          user-coll-one (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one))
          user-coll-two (db-pop/add-user-coll-assoc (:username user-two) (:id coll-two))
          res-one (rp/user-id-collections (:id user-one))
          res-two (rp/user-id-collections (:id user-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= '()
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (frequencies (map #(-> %
                                    (update :id str)
                                    (update :owner str)
                                    (ut/remove-db-only))
                               [coll-thr coll-fou]))
             (frequencies (map ut/remove-db-only (m/decode-response-body res-two))))))))

(deftest user-all-crses
  (testing "find all courses by user"
    (let [user-one (db-pop/add-user)
          user-two (db-pop/add-user)
          crse-one (db-pop/add-course)
          crse-two (db-pop/add-course)
          ; Connect one-one, two-two
          user-crse-one (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one))
          user-crse-two (db-pop/add-user-crse-assoc (:id user-two) (:id crse-two))
          res-one (rp/user-id-courses (:id user-one))
          res-two (rp/user-id-courses (:id user-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> crse-one
                 (update :id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> crse-two
                 (update :id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))

(defn remove-course-db-fields
  [course]
  (-> course
      (ut/remove-db-only)
      (dissoc :id :account-role :user-id)))

(defn check-against-test-user
  [user-id]
  (is (= (frequencies (get-in env [:test-user
                                   :courses]))
         (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER user-id))))))
