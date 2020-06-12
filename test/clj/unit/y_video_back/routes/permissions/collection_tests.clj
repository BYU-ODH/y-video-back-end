(ns y-video-back.routes.permissions.collection-tests
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [y-video-back.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [y-video-back.utils.model_generator :as g]
    [y-video-back.utils.route_proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.db.annotations :as annotations]
    [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
    [y-video-back.db.users-by-collection :as users-by-collection]
    [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.content-files-assoc :as content_files_assoc]
    [y-video-back.db.contents :as contents]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user_collections_assoc]
    [y-video-back.db.user-courses-assoc :as user_courses_assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.words :as words]
    [y-video-back.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def user-admin (users/CREATE {:email "admin_1@gmail.com"
                                 :last-login "never"
                                 :account-name "Mr. Admin 1"
                                 :account-type 0
                                 :username "a1"}))
  (def user-la (users/CREATE {:email "la_1@gmail.com"
                              :last-login "never"
                              :account-name "Mr. Assistant 1"
                              :account-type 1
                              :username "l1"}))
  (def user-instr-c1 (users/CREATE {:email "instructor_1@gmail.com"
                                    :last-login "never"
                                    :account-name "Mr. Instructor 1"
                                    :account-type 2
                                    :username "i1"}))
  (def user-instr-na (users/CREATE {:email "instructor_2@gmail.com"
                                    :last-login "never"
                                    :account-name "Mr. Instructor 2"
                                    :account-type 2
                                    :username "i2"}))
  (def user-stud-stud (users/CREATE {:email "student_1@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Student 1"
                                     :account-type 3
                                     :username "s1"}))
  (def user-stud-ta (users/CREATE {:email "student_2@gmail.com"
                                   :last-login "never"
                                   :account-name "Mr. Student 2"
                                   :account-type 3
                                   :username "s2"}))
  (def user-stud-na (users/CREATE {:email "student_3@gmail.com"
                                   :last-login "never"
                                   :account-name "Mr. Student 3"
                                   :account-type 3
                                   :username "s3"}))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-user-crse-one (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id user-stud-stud)
                                                                          :course_id (:id test-crse-one)
                                                                          :account_role 2}))) ; student in course
  (def test-user-crse-two (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id user-instr-c1)
                                                                          :course_id (:id test-crse-one)
                                                                          :account_role 0})))
  (def test-user-coll-one (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id user-instr-c1)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 0}))) ; owner of collection
  (def test-user-coll-two (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id user-stud-ta)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 1}))) ; TA for collection
  (def test-coll-crse-one (ut/under-to-hyphen (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                                                                :course_id (:id test-crse-one)})))
  (def test-coll-cont-one (ut/under-to-hyphen (collection_contents_assoc/CREATE {:collection_id (:id test-coll-one)
                                                                                 :content_id (:id test-cont-one)})))
  (mount.core/start #'y-video-back.handler/app))

; (no|with) connection -> (not) connected via many-to-many table

; Create collection
(deftest collection-create
  (testing "student create collection, no connection"
    (let [res (rp/collection-post (:id user-stud-stud)
                                  (g/get_random_collection_without_id)
                                  (:id user-stud-stud))]
      (is (= 401 (:status res)))))
  (testing "instr create collection"
    (let [res (rp/collection-post (:id user-instr-c1)
                                  (g/get_random_collection_without_id)
                                  (:id user-instr-c1))]
      (is (= 200 (:status res)))))
  (testing "lab assistant create collection"
    (let [res (rp/collection-post (:id user-la)
                                  (g/get_random_collection_without_id)
                                  (:id user-instr-c1))]
      (is (= 200 (:status res)))))
  (testing "admin create collection"
    (let [res (rp/collection-post (:id user-admin)
                                  (g/get_random_collection_without_id)
                                  (:id user-instr-c1))]
      (is (= 200 (:status res))))))

; Retrieve collection
(deftest collection-read
  (testing "student reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-stud-na)
                                    (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-instr-na)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-la)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "admin reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-admin)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student reading collection, with connection (student)"
    (let [res (rp/collection-id-get (:id user-stud-stud)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student reading collection, with connection (TA)"
    (let [res (rp/collection-id-get (:id user-stud-ta)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor reading collection, with connection"
    (let [res (rp/collection-id-get (:id user-instr-c1)
                                    (:id test-coll-one))]
      (is (= 200 (:status res))))))

; Update collection
(deftest collection-update
  (testing "student update collection, no connection"
    (let [res (rp/collection-id-patch (:id user-stud-na)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 401 (:status res)))))
  (testing "instructor update collection, no connection"
    (let [res (rp/collection-id-patch (:id user-instr-na)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 401 (:status res)))))
  (testing "lab assistant update collection, no connection"
    (let [res (rp/collection-id-patch (:id user-la)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 200 (:status res)))))
  (testing "admin update collection, no connection"
    (let [res (rp/collection-id-patch (:id user-admin)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 200 (:status res)))))
  (testing "student update collection, with connection (student)"
    (let [res (rp/collection-id-patch (:id user-stud-stud)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 401 (:status res)))))
  (testing "student update collection, with connection (TA)"
    (let [res (rp/collection-id-patch (:id user-stud-ta)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 200 (:status res)))))
  (testing "instructor update collection, with connection (owner?)"
    (let [res (rp/collection-id-patch (:id user-instr-c1)
                                      (:id test-coll-one)
                                      (g/get_random_collection_without_id))]
      (is (= 200 (:status res))))))

; Delete collection
(deftest collection-delete
  (testing "student delete collection, no connection"
    (let [res (rp/collection-id-delete (:id user-stud-na)
                                       (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor delete collection, no connection"
    (let [res (rp/collection-id-delete (:id user-instr-na)
                                       (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "lab assistant delete collection, no connection"
    (let [res (rp/collection-id-delete (:id user-la)
                                       (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "admin delete collection, no connection"
    (let [res (rp/collection-id-delete (:id user-admin)
                                       (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student delete collection, with connection (student)"
    (let [res (rp/collection-id-delete (:id user-stud-stud)
                                       (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "student delete collection, with connection (TA)"
    (let [res (rp/collection-id-delete (:id user-stud-ta)
                                       (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor delete collection, with connection (owner?)"
    (let [res (rp/collection-id-delete (:id user-instr-c1)
                                       (:id test-coll-one))]
      (is (= 401 (:status res))))))

; Connect user and collection
(deftest collection-add-user
  (testing "student add user, no connection"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-stud-na)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 401 (:status res))))))
  (testing "instructor add user, no connection"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-instr-na)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 401 (:status res))))))
  (testing "lab assistant add user, no connection"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-la)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 200 (:status res))))))
  (testing "admin add user, no connection"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-admin)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 200 (:status res))))))
  (testing "student add user, with connection (student)"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-stud-stud)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 401 (:status res))))))
  (testing "student add user, with connection (TA)"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-stud-ta)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 401 (:status res))))))
  (testing "instructor add user, with connection"
    (let [test-add-user (users/CREATE (g/get_random_user_without_id))]
      (let [res (rp/collection-id-add-user (:id user-instr-c1)
                                           (:id test-coll-one)
                                           (:id test-add-user)
                                           0)]
        (is (= 200 (:status res)))))))

