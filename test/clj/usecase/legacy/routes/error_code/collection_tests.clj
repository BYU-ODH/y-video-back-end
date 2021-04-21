(ns legacy.routes.error-code.collection-tests
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
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.users :as users]
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]))

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
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (mount.core/start #'y-video-back.handler/app))

(deftest collection-post
  (testing "add duplicated collection"
    (let [new-collection (into
                           (g/get-random-collection-without-id-no-owner)
                           {:owner (:id test-user-one)})
          add-collection-res (collections/CREATE new-collection)
          res (rp/collection-post new-collection)]
      (is (= 500 (:status res)))))
  (testing "add collection with nonexistent owner"
    (let [new-collection (into
                           (g/get-random-collection-without-id-no-owner)
                           {:owner (java.util.UUID/randomUUID)})
          res (rp/collection-post new-collection)]
      (is (= 500 (:status res))))))

(deftest collection-id-get
  (testing "read nonexistent collection"
    (let [res (rp/collection-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest collection-id-patch
  (testing "update nonexistent collection"
    (let [res (rp/collection-id-patch (java.util.UUID/randomUUID) (g/get-random-collection-without-id))]
      (is (= 404 (:status res)))))
  (testing "update collection to nonexistent user (all fields)"
    (let [new-collection (into (g/get-random-collection-without-id-no-owner)
                               {:owner (:id test-user-one)})
          add-coll-res (collections/CREATE new-collection)
          res (rp/collection-id-patch (:id add-coll-res) (into
                                                           (g/get-random-collection-without-id-no-owner)
                                                           {:owner (java.util.UUID/randomUUID)}))]
      (is (= 500 (:status res)))))
  (testing "update collection to nonexistent user (owner only)"
    (let [new-collection (into (g/get-random-collection-without-id-no-owner)
                               {:owner (:id test-user-one)})
          add-coll-res (collections/CREATE new-collection)
          res (rp/collection-id-patch (:id add-coll-res) {:owner (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res)))))
  (testing "update collection to taken name-owner pair (all fields)"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-two (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-one-res (collections/CREATE coll-one)
          coll-two-res (collections/CREATE coll-two)
          res (rp/collection-id-patch (:id coll-two-res) coll-one)]
      (is (= 500 (:status res)))))
  (testing "update collection to taken name-owner pair (name only)"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-two (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-one-res (collections/CREATE coll-one)
          coll-two-res (collections/CREATE coll-two)
          res (rp/collection-id-patch (:id coll-two-res) {:collection-name
                                                          (:collection-name coll-one)})]
      (is (= 500 (:status res)))))
  (testing "update collection to taken name-owner pair (owner only)"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-two (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-two)})
          coll-one-res (collections/CREATE coll-one)
          coll-two-res (collections/CREATE coll-two)
          res-name (rp/collection-id-patch (:id coll-two-res) {:collection-name
                                                               (:collection-name coll-one)})
          res (rp/collection-id-patch (:id coll-two-res) {:owner
                                                          (:owner coll-one)})]
      (is (= 200 (:status res-name)))
      (is (= 500 (:status res))))))
(deftest collection-id-delete
  (testing "delete nonexistent collection"
    (let [res (rp/collection-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest collection-add-user
  (testing "add nonexistent user to collection"
    (let [res (rp/collection-id-add-user (:id test-coll-one) "sadkjhasdvfkljsdhakj" 1)]
      (is (= 200 (:status res)))))
  (testing "add user to nonexistent collection"
    (let [res (rp/collection-id-add-user (java.util.UUID/randomUUID) (:username test-user-one) 1)]
      (is (= 404 (:status res)))))
  (testing "add user to collection, already connected"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          add-user-res (user-collections-assoc/CREATE {:username (:username new-user)
                                                       :collection-id (:id test-coll-one)
                                                       :account-role 1})
          res (rp/collection-id-add-user (:id test-coll-one) (:username new-user) 1)]
      (is (= 200 (:status res))))))

(deftest collection-add-users
  (testing "add nonexistent user to collection"
    (let [res (rp/collection-id-add-users (:id test-coll-one) ["sdalikuhasdflkjhaskglj"] 1)]
      (is (= 200 (:status res)))))
  (testing "add user to nonexistent collection"
    (let [res (rp/collection-id-add-users (java.util.UUID/randomUUID) [(:username test-user-one)] 1)]
      (is (= 404 (:status res)))))
  (testing "add user to collection, already connected"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          add-user-res (user-collections-assoc/CREATE {:username (:username new-user)
                                                       :collection-id (:id test-coll-one)
                                                       :account-role 1})
          res (rp/collection-id-add-users (:id test-coll-one) [(:username new-user)] 1)]
      (is (= 200 (:status res))))))

