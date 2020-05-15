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

; - - - - - - - - BASIC INSERT AND SELECT BY ID TESTS - - - - - - - - - - - -

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

; - - - - - - - - - MANY-TO-MANY TABLE TESTS - - - - - - - - - - - - -

(deftest test-account-collection
  ; Create an account and collection, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [account_args {:email "me@gmail.com" :lastlogin "sometime" :name "will" :role 0 :username "conquerer01"}
         collection_args {:name "collection name!" :published false :archived false}
         role 0]
   (let
        ; Add account and collection
        [account_res (db/add-account! t-conn account_args)
         collection_res (db/add-collection! t-conn collection_args)]
            ; Check successful adds
            (is (= 1 (count account_res)))
            (is (= 1 (count collection_res)))
            (is (= (into account_args {:account_id (:account_id (get account_res 0))}) (db/get-account t-conn {:account_id (:account_id (get account_res 0))})))
            (is (= (into collection_args {:collection_id (:collection_id (get collection_res 0))}) (db/get-collection t-conn {:collection_id (:collection_id (get collection_res 0))})))
            ; Connect account and collection
            (is (= 1 (db/add-account-collection! t-conn {:account_id (:account_id (get account_res 0))
                                                         :collection_id (:collection_id (get collection_res 0))
                                                         :role role})))
            ; Check both directions of connectedness
            (is (= [(into collection_args {:collection_id (:collection_id (get collection_res 0))})]
                   (db/get-collections-by-account t-conn {:account_id (:account_id (get account_res 0))})))
            (is (= [(into account_args {:account_id (:account_id (get account_res 0))})]
                   (db/get-accounts-by-collection t-conn {:collection_id (:collection_id (get collection_res 0))})))
            ; Delete connection between account and collection
            (is (= 1 (db/delete-account-collection t-conn {:account_id (:account_id (get account_res 0))
                                                         :collection_id (:collection_id (get collection_res 0))})))
            ; Check connection was deleted from both directions
            (is (= []
                   (db/get-collections-by-account t-conn {:account_id (:account_id (get account_res 0))})))
            (is (= []
                   (db/get-accounts-by-collection t-conn {:collection_id (:collection_id (get collection_res 0))})))
            ))))

; - - - - - - - - - - DELETE TESTS - - - - - - - - - - - - - - - - -

(deftest test-delete-collection
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args {:name "collection name!" :published false :archived false}]
    (let [res
              ; Add collection to db
              (db/add-collection!
              t-conn
              args)]
              ; Check successful add
               (is (= 1 (count res)))
               (is (=
                 (into args {:collection_id (:collection_id (get res 0))}) (db/get-collection t-conn {:collection_id (:collection_id (get res 0))})))
               ; Delete collection from db
               (is (= 1
                 (db/delete-collection t-conn {:collection_id (:collection_id (get res 0))})))
               ; Check successful delete
               (is (= nil (db/get-collection t-conn {:collection_id (:collection_id (get res 0))})))))))

(deftest test-delete-collection-with-course
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [course_args {:department "Russian" :catalog_number "421" :section_number "001"}
          collection_args {:name "collection name!" :published false :archived false}]
    (let [course_res (db/add-course! t-conn course_args)
          collection_res (db/add-collection! t-conn collection_args)]
             ; Check successful course add
             (is (= 1 (count course_res)))
             (is (= 1 (count collection_res)))
             ; Add collection-course connection
             (is (= 1 (db/add-collection-course! t-conn {:collection_id (:collection_id (get collection_res 0))
                                                         :course_id (:course_id (get course_res 0))})))
             ; Delete collection
             (is (= 1 (db/delete-collection t-conn {:collection_id (:collection_id (get collection_res 0))})))
             ; Check that course is still there
             (is (=
                (into course_args {:course_id (:course_id (get course_res 0))})
                (db/get-course t-conn {:course_id (:course_id (get course_res 0))})))
             ; Check that collection no longer in course assoc_collections
             (is (= 0 (count (db/get-collections-by-course t-conn {:course_id (:course_id (get course_res 0))}))))
             ))))