; Disconnects user and collection
(deftest collection-remove-user
  (testing "student remove user, no connection"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-stud-na)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 401 (:status res))))))
  (testing "instructor remove user, no connection"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-instr-na)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 401 (:status res))))))
  (testing "lab assistant remove user, no connection"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-la)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 200 (:status res))))))
  (testing "admin remove user, no connection"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-admin)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 200 (:status res))))))
  (testing "student remove user, with connection (student)"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-stud-stud)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 401 (:status res))))))
  (testing "student remove user, with connection (TA)"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-stud-ta)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 401 (:status res))))))
  (testing "instructor remove user, with connection"
    (let [test-remove-user (users/CREATE (g/get_random_user_without_id))]
      (user_collections_assoc/CREATE {:user_id (:id test-remove-user)
                                      :collection_id (:id test-coll-one)
                                      :account_role 0})
      (let [res (rp/collection-id-remove-user (:id user-instr-c1)
                                           (:id test-coll-one)
                                           (:id test-remove-user))]
        (is (= 200 (:status res)))))))

; Connect content and collection
(deftest collection-add-content
  (testing "student add content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-stud-na)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "instructor add content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-instr-na)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "lab assistant add content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-la)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 200 (:status res))))))
  (testing "admin add content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-admin)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 200 (:status res))))))
  (testing "student add content, with connection (student)"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-stud-stud)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "student add content, with connection (TA)"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-stud-ta)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "instructor add content, with connection (owner?)"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (let [res (rp/collection-id-add-content (:id user-instr-c1)
                                              (:id test-coll-one)
                                              (:id new-content))]
        (is (= 200 (:status res)))))))

