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

; - - - - - - - - BASIC INSERT, SELECT BY ID, DELETE TESTS - - - - - - -

(deftest test-account
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args {:email "me@gmail.com" :lastlogin "sometime" :name "will" :role 0 :username "conquerer01"}]
     (let [res
       ; Add account
           (db/add-account! t-conn args)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args {:id (:id (get res 0))}) (db/get-account t-conn {:id (:id (get res 0))})))
       ; Delete account
       (is (= 1 (db/delete-account t-conn {:id (:id (get res 0))})))
       ; Check that account is deleted
       (is (= nil (db/get-account t-conn {:id (:id (get res 0))})))))))

(deftest test-tword
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [account_args {:email "me@gmail.com" :lastlogin "sometime" :name "will" :role 0 :username "conquerer01"}
          tword_args {:tword "a word!" :src_lang "ru" :dest_lang "en"}]
     (let [
       ; Add tword and account
           account_res (db/add-account! t-conn account_args)
           tword_res (db/add-tword! t-conn (into tword_args {:account_id (:id (get account_res 0))}))]
       ; Check successful add and select
       (is (= 1 (count tword_res)))
       (is (= (into tword_args {:id (:id (get tword_res 0))
                                :account_id (:id (get account_res 0))})
              (db/get-tword t-conn {:id (:id (get tword_res 0))})))
       ; Delete tword
       (is (= 1 (db/delete-tword t-conn {:id (:id (get tword_res 0))})))
       ; Check that tword is deleted
       (is (= nil (db/get-tword t-conn {:id (:id (get tword_res 0))})))))))

(deftest test-collection
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args {:name "collection name!" :published false :archived false}]
     (let [res
       ; Add collection
           (db/add-collection! t-conn args)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args {:id (:id (get res 0))}) (db/get-collection t-conn {:id (:id (get res 0))})))
       ; Delete collection
       (is (= 1 (db/delete-collection t-conn {:id (:id (get res 0))})))
       ; Check that collection is deleted
       (is (= nil (db/get-collection t-conn {:id (:id (get res 0))})))))))

(deftest test-course
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args {:department "Russian" :catalog_number "421" :section_number "001"}]
     (let [res
       ; Add course
           (db/add-course! t-conn args)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args {:id (:id (get res 0))}) (db/get-course t-conn {:id (:id (get res 0))})))
       ; Delete course
       (is (= 1 (db/delete-course t-conn {:id (:id (get res 0))})))
       ; Check that course is deleted
       (is (= nil (db/get-course t-conn {:id (:id (get res 0))})))))))

(deftest test-content
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args {:collection_id nil
                :name "content name!" :type "text and stuff" :requester_email "notme@gmail.com"
                :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
                :full_video false :published false :allow_definitions false :allow_notes false
                :allow_captions false :date_validated "don't remember"
                :views 0 :metadata "so meta"}]
     (let [res
       ; Add content
           (db/add-content! t-conn args)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args {:id (:id (get res 0))}) (db/get-content t-conn {:id (:id (get res 0))})))
       ; Delete content
       (is (= 1 (db/delete-content t-conn {:id (:id (get res 0))})))
       ; Check that content is deleted
       (is (= nil (db/get-content t-conn {:id (:id (get res 0))})))))))

(deftest test-file
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args {:filepath "/usr/then/other/stuff" :mime "what even is this?" :metadata "so meta"}]
     (let [res
       ; Add file
           (db/add-file! t-conn args)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args {:id (:id (get res 0))}) (db/get-file t-conn {:id (:id (get res 0))})))
       ; Delete file
       (is (= 1 (db/delete-file t-conn {:id (:id (get res 0))})))
       ; Check that file is deleted
       (is (= nil (db/get-file t-conn {:id (:id (get res 0))})))))))

; - - - - - - - - - MANY-TO-MANY TABLE TESTS - - - - - - - - - - - - -

