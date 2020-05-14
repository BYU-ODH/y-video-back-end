(ns y-video-postgres-swagger.test.db.core
  (:require
   [y-video-postgres-swagger.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [y-video-postgres-swagger.config :refer [env]]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'y-video-postgres-swagger.config/env
     #'y-video-postgres-swagger.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-account
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [args {:email "me@gmail.com" :lastlogin "sometime" :name "will" :role 0 :username "conquerer01"}]
   (let [res (db/add-account!
             t-conn
             args)]
              (is (= 1 (count res)))
              (is (= (into args {:account_id (:account_id (get res 0))}) (db/get-account t-conn {:account_id (:account_id (get res 0))})))))))

(deftest test-tword
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [args {:account_id nil :tword "a word!" :src_lang "ru" :dest_lang "en"}]
   (let [res (db/add-tword!
             t-conn
             args)]
              (is (= 1 (count res)))
              (is (= (into args {:tword_id (:tword_id (get res 0))}) (db/get-tword t-conn {:tword_id (:tword_id (get res 0))})))))))

(deftest test-collection
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [args {:name "collection name!" :published false :archived false}]
   (let [res (db/add-collection!
             t-conn
             args)]
              (is (= 1 (count res)))
              (is (=
                (into args {:collection_id (:collection_id (get res 0))}) (db/get-collection t-conn {:collection_id (:collection_id (get res 0))})))))))

(deftest test-course
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [args {:department "Russian" :catalog_number "421" :section_number "001"}]
   (let [res (db/add-course!
             t-conn
             args)]
              (is (= 1 (count res)))
              (is (=
                (into args {:course_id (:course_id (get res 0))})
                (db/get-course t-conn {:course_id (:course_id (get res 0))})))))))

(deftest test-content
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [args {:collection_id nil
               :name "content name!" :type "text and stuff" :requester_email "notme@gmail.com"
               :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
               :full_video false :published false :date_validated "don't remember"
               :metadata "so meta"}]
   (let [res (db/add-content!
             t-conn
             args)]
              (is (= 1 (count res)))
              (is (= (into args {:content_id (:content_id (get res 0))}) (db/get-content t-conn {:content_id (:content_id (get res 0))})))))))

(deftest test-file
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [args {:filepath "/usr/then/other/stuff" :mime "what even is this?" :metadata "so meta"}]
   (let [res (db/add-file!
             t-conn
             args)]
              (is (= 1 (count res)))
              (is (= (into args {:file_id (:file_id (get res 0))}) (db/get-file t-conn {:file_id (:file_id (get res 0))})))))))
