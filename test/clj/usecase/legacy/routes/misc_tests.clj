(ns legacy.routes.misc-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.core :as core]
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
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.user-creator :as uc]))

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

; (deftest read-where-and-res
;   (testing "temp"
;     (let [id-one (java.util.UUID/randomUUID)
;           id-two (java.util.UUID/randomUUID)]
;       (is (= "" (core/read-where-and :content-subtitles-assoc-undeleted [:content-id :subtitle-id] [id-one id-two]))))))