(deftest collection-remove-user
  (testing "remove nonexistent user from collection"
    (let [res (rp/collection-id-remove-user (:id test-coll-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "remove user from nonexistent collection"
    (let [res (rp/collection-id-remove-user (java.util.UUID/randomUUID) (:id test-user-one))]
      (is (= 404 (:status res)))))
  (testing "remove user from collection, not connected"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          res (rp/collection-id-remove-user (:id test-coll-one) (:id new-user))]
      (is (= 500 (:status res))))))


(deftest collection-add-course
  (testing "add existing course to collection"
    (let [course-one (db-pop/add-course)
          res (rp/collection-id-add-course (:id test-coll-one)
                                           (:department course-one)
                                           (:catalog-number course-one)
                                           (:section-number course-one))]
      (is (= 200 (:status res)))))
  (testing "add course to nonexistent collection"
    (let [res (rp/collection-id-add-course (java.util.UUID/randomUUID)
                                           (:department test-crse-one)
                                           (:catalog-number test-crse-one)
                                           (:section-number test-crse-one))]
      (is (= 404 (:status res)))))
  (testing "add course to collection, already connected"
    (let [new-course (db-pop/add-course)
          add-course-res (collection-courses-assoc/CREATE {:course-id (:id new-course)
                                                           :collection-id (:id test-coll-one)})
          res (rp/collection-id-add-course (:id test-coll-one)
                                           (:department new-course)
                                           (:catalog-number new-course)
                                           (:section-number new-course))]
      (is (= 500 (:status res))))))

(deftest collection-remove-course
  (testing "remove nonexistent course from collection"
    (let [res (rp/collection-id-remove-course (:id test-coll-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "remove course from nonexistent collection"
    (let [res (rp/collection-id-remove-course (java.util.UUID/randomUUID) (:id test-crse-one))]
      (is (= 404 (:status res)))))
  (testing "remove course from collection, not connected"
    (let [new-course (courses/CREATE (g/get-random-course-without-id))
          res (rp/collection-id-remove-course (:id test-coll-one) (:id new-course))]
      (is (= 500 (:status res))))))

(deftest collection-contents
  (testing "read contents for nonexistent collection"
    (let [res (rp/collection-id-contents (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "read contents for collection without contents"
    (let [new-collection (into (g/get-random-collection-without-id-no-owner)
                               {:owner (:id test-user-one)})
          add-coll-res (collections/CREATE new-collection)
          res (rp/collection-id-contents (:id add-coll-res))]
      (is (= 200 (:status res)))
      (is (= {:content [] :expired-content []} (m/decode-response-body res))))))

(deftest collection-courses
  (testing "read courses for nonexistent collection"
    (let [res (rp/collection-id-courses (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "read courses for collection without courses"
    (let [new-collection (into (g/get-random-collection-without-id-no-owner)
                               {:owner (:id test-user-one)})
          add-coll-res (collections/CREATE new-collection)
          res (rp/collection-id-courses (:id add-coll-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest collection-users
  (testing "read users for nonexistent collection"
    (let [res (rp/collection-id-users (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "read users for collection without users"
    (let [new-collection (into (g/get-random-collection-without-id-no-owner)
                               {:owner (:id test-user-one)})
          add-coll-res (collections/CREATE new-collection)
          res (rp/collection-id-users (:id add-coll-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))





