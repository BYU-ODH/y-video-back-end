(ns legacy.routes.file
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
      [y-video-back.db.files :as files]
      [y-video-back.db.auth-tokens :as auth-tokens]
      [y-video-back.db.languages :as languages]
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
    (ut/delete-all-files (-> env :FILES :media-trash-url))
    (ut/delete-all-files (-> env :FILES :test-temp))))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(deftest test-file
  (testing "file CREATE"
    (let [filecontent (ut/get-filecontent)
          file-one (dissoc (db-pop/get-file) :filepath :aspect-ratio)]
      (let [res (rp/file-post file-one filecontent)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))
              db-file (dissoc (ut/remove-db-only (files/READ id)) :aspect-ratio)]
          (is (= (assoc (into file-one {:id id}) :filepath (:filepath db-file))
                 db-file))
          (is (.exists (io/as-file (str (-> env :FILES :media-url) (:filepath db-file)))))))))
  (testing "file CREATE session-id remains valid"
    (let [filecontent (ut/get-filecontent)
          file-one (dissoc (db-pop/get-file) :filepath :aspect-ratio)
          user-one (db-pop/add-user "admin")
          token (:id (auth-tokens/CREATE {:user-id (:id user-one)}))]
      (is (not (nil? (auth-tokens/READ-UNEXPIRED token))))
      (let [res (rp/file-post token file-one filecontent)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))
              db-file (dissoc (ut/remove-db-only (files/READ id)) :aspect-ratio)]
          (is (= (assoc (into file-one {:id id}) :filepath (:filepath db-file))
                 db-file))
          (is (.exists (io/as-file (str (-> env :FILES :media-url) (:filepath db-file)))))
          (is (not (nil? (auth-tokens/READ-UNEXPIRED token))))))))
  (testing "file CREATE with new file-version/langauge"
    (let [filecontent (ut/get-filecontent)
          file-one (-> (db-pop/get-file)
                       (dissoc :filepath)
                       (dissoc :file-version)
                       (dissoc :aspect-ratio)
                       (assoc :file-version "dasfowinvkjha"))]
      ; Check file-version is not in language table
      (is (not (languages/EXISTS? (:file-version file-one))))
      (let [res (rp/file-post file-one filecontent)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))
              db-file (dissoc (ut/remove-db-only (files/READ id)) :aspect-ratio)]
          (is (= (assoc (into file-one {:id id}) :filepath (:filepath db-file))
                 db-file))
          (is (.exists (io/as-file (str (-> env :FILES :media-url) (:filepath db-file)))))))
      ; Check file-version is in language table
      (is (languages/EXISTS? (:file-version file-one)))))

  (testing "file READ"
    (let [file-one (ut/under-to-hyphen (files/CREATE (db-pop/get-file)))
          res (rp/file-id-get (:id file-one))]
      (is (= 200 (:status res)))
      (is (= (-> file-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :resource-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "file UPDATE"
    (let [file-one (files/CREATE (db-pop/get-file))
          file-two (db-pop/get-file)]
      (let [res (rp/file-id-patch (:id file-one) file-two)]
        (is (= 200 (:status res)))
        (is (= (into file-two {:id (:id file-one)}) (ut/remove-db-only (files/READ (:id file-one))))))))
  (testing "file DELETE"
    (let [filecontent (ut/get-filecontent)
          file-one (dissoc (db-pop/get-file) :filepath :aspect-ratio)]
      (let [res (rp/file-post file-one filecontent)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))
              db-file (dissoc (ut/remove-db-only (files/READ id)) :aspect-ratio)]
          (is (= (assoc (into file-one {:id id}) :filepath (:filepath db-file))
                 db-file))
          (is (.exists (io/as-file (str (-> env :FILES :media-url) (:filepath db-file)))))
          (let [res (rp/file-id-delete id)]
            (is (= 200 (:status res)))
            (is (= nil (files/READ id)))
            (is (not (.exists (io/as-file (str (-> env :FILES :media-url) (:filepath db-file))))))
            (is (.exists (io/as-file (str (-> env :FILES :media-trash-url) (:filepath db-file)))))))))))


