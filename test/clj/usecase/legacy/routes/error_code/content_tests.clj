(ns legacy.routes.error-code.content-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.files :as files]
      [y-video-back.db.courses :as courses]
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
  (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-cont-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000")))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (mount.core/start #'y-video-back.handler/app))

(defn get-new-content
  "Returns new content, with connected collection, resource, and user created in db. Content is not add to db."
  []
  (let [new-user (users/CREATE (g/get-random-user-without-id))
        new-coll (collections/CREATE (into (g/get-random-collection-without-id) {:owner (:id new-user)}))
        new-rsrc (resources/CREATE (g/get-random-resource-without-id))
        new-file (files/CREATE (g/get-random-file-without-id (:id new-rsrc)))]
    (g/get-random-content-without-id (:id new-coll) (:id new-rsrc) (:id new-file))))

(deftest content-post
  (comment (testing "add duplicated content")
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
      (is (= 500 (:status res))))))

(deftest content-delete
  (testing "delete nonexistent content"
    (let [res (rp/content-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-add-view
  (testing "add view to nonexistent content"
    (let [res (rp/content-id-add-view (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest content-subtitles
  (testing "invalid content"
    (let [res (rp/content-id-subtitles (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "no subtitles"
    (let [cont-one (db-pop/add-content)
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res))))))







