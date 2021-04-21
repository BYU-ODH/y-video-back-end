(ns legacy.routes.permissions.account-type.media-tests
  (:require
    [clojure.java.io :as io]
    [clojure.java.jdbc :as jdbc]
    [clojure.test :refer :all]
    [legacy.db.test-util :as tcore]
    [legacy.utils.db-populator :as db-pop]
    [legacy.utils.route-proxy.proxy :as rp]
    [legacy.utils.utils :as ut]
    [mount.core :as mount]
    [muuntaja.core :as m]
    [y-video-back.config :refer [env]]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.db.files :as files]
    [y-video-back.handler :refer :all]
    [y-video-back.user-creator :as uc]))

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
