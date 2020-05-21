(ns y-video-postgres-swagger.test.db.core
  (:require
   [y-video-postgres-swagger.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [y-video-postgres-swagger.config :refer [env]]
   [y-video-postgres-swagger.test.test_model_generator :as model-generator]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'y-video-postgres-swagger.config/env
     #'y-video-postgres-swagger.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(defn get-id
  "Retrieves ID from first result in res"
  [res]
  (:id (get res 0)))

; - - - - - - - - BASIC INSERT, SELECT BY ID, DELETE TESTS - - - - - - -

(deftest test-user
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args (model-generator/get_random_user_without_id)]
     (let [res
       ; Add user
           (db/add-user! t-conn args)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args {:id (get-id res)}) (db/get-user t-conn {:id (get-id res)})))
       ; Delete user
       (is (= 1 (db/delete-user t-conn {:id (get-id res)})))
       ; Check that user is deleted
       (is (= nil (db/get-user t-conn {:id (get-id res)})))))))

(deftest test-word
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [user_args (model-generator/get_random_user_without_id)
          word_args (model-generator/get_random_word_without_id_or_user_id)]
     (let [
       ; Add word and user
           user_res (db/add-user! t-conn user_args)
           word_res (db/add-word! t-conn (into word_args {:user_id (:id (get user_res 0))}))]
       ; Check successful add and select
       (is (= 1 (count word_res)))
       (is (= (into word_args {:id (:id (get word_res 0))
                                :user_id (:id (get user_res 0))})
              (db/get-word t-conn {:id (:id (get word_res 0))})))
       ; Delete word
       (is (= 1 (db/delete-word t-conn {:id (:id (get word_res 0))})))
       ; Check that word is deleted
       (is (= nil (db/get-word t-conn {:id (:id (get word_res 0))})))))))

(deftest test-collection
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args (model-generator/get_random_collection_without_id)]
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
                :content_name "content name!" :content_type "text and stuff" :requester_email "notme@gmail.com"
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

(deftest test-user-collection-deleting-connection
  ; Create an user and collection, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [user_args {:email "me@gmail.com" :last_login "sometime" :account_name "will" :account_role 0 :username "conquerer01"}
         collection_args {:collection_name "collection name!" :published false :archived false}
         account_role 0]
    (let
         ; Add user and collection
         [user_res (db/add-user! t-conn user_args)
          collection_res (db/add-collection! t-conn collection_args)]
          ; Check successful adds
         (is (= 1 (count user_res)))
         (is (= 1 (count collection_res)))
         (is (= (into user_args {:id (:id (get user_res 0))}) (db/get-user t-conn {:id (:id (get user_res 0))})))
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect user and collection
         (is (= 1 (db/add-user-collection! t-conn {:user_id (:id (get user_res 0))
                                                      :collection_id (:id (get collection_res 0))
                                                      :account_role account_role})))
            ; Check both directions of connectedness
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-user t-conn {:id (:id (get user_res 0))})))
         (is (= [(into user_args {:id (:id (get user_res 0))})]
                (db/get-users-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Delete connection between user and collection
         (is (= 1 (db/delete-user-collection t-conn {:user_id (:id (get user_res 0))
                                                        :collection_id (:id (get collection_res 0))})))
            ; Check connection was deleted from both directions
         (is (= []
                (db/get-collections-by-user t-conn {:id (:id (get user_res 0))})))
         (is (= []
                (db/get-users-by-collection t-conn {:id (:id (get collection_res 0))})))))))