; Disconnects content and collection
(deftest collection-remove-content
  (testing "student remove content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-stud-na)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "instructor remove content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-instr-na)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "lab assistant remove content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-la)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 200 (:status res))))))
  (testing "admin remove content, no connection"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-admin)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 200 (:status res))))))
  (testing "student remove content, with connection (student)"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-stud-stud)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "student remove content, with connection (TA)"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-stud-ta)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 401 (:status res))))))
  (testing "instructor remove content, with connection (owner?)"
    (let [new-content (contents/CREATE (g/get_random_content_without_id))]
      (collection_contents_assoc/CREATE {:content_id (:id new-content)
                                         :collection_id (:id test-coll-one)})
      (let [res (rp/collection-id-remove-content (:id user-instr-c1)
                                                 (:id test-coll-one)
                                                 (:id new-content))]
        (is (= 200 (:status res)))))))

; Retrieve all contents for collection
(deftest collection-get-all-contents
  (testing "student get contents, no connection"
    (let [res (rp/collection-id-contents (:id user-stud-na)
                                         (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor get contents, no connection"
    (let [res (rp/collection-id-contents (:id user-instr-na)
                                         (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "lab assistant get contents, no connection"
    (let [res (rp/collection-id-contents (:id user-la)
                                         (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "admin get contents, no connection"
    (let [res (rp/collection-id-contents (:id user-admin)
                                         (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student get contents, with connection (student)"
    (let [res (rp/collection-id-contents (:id user-stud-stud)
                                         (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student get contents, with connection (TA)"
    (let [res (rp/collection-id-contents (:id user-stud-ta)
                                         (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor get contents, with connection (owner)"
    (let [res (rp/collection-id-contents (:id user-instr-c1)
                                         (:id test-coll-one))]
      (is (= 200 (:status res))))))

; Retrieve all courses for collection
(deftest collection-get-all-courses
  (testing "student get courses, no connection"
    (let [res (rp/collection-id-courses (:id user-stud-na)
                                        (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor get courses, no connection"
    (let [res (rp/collection-id-courses (:id user-instr-na)
                                        (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "lab assistant get courses, no connection"
    (let [res (rp/collection-id-courses (:id user-la)
                                        (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "admin get courses, no connection"
    (let [res (rp/collection-id-courses (:id user-admin)
                                        (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student get courses, with connection (student)"
    (let [res (rp/collection-id-courses (:id user-stud-stud)
                                        (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "student get courses, with connection (TA)"
    (let [res (rp/collection-id-courses (:id user-stud-ta)
                                        (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor get courses, with connection (owner)"
    (let [res (rp/collection-id-courses (:id user-instr-c1)
                                        (:id test-coll-one))]
      (is (= 401 (:status res))))))

; Retrieve all users for collection
(deftest collection-get-all-users
  (testing "student get users, no connection"
    (let [res (rp/collection-id-users (:id user-stud-na)
                                      (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor get users, no connection"
    (let [res (rp/collection-id-users (:id user-instr-na)
                                      (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "lab assistant get users, no connection"
    (let [res (rp/collection-id-users (:id user-la)
                                      (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "admin get users, no connection"
    (let [res (rp/collection-id-users (:id user-admin)
                                      (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student get users, with connection (student)"
    (let [res (rp/collection-id-users (:id user-stud-stud)
                                      (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "student get users, with connection (TA)"
    (let [res (rp/collection-id-users (:id user-stud-ta)
                                      (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor get users, with connection (owner)"
    (let [res (rp/collection-id-users (:id user-instr-c1)
                                      (:id test-coll-one))]
      (is (= 401 (:status res))))))

; Connect course and collection
(deftest collection-add-course
  (testing "student add course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-stud-na)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "instructor add course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-instr-na)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "lab assistant add course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-la)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 200 (:status res))))))
  (testing "admin add course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-admin)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 200 (:status res))))))
  (testing "student add course, with connection (student)"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-stud-stud)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "student add course, with connection (TA)"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-stud-ta)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "instructor add course, with connection (owner)"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (let [res (rp/collection-id-add-course (:id user-instr-c1)
                                             (:id test-coll-one)
                                             (:id new-course))]
        (is (= 200 (:status res)))))))

; Disconnects course and collection
(deftest collection-remove-course
  (testing "student remove course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-stud-na)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "instructor remove course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-instr-na)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "lab assistant remove course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-la)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 200 (:status res))))))
  (testing "admin remove course, no connection"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-admin)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 200 (:status res))))))
  (testing "student remove course, with connection (student)"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-stud-stud)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "student remove course, with connection (TA)"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-stud-ta)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 401 (:status res))))))
  (testing "instructor remove course, with connection (owner)"
    (let [new-course (courses/CREATE (g/get_random_course_without_id))]
      (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                        :course_id (:id new-course)})
      (let [res (rp/collection-id-remove-course (:id user-instr-c1)
                                                (:id test-coll-one)
                                                (:id new-course))]
        (is (= 200 (:status res)))))))