(deftest test-account-collection-deleting-connection
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
         (is (= (into account_args {:id (:id (get account_res 0))}) (db/get-account t-conn {:id (:id (get account_res 0))})))
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect account and collection
         (is (= 1 (db/add-account-collection! t-conn {:account_id (:id (get account_res 0))
                                                      :collection_id (:id (get collection_res 0))
                                                      :role role})))
            ; Check both directions of connectedness
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-account t-conn {:id (:id (get account_res 0))})))
         (is (= [(into account_args {:id (:id (get account_res 0))})]
                (db/get-accounts-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Delete connection between account and collection
         (is (= 1 (db/delete-account-collection t-conn {:account_id (:id (get account_res 0))
                                                        :collection_id (:id (get collection_res 0))})))
            ; Check connection was deleted from both directions
         (is (= []
                (db/get-collections-by-account t-conn {:id (:id (get account_res 0))})))
         (is (= []
                (db/get-accounts-by-collection t-conn {:id (:id (get collection_res 0))})))))))

(deftest test-account-collection-deleting-each
  ; Create an account and collection, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [account_args_one {:email "me@gmail.com" :lastlogin "sometime" :name "will" :role 0 :username "conquerer01"}
         account_args_two {:email "you@gmail.com" :lastlogin "never" :name "matthew" :role 1 :username "trouble"}
         collection_args {:name "collection name!" :published false :archived false}
         role 0]
    (let
         ; Add account and collection
         [account_res_one (db/add-account! t-conn account_args_one)
          account_res_two (db/add-account! t-conn account_args_two)
          collection_res (db/add-collection! t-conn collection_args)]
          ; Check successful adds
         (is (= 1 (count account_res_one)))
         (is (= 1 (count account_res_two)))
         (is (= 1 (count collection_res)))
         (is (= (into account_args_one {:id (:id (get account_res_one 0))}) (db/get-account t-conn {:id (:id (get account_res_one 0))})))
         (is (= (into account_args_two {:id (:id (get account_res_two 0))}) (db/get-account t-conn {:id (:id (get account_res_two 0))})))
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect account and collection
         (is (= 1 (db/add-account-collection! t-conn {:account_id (:id (get account_res_one 0))
                                                      :collection_id (:id (get collection_res 0))
                                                      :role role})))
            ; Check both directions of connectedness
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-account t-conn {:id (:id (get account_res_one 0))})))
         (is (= [(into account_args_one {:id (:id (get account_res_one 0))})]
                (db/get-accounts-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Delete account
         (is (= 1 (db/delete-account t-conn {:id (:id (get account_res_one 0))})))
            ; Check account and connection were deleted
         (is (= nil
                (db/get-account t-conn {:id (:id (get account_res_one 0))})))
         (is (= []
                (db/get-accounts-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Check collection is still there
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect collection to other account
         (is (= 1 (db/add-account-collection! t-conn {:account_id (:id (get account_res_two 0))
                                                      :collection_id (:id (get collection_res 0))
                                                      :role role})))
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-account t-conn {:id (:id (get account_res_two 0))})))
         (is (= [(into account_args_two {:id (:id (get account_res_two 0))})]
                (db/get-accounts-by-collection t-conn {:id (:id (get collection_res 0))})))
           ; Delete collection
         (is (= 1 (db/delete-collection t-conn {:id (:id (get collection_res 0))})))
           ; Check account is still there
         (is (= (into account_args_two {:id (:id (get account_res_two 0))}) (db/get-account t-conn {:id (:id (get account_res_two 0))})))
         (is (= [] (db/get-collections-by-account t-conn {:id (:id (get account_res_two 0))})))))))

(deftest test-collection-course
  ; Create a collection and course, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [course_args {:department "Russian" :catalog_number "421" :section_number "001"}
         collection_args {:name "collection name!" :published false :archived false}]
    (let
         ; Add course and collection
         [course_res (db/add-course! t-conn course_args)
          collection_res (db/add-collection! t-conn collection_args)]
             ; Check successful adds
         (is (= 1 (count course_res)))
         (is (= 1 (count collection_res)))
         (is (= (into course_args {:id (:id (get course_res 0))}) (db/get-course t-conn {:id (:id (get course_res 0))})))
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect course and collection
         (is (= 1 (db/add-collection-course! t-conn {:course_id (:id (get course_res 0))
                                                     :collection_id (:id (get collection_res 0))})))
            ; Check both directions of connectedness
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-course t-conn {:id (:id (get course_res 0))})))
         (is (= [(into course_args {:id (:id (get course_res 0))})]
                (db/get-courses-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Delete connection between course and collection
         (is (= 1 (db/delete-collection-course t-conn {:course_id (:id (get course_res 0))
                                                       :collection_id (:id (get collection_res 0))})))
            ; Check connection was deleted from both directions
         (is (= []
                (db/get-collections-by-course t-conn {:id (:id (get course_res 0))})))
         (is (= []
                (db/get-courses-by-collection t-conn {:id (:id (get collection_res 0))})))))))


