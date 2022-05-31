(ns legacy.routes.media
    (:require
     [y-video-back.config :refer [env]]
     [clojure.test :refer :all]
     [y-video-back.handler :refer :all]
     [legacy.db.test-util :as tcore]
     [muuntaja.core :as m]
     [clojure.java.jdbc :as jdbc]
     [mount.core :as mount]
     [legacy.utils.route-proxy.proxy :as rp]
     [y-video-back.db.core :refer [*db*] :as db]
     [legacy.utils.utils :as ut]
     [legacy.utils.db-populator :as db-pop]
     [y-video-back.db.users :as users]
     [y-video-back.db.files :as files]
     [y-video-back.db.file-keys :as file-keys]
     [y-video-back.user-creator :as uc]
     [clojure.java.io :as io]
     [clojure.java.shell :as shell]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (ut/renew-db)
    (f)
    (ut/delete-all-files (-> env :FILES :media-url))))

(tcore/basic-transaction-fixtures
  (def user-one (users/CREATE (assoc (dissoc (db-pop/get-user) :account-type) :account-type 0)))
  (def rsrc-one (db-pop/add-resource))
  (io/copy (io/file (subs (str (.toURI (io/resource "small_test_video.mp4"))) 5))
           (io/file (str (-> env :FILES :media-url) "small_test_video.mp4")))
  (def file-one (files/CREATE {:resource-id (:id rsrc-one)
                               :filepath "small_test_video.mp4" ; move this into github repository?
                               :file_version (:id (db-pop/add-language))
                               :metadata "text"}))
  (mount.core/start #'y-video-back.handler/app))

; get file key - all good
(deftest file-key-and-streaming
  (testing "get file-key with admin user, then stream"
    (let [res (rp/get-file-key (uc/user-id-to-session-id (:id user-one)) (:id file-one))
          res-body (m/decode-response-body res)]
      (is (= 200 (:status res)))
      (is (contains? res-body :file-key))
      (let [file-key (:file-key res-body)
            res (rp/stream-media file-key)]
        (is (= 200 (:status res)))
        (is (= java.io.File (type (:body res))))
        (is (= (str (clojure.string/trim-newline (:out (shell/sh "pwd"))) "/" (-> env :FILES :media-url) (:filepath file-one)) (.getAbsolutePath (:body res)))))))
  (testing "get file-key with admin user, then let expire"
    (let [res (rp/get-file-key (uc/user-id-to-session-id (:id user-one)) (:id file-one))
          res-body (m/decode-response-body res)]
      (is (= 200 (:status res)))
      (is (contains? res-body :file-key))
      (Thread/sleep (* 3 (-> env :FILES :timeout)))
      (is (not (nil? (file-keys/READ (ut/to-uuid (:file-key res-body))))))
      (let [file-key (:file-key res-body)
            res (rp/stream-media file-key)]
        (is (= 404 (:status res))))
      (is (nil? (file-keys/READ (ut/to-uuid (:file-key res-body)))))
      (let [file-key (:file-key res-body)
            res (rp/stream-media file-key)]
        (is (= 404 (:status res)))))))
; TODO Tests to add
; get file key, bad session-id
; get file key, user doesn't have permission to file
; etc



