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
    (let [res (db/add-account!
              t-conn
              {:email "test@gmail.com"
               :lastlogin "never"
               :name "Buster"
               :role 0
               :username "b1"})]
               (is (= 1 (count res)))
               (is (= {
                 :account_id (:account_id (get res 0))
                 :email "test@gmail.com"
                 :lastlogin "never"
                 :name "Buster"
                 :role 0
                 :username "b1"
                 } (db/get-account t-conn {:account_id (:account_id (get res 0))}))))))

(deftest test-tword
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [account_id nil tword "a word!" src_lang "ru" dest_lang "en"]
   (let [res (db/add-tword!
             t-conn
             {
               :account_id account_id
               :tword tword
               :src_lang src_lang
               :dest_lang dest_lang
               })]
               (print res)
              (is (= 1 (count res)))
              (is (= {
                :tword_id (:tword_id (get res 0))
                :account_id account_id
                :tword tword
                :src_lang src_lang
                :dest_lang dest_lang
                } (db/get-tword t-conn {:tword_id (:tword_id (get res 0))})))))))

(deftest test-collection
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [name "collection name!" published false archived false]
   (let [res (db/add-collection!
             t-conn
             {
               :name name
               :published published
               :archived archived
               })]
              (is (= 1 (count res)))
              (is (= {
                :collection_id (:collection_id (get res 0))
                :name name
                :published published
                :archived archived
                } (db/get-collection t-conn {:collection_id (:collection_id (get res 0))})))))))

(deftest test-course
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [department "Russian" catalog_number "421" section_number "001"]
   (let [res (db/add-course!
             t-conn
             {
               :department department
               :catalog_number catalog_number
               :section_number section_number
               })]
              (is (= 1 (count res)))
              (is (= {
                :course_id (:course_id (get res 0))
                :department department
                :catalog_number catalog_number
                :section_number section_number
                } (db/get-course t-conn {:course_id (:course_id (get res 0))})))))))
