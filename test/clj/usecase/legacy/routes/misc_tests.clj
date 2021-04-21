(ns legacy.routes.misc-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.core :as core]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.resources :as resources]
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
  (mount.core/start #'y-video-back.handler/app))

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
