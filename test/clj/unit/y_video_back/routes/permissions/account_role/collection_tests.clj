(ns y-video-back.routes.permissions.account-role.collection-tests
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
    [y-video-back.db.contents :as contents]
    [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
    [y-video-back.db.users-by-collection :as users-by-collection]
    [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.resources :as resources]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.words :as words]
    [y-video-back.utils.utils :as ut]
    [y-video-back.utils.db-populator :as db-pop]
    [y-video-back.user-creator :as uc]))

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

;post: /api/collection
(deftest collection-post
  (testing "instructor, collection-post, self as owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-one))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-post, other instructor as owner"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-two))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 401 (:status res)))))
  (testing "student, collection-post, self as owner"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/get-collection (:id user-one))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 401 (:status res))))))

;get: /api/collection/{id}
;delete: /api/collection/{id}
;patch: /api/collection/{id}
;post: /api/collection/{id}/add-user
;post: /api/collection/{id}/remove-user
;post: /api/collection/{id}/add-course
;post: /api/collection/{id}/remove-course
;get: /api/collection/{id}/contents
;get: /api/collection/{id}/courses
;get: /api/collection/{id}/users