(comment (deftest test-collection-content)
  ; Create a collection and course, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [content_args {:id nil
                       :name "content name!" :type "text and stuff" :requester_email "notme@gmail.com"
                       :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
                       :full_video false :published false :date_validated "don't remember"
                       :metadata "so meta"}
         collection_args {:name "collection name!" :published false :archived false}
         extra_content_args {:allow_definitions false :allow_notes false :allow_captions false}]
    (let
         ; Add content and collection
         [content_res (db/add-content! t-conn content_args)
          collection_res (db/add-collection! t-conn collection_args)]
             ; Check successful adds
         (is (= 1 (count content_res)))
         (is (= 1 (count collection_res)))
         (is (= (into content_args {:id (:id (get content_res 0))}) (db/get-content t-conn {:id (:id (get content_res 0))})))
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect content and collection
         (is (= 1 (db/add-collection-content! t-conn (into {:content_id (:id (get content_res 0))
                                                            :collection_id (:id (get collection_res 0))}
                                                      extra_content_args))))

            ; Check both directions of connectedness
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-content t-conn {:id (:id (get content_res 0))})))
         (is (= [(into content_args {:id (:id (get content_res 0))})]
                (db/get-contents-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Delete connection between content and collection
         (is (= 1 (db/delete-collection-content t-conn {:content_id (:id (get content_res 0))
                                                        :collection_id (:id (get collection_res 0))})))
            ; Check connection was deleted from both directions
         (is (= []
                (db/get-collections-by-content t-conn {:id (:id (get content_res 0))})))
         (is (= []
                (db/get-contents-by-collection t-conn {:id (:id (get collection_res 0))})))))))


(deftest test-content-file
  ; Create a content and file, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [file_args {:filepath "/usr/then/other/stuff" :mime "what even is this?" :metadata "so meta"}
         content_args {:collection_id nil
                       :name "content name!" :type "text and stuff" :requester_email "notme@gmail.com"
                       :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
                       :full_video false :published false :allow_definitions false :allow_notes false :allow_captions false :date_validated "don't remember"
                       :views 0 :metadata "so meta"}]
    (let
         ; Add file and content
         [file_res (db/add-file! t-conn file_args)
          content_res (db/add-content! t-conn content_args)]
             ; Check successful adds
         (is (= 1 (count file_res)))
         (is (= 1 (count content_res)))
         (is (= (into file_args {:id (:id (get file_res 0))}) (db/get-file t-conn {:id (:id (get file_res 0))})))
         (is (= (into content_args {:id (:id (get content_res 0))}) (db/get-content t-conn {:id (:id (get content_res 0))})))
            ; Connect file and content
         (is (= 1 (db/add-content-file! t-conn {:file_id (:id (get file_res 0))
                                                :content_id (:id (get content_res 0))})))
            ; Check both directions of connectedness
         (is (= [(into content_args {:id (:id (get content_res 0))})]
                (db/get-contents-by-file t-conn {:id (:id (get file_res 0))})))
         (is (= [(into file_args {:id (:id (get file_res 0))})]
                (db/get-files-by-content t-conn {:id (:id (get content_res 0))})))
            ; Delete connection between file and content
         (is (= 1 (db/delete-content-file t-conn {:file_id (:id (get file_res 0))
                                                  :content_id (:id (get content_res 0))})))
            ; Check connection was deleted from both directions
         (is (= []
                (db/get-contents-by-file t-conn {:id (:id (get file_res 0))})))
         (is (= []
                (db/get-files-by-content t-conn {:id (:id (get content_res 0))})))))))


; - - - - - - - - - - - UPDATE TESTS - - - - - - - - - - - - - - - - -

