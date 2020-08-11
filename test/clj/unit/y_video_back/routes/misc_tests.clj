(ns y-video-back.routes.misc-tests
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
      [y-video-back.db.contents :as contents]
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
      [y-video-back.routes.service-handlers.utils.db-utils :as dbu]
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

(deftest permission-middleware-temp
  (testing "check role permissions"
    (let [user-one (db-pop/add-user 2)
          ;coll-one (db-pop/add-collection (:id user-one))
          coll-two (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-two) 2)
          rsrc-one (db-pop/add-resource)
          ;cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-two) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res))))))

(deftest test-cont-add-view
  (testing "content add view"
    (let [cont-one (db-pop/add-content)
          rsrc-one (resources/READ (:resource-id cont-one))
          res (rp/content-id-add-view (:id cont-one))]
      (is (= 200 (:status res)))
      (let [new-content (contents/READ (:id cont-one))
            new-resource (resources/READ (:resource-id cont-one))]
        (is (= (+ 1 (:views cont-one)) (:views new-content)))
        (is (= (+ 1 (:views rsrc-one)) (:views new-resource)))))))
