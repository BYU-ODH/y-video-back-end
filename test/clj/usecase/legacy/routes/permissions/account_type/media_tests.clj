(ns legacy.routes.permissions.account-type.media-tests
  (:require
    [y-video-back.config :refer [env]]
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
    [legacy.utils.utils :as ut]
    [legacy.utils.db-populator :as db-pop]
    [y-video-back.user-creator :as uc]
    [clojure.java.io :as io]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (ut/renew-db)
    (f)
    (ut/delete-all-files (-> env :FILES :media-url))
    (ut/delete-all-files (-> env :FILES :test-temp))))

(tcore/basic-transaction-fixtures
  (def admin-one (db-pop/add-user "admin"))
  (def rsrc-one (db-pop/add-resource))
  (def file-one (files/CREATE {:resource-id (:id rsrc-one)
                               :filepath (str (io/resource "small_test_video.mp4")) ; move this into github repository?
                               :file_version (:id (db-pop/add-language))
                               :metadata "text"}))
  (mount.core/start #'y-video-back.handler/app))

;get: /api/media/get-file-key/{file-id}
(deftest media-get-file-key
  (testing "admin - no connection, media-get-file-key"
    (let [user-one (db-pop/add-user "admin")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res-file (rp/file-post file-one filecontent)]
      (is (= 200 (:status res-file)))
      (let [file-id (:id (m/decode-response-body res-file))
            res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                                 file-id)]
        (is (= 200 (:status res))))))
  (testing "lab assistant - no connection, media-get-file-key"
    (let [user-one (db-pop/add-user "lab-assistant")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res-file (rp/file-post file-one filecontent)]
      (is (= 200 (:status res-file)))
      (let [file-id (:id (m/decode-response-body res-file))
            res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                                 file-id)]
        (is (= 200 (:status res))))))
  (testing "instructor - no connection, media-get-file-key"
    (let [user-one (db-pop/add-user "instructor")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res-file (rp/file-post file-one filecontent)]
      (is (= 200 (:status res-file)))
      (let [file-id (:id (m/decode-response-body res-file))
            res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                                 file-id)]
        (is (= 403 (:status res))))))
  (testing "student - no connection, media-get-file-key"
    (let [user-one (db-pop/add-user "student")
          file-one (db-pop/get-file)
          filecontent (ut/get-filecontent)
          res-file (rp/file-post file-one filecontent)]
      (is (= 200 (:status res-file)))
      (let [file-id (:id (m/decode-response-body res-file))
            res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                                 file-id)]
        (is (= 403 (:status res)))))))