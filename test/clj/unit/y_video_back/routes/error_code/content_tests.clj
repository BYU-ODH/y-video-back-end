(ns y-video-back.routes.error-code.content-tests
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
      [y-video-back.db.contents :as contents]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resource-files-assoc :as resource-files-assoc]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-cont-one)))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (mount.core/start #'y-video-back.handler/app))

(defn get-new-content
  "Returns new content, with connected collection, resource, and user created in db. Annotation is not added to db."
  []
  (let [new-user (users/CREATE (g/get-random-user-without-id))
        new-coll (collections/CREATE (into (g/get-random-collection-without-id) {:owner (:id new-user)}))
        new-cont (resources/CREATE (g/get-random-resource-without-id))]
    (g/get-random-content-without-id (:id new-coll) (:id new-cont))))

(deftest content-post
  (testing "add duplicated content"
    (let [new-content (get-new-content)
          add-content-res (contents/CREATE new-content)
          res (rp/content-post new-content)]
      (is (= 500 (:status res)))))
  (testing "add content with nonexistent collection"
    (let [new-content (get-new-content)
          res (rp/content-post (into (dissoc new-content :collection-id) {:collection-id (java.util.UUID/randomUUID)}))]
      (is (= 500 (:status res)))))
  (testing "add content with nonexistent resource"
    (let [new-content (get-new-content)
          res (rp/content-post (into (dissoc new-content :resource-id) {:resource-id (java.util.UUID/randomUUID)}))]
      (is (= 500 (:status res)))))
  (testing "add content with nonexistent collection and resource"
    (let [new-content (get-new-content)
          res (rp/content-post (-> new-content
                                      (dissoc :collection-id :resource-id)
                                      (into {:collection-id (java.util.UUID/randomUUID)})
                                      (into {:resource-id (java.util.UUID/randomUUID)})))]
      (is (= 500 (:status res))))))

(deftest content-id-get
  (testing "read nonexistent content"
    (let [res (rp/content-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-update
  (testing "update nonexistent content"
    (let [res (rp/content-id-patch (java.util.UUID/randomUUID) (get-new-content))]
      (is (= 404 (:status res)))))
  (testing "update content to nonexistent collection"
    (let [add-annt (contents/CREATE (get-new-content))
          res (rp/content-id-patch (:id add-annt) {:collection-id (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res)))))
  (testing "update content to nonexistent resource"
    (let [add-annt (contents/CREATE (get-new-content))
          res (rp/content-id-patch (:id add-annt) {:resource-id (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res)))))
  (testing "update content to nonexistent collection and resource"
    (let [add-annt (contents/CREATE (get-new-content))
          res (rp/content-id-patch (:id add-annt) {:collection-id (java.util.UUID/randomUUID)
                                                      :resource-id (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res)))))
  (testing "update content to taken collection-resource (all fields)"
    (let [annt-one (get-new-content)
          annt-two (get-new-content)
          add-annt-one (contents/CREATE annt-one)
          add-annt-two (contents/CREATE annt-two)
          res (rp/content-id-patch (:id add-annt-two) annt-one)]
      (is (= 500 (:status res)))))
  (testing "update content to taken collection-resource (collection)"
    (let [annt-one (get-new-content)
          annt-two (get-new-content)
          add-annt-one (contents/CREATE annt-one)
          add-annt-two (contents/CREATE annt-two)
          res-setup (rp/content-id-patch (:id add-annt-two) {:resource-id (:resource-id annt-one)})
          res (rp/content-id-patch (:id add-annt-two) {:collection-id (:collection-id annt-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res)))))
  (testing "update content to taken collection-resource (resource)"
    (let [annt-one (get-new-content)
          annt-two (get-new-content)
          add-annt-one (contents/CREATE annt-one)
          add-annt-two (contents/CREATE annt-two)
          res-setup (rp/content-id-patch (:id add-annt-two) {:collection-id (:collection-id annt-one)})
          res (rp/content-id-patch (:id add-annt-two) {:resource-id (:resource-id annt-one)})]
      (is (= 200 (:status res-setup)))
      (is (= 500 (:status res))))))

(deftest content-delete
  (testing "delete nonexistent content"
    (let [res (rp/content-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-get-by-collection-and-resource
  (testing "find by valid ids, but no content"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          new-coll (collections/CREATE (into (g/get-random-collection-without-id) {:owner (:id new-user)}))
          new-cont (resources/CREATE (g/get-random-resource-without-id))
          res (rp/content-get-by-ids (:id new-coll) (:id new-cont))]
      (is (= 404 (:status res)))))
  (testing "find by invalid ids"
    (let [res (rp/content-get-by-ids (java.util.UUID/randomUUID) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "find by invalid collection id"
    (let [new-cont (resources/CREATE (g/get-random-resource-without-id))
          res (rp/content-get-by-ids (java.util.UUID/randomUUID) (:id new-cont))]
      (is (= 500 (:status res)))))
  (testing "find by invalid resource id"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          new-coll (collections/CREATE (into (g/get-random-collection-without-id) {:owner (:id new-user)}))
          res (rp/content-get-by-ids (:id new-coll) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res))))))