(deftest test-account-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:email "me@gmail.com" :lastlogin "sometime" :name "will" :role 0 :username "conquerer01"}
          args_2 {:email "you@outlook.com" :lastlogin "just now" :name "matthew" :role 1 :username "daddy-o"}]
     (let [res
       ; Add account
           (db/add-account! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-account t-conn {:id (:id (get res 0))})))
       ; Update account
       (is (= 1 (db/update-account t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-account t-conn {:id (:id (get res 0))})))))))


(deftest test-tword-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:account_id nil :tword "a word!" :src_lang "ru" :dest_lang "en"}
          args_2 {:account_id nil :tword "another word!" :src_lang "es" :dest_lang "po"}]
     (let [res
       ; Add tword
           (db/add-tword! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-tword t-conn {:id (:id (get res 0))})))
       ; Update tword
       (is (= 1 (db/update-tword t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-tword t-conn {:id (:id (get res 0))})))))))


(deftest test-collection-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:name "collection name!" :published false :archived false}
          args_2 {:name "different name!" :published true :archived true}]
     (let [res
       ; Add collection
           (db/add-collection! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-collection t-conn {:id (:id (get res 0))})))
       ; Update collection
       (is (= 1 (db/update-collection t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-collection t-conn {:id (:id (get res 0))})))))))


(deftest test-course-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:department "Russian" :catalog_number "421" :section_number "001"}
          args_2 {:department "Computer Science" :catalog_number "260" :section_number "003"}]
     (let [res
       ; Add course
           (db/add-course! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-course t-conn {:id (:id (get res 0))})))
       ; Update course
       (is (= 1 (db/update-course t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-course t-conn {:id (:id (get res 0))})))))))


(deftest test-content-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:collection_id nil
                  :name "content name!" :type "text and stuff" :requester_email "notme@gmail.com"
                  :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
                  :full_video false :published false :allow_definitions false :allow_notes false :allow_captions false :date_validated "don't remember"
                  :views 0 :metadata "so meta"}
          args_2 {:collection_id nil
                  :name "different name!" :type "stringy things" :requester_email "notyou@yahoo.com"
                  :thumbnail "just two thumbs" :copyrighted true :physical_copy_exists true
                  :full_video true :published true :allow_definitions true :allow_notes true :allow_captions true :date_validated "not long ago"
                  :views 1 :metadata "like, really really meta"}]
     (let [res
       ; Add content
           (db/add-content! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-content t-conn {:id (:id (get res 0))})))
       ; Update content
       (is (= 1 (db/update-content t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-content t-conn {:id (:id (get res 0))})))))))


(deftest test-file-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:filepath "/usr/then/other/stuff" :mime "what even is this?" :metadata "so meta"}
          args_2 {:filepath "/usr/then/DETOUR!/other/stuff" :mime "still don't know what mime means" :metadata "even more and more meta"}]
     (let [res
       ; Add file
           (db/add-file! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-file t-conn {:id (:id (get res 0))})))
       ; Update file
       (is (= 1 (db/update-file t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-file t-conn {:id (:id (get res 0))})))))))



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
               (into args {:id (:id (get res 0))}) (db/get-collection t-conn {:id (:id (get res 0))})))
               ; Delete collection from db
          (is (= 1
               (db/delete-collection t-conn {:id (:id (get res 0))})))
               ; Check successful delete
          (is (= nil (db/get-collection t-conn {:id (:id (get res 0))})))))))

(comment (deftest test-delete-collection-with-course)
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [course_args {:department "Russian" :catalog_number "421" :section_number "001"}
          collection_args {:name "collection name!" :published false :archived false}]
     (let [course_res (db/add-course! t-conn course_args)
           collection_res (db/add-collection! t-conn collection_args)]
              ; Check successful course add
          (is (= 1 (count course_res)))
          (is (= 1 (count collection_res)))
             ; Add collection-course connection
          (is (= 1 (db/add-collection-course! t-conn {:collection_id (:id (get collection_res 0))
                                                      :course_id (:id (get course_res 0))})))
             ; Delete collection
          (is (= 1 (db/delete-collection t-conn {:id (:id (get collection_res 0))})))
             ; Check that course is still there
          (is (=
               (into course_args {:id (:id (get course_res 0))})
               (db/get-course t-conn {:id (:id (get course_res 0))})))
             ; Check that collection no longer in course assoc_collections
          (is (= 0 (count (db/get-collections-by-course t-conn {:id (:id (get course_res 0))}))))))))
