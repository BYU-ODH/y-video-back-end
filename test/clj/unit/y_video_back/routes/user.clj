(ns y-video-back.routes.user
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
      [y-video-back.db.users :as users]))

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
