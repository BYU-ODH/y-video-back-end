(ns legacy.routes.error-code.file-tests
    (:require
      [y-video-back.config :refer [env]]
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
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
    (f)
    (ut/delete-all-files (-> env :FILES :media-url))
    (ut/delete-all-files (-> env :FILES :test-temp))))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (db-pop/add-file (:id test-rsrc-one)))
  (mount.core/start #'y-video-back.handler/app))

(deftest file-post
  (testing "add duplicated file (i.e. duplicate filename)"
    (let [filecontent (ut/get-filecontent)
          file-one (dissoc (db-pop/get-file) :filepath)
          res-one (rp/file-post file-one filecontent)
          res-two (rp/file-post file-one filecontent)]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))))
  (testing "add file to nonexistent resource"
    (let [filecontent (ut/get-filecontent)
          file-one (dissoc (db-pop/get-file (java.util.UUID/randomUUID)) :filepath)
          res (rp/file-post file-one filecontent)]
      (is (= 500 (:status res)))))
  (testing "add file with nonexistent language"
    (let [filecontent (ut/get-filecontent)
          lang-one (db-pop/get-language)
          file-one (dissoc (db-pop/get-file) :filepath)
          res (rp/file-post (assoc (dissoc file-one :file-version) :file-version (:id lang-one)) filecontent)]
      (is (= 200 (:status res))))))

(deftest file-id-get
  (testing "read nonexistent file"
    (let [res (rp/file-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "nil as id with files in system"
    (let [filecontent (ut/get-filecontent)
          file-one (dissoc (db-pop/get-file) :filepath)
          res-one (rp/file-post file-one filecontent)
          res (rp/file-id-get nil)]
      (is (= 200 (:status res-one)))
      (is (= 404 (:status res))))))

(deftest file-id-patch
  (testing "update nonexistent file"
    (let [res (rp/file-id-patch (java.util.UUID/randomUUID) (db-pop/get-file (:id test-rsrc-one)))]
      (is (= 404 (:status res)))))
  (testing "update file to taken filepath (all fields)"
    (let [file-one (db-pop/get-file (:id test-rsrc-one))
          file-two (db-pop/get-file (:id test-rsrc-one))
          file-one-res (files/CREATE file-one)
          file-two-res (files/CREATE file-two)
          res (rp/file-id-patch (:id file-two-res) file-one)]
      (is (= 500 (:status res)))))
  (testing "update file to taken filepath (filepath)"
    (let [file-one (db-pop/get-file (:id test-rsrc-one))
          file-two (db-pop/get-file (:id test-rsrc-one))
          file-one-res (files/CREATE file-one)
          file-two-res (files/CREATE file-two)
          res (rp/file-id-patch (:id file-two-res) {:filepath (:filepath file-one)})]
      (is (= 500 (:status res)))))
  (testing "update file to nonexistent resource (all fields)"
    (let [file-one (db-pop/get-file (:id test-rsrc-one))
          file-two (db-pop/get-file (java.util.UUID/randomUUID))
          file-one-res (files/CREATE file-one)
          res (rp/file-id-patch (:id file-one-res) file-two)]
      (is (= 500 (:status res)))))
  (testing "update file to nonexistent resource (resource-id only)"
    (let [file-one (db-pop/get-file (:id test-rsrc-one))
          file-one-res (files/CREATE file-one)
          res (rp/file-id-patch (:id file-one-res) {:resource-id (java.util.UUID/randomUUID)})]
      (is (= 500 (:status res)))))
  (testing "update file to nonexistent language (all fields)"
    (let [file-one (db-pop/get-file (:id test-rsrc-one))
          file-two (db-pop/get-file (java.util.UUID/randomUUID))
          lang-one (db-pop/get-language)
          file-one-res (files/CREATE file-one)
          res (rp/file-id-patch (:id file-one-res) (assoc (dissoc file-two :file-version) :file-version (:id lang-one)))]
      (is (= 500 (:status res)))))
  (testing "update file to nonexistent language (language only)"
    (let [file-one (db-pop/get-file (:id test-rsrc-one))
          lang-one (db-pop/get-language)
          file-one-res (files/CREATE file-one)
          res (rp/file-id-patch (:id file-one-res) {:file-version (:id lang-one)})]
      (is (= 500 (:status res))))))

(deftest file-id-delete
  (testing "delete nonexistent file"
    (let [res (rp/file-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))