(deftest test-user-collection-deleting-each
  ; Create an user and collection, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [user_args_one {:email "me@gmail.com" :last_login "sometime" :account_name "will" :account_role 0 :username "conquerer01"}
         user_args_two {:email "you@gmail.com" :last_login "never" :account_name "matthew" :account_role 1 :username "trouble"}
         collection_args {:collection_name "collection name!" :published false :archived false}
         account_role 0]
    (let
         ; Add user and collection
         [user_res_one (db/add-user! t-conn user_args_one)
          user_res_two (db/add-user! t-conn user_args_two)
          collection_res (db/add-collection! t-conn collection_args)]
          ; Check successful adds
         (is (= 1 (count user_res_one)))
         (is (= 1 (count user_res_two)))
         (is (= 1 (count collection_res)))
         (is (= (into user_args_one {:id (:id (get user_res_one 0))}) (db/get-user t-conn {:id (:id (get user_res_one 0))})))
         (is (= (into user_args_two {:id (:id (get user_res_two 0))}) (db/get-user t-conn {:id (:id (get user_res_two 0))})))
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect user and collection
         (is (= 1 (db/add-user-collection! t-conn {:user_id (:id (get user_res_one 0))
                                                      :collection_id (:id (get collection_res 0))
                                                      :account_role account_role})))
            ; Check both directions of connectedness
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-user t-conn {:id (:id (get user_res_one 0))})))
         (is (= [(into user_args_one {:id (:id (get user_res_one 0))})]
                (db/get-users-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Delete user
         (is (= 1 (db/delete-user t-conn {:id (:id (get user_res_one 0))})))
            ; Check user and connection were deleted
         (is (= nil
                (db/get-user t-conn {:id (:id (get user_res_one 0))})))
         (is (= []
                (db/get-users-by-collection t-conn {:id (:id (get collection_res 0))})))
            ; Check collection is still there
         (is (= (into collection_args {:id (:id (get collection_res 0))}) (db/get-collection t-conn {:id (:id (get collection_res 0))})))
            ; Connect collection to other user
         (is (= 1 (db/add-user-collection! t-conn {:user_id (:id (get user_res_two 0))
                                                      :collection_id (:id (get collection_res 0))
                                                      :account_role account_role})))
         (is (= [(into collection_args {:id (:id (get collection_res 0))})]
                (db/get-collections-by-user t-conn {:id (:id (get user_res_two 0))})))
         (is (= [(into user_args_two {:id (:id (get user_res_two 0))})]
                (db/get-users-by-collection t-conn {:id (:id (get collection_res 0))})))
           ; Delete collection
         (is (= 1 (db/delete-collection t-conn {:id (:id (get collection_res 0))})))
           ; Check user is still there
         (is (= (into user_args_two {:id (:id (get user_res_two 0))}) (db/get-user t-conn {:id (:id (get user_res_two 0))})))
         (is (= [] (db/get-collections-by-user t-conn {:id (:id (get user_res_two 0))})))))))

(deftest test-collection-course
  ; Create a collection and course, connect them, test connection, delete connection, test connection again
 (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
   (let [course_args {:department "Russian" :catalog_number "421" :section_number "001"}
         collection_args {:collection_name "collection name!" :published false :archived false}]
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
                       :content_name "content name!" :content_type "text and stuff" :requester_email "notme@gmail.com"
                       :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
                       :full_video false :published false :date_validated "don't remember"
                       :metadata "so meta"}
         collection_args {:collection_name "collection name!" :published false :archived false}
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
                       :content_name "content name!" :content_type "text and stuff" :requester_email "notme@gmail.com"
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

(deftest test-user-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:email "me@gmail.com" :last_login "sometime" :account_name "will" :account_role 0 :username "conquerer01"}
          args_2 {:email "you@outlook.com" :last_login "just now" :account_name "matthew" :account_role 1 :username "daddy-o"}]
     (let [res
       ; Add user
           (db/add-user! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-user t-conn {:id (:id (get res 0))})))
       ; Update user
       (is (= 1 (db/update-user t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-user t-conn {:id (:id (get res 0))})))))))


(deftest test-word-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:user_id nil :word "a word!" :src_lang "ru" :dest_lang "en"}
          args_2 {:user_id nil :word "another word!" :src_lang "es" :dest_lang "po"}]
     (let [res
       ; Add word
           (db/add-word! t-conn args_1)]
       ; Check successful add and select
       (is (= 1 (count res)))
       (is (= (into args_1 {:id (:id (get res 0))}) (db/get-word t-conn {:id (:id (get res 0))})))
       ; Update word
       (is (= 1 (db/update-word t-conn (into args_2 {:id (:id (get res 0))}))))
       ; Check successful update
       (is (= (into args_2 {:id (:id (get res 0))}) (db/get-word t-conn {:id (:id (get res 0))})))))))


(deftest test-collection-update
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (let [args_1 {:collection_name "collection name!" :published false :archived false}
          args_2 {:collection_name "different name!" :published true :archived true}]
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
                  :content_name "content name!" :content_type "text and stuff" :requester_email "notme@gmail.com"
                  :thumbnail "all thumbs" :copyrighted false :physical_copy_exists false
                  :full_video false :published false :allow_definitions false :allow_notes false :allow_captions false :date_validated "don't remember"
                  :views 0 :metadata "so meta"}
          args_2 {:collection_id nil
                  :content_name "different name!" :content_type "stringy things" :requester_email "notyou@yahoo.com"
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
    (let [args {:collection_name "collection name!" :published false :archived false}]
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
          collection_args {:collection_name "collection name!" :published false :archived false}]
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
