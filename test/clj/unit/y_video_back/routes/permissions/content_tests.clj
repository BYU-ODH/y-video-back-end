(ns y-video-back.routes.permissions.content-tests
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [y-video-back.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [y-video-back.utils.model-generator :as g]
    [y-video-back.utils.route-proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.db.annotations :as annotations]
    [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
    [y-video-back.db.users-by-collection :as users-by-collection]
    [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.content-files-assoc :as content-files-assoc]
    [y-video-back.db.contents :as contents]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.words :as words]
    [y-video-back.utils.utils :as ut]))

(comment

  (declare ^:dynamic *txn*)

  (use-fixtures
    :once
    (fn [f]
      (mount/start #'y-video-back.config/env
                   #'y-video-back.handler/app
                   #'y-video-back.db.core/*db*)
      (f)))

  (tcore/basic-transaction-fixtures
    (def user-admin (users/CREATE {:email "admin-1@gmail.com"
                                   :last-login "never"
                                   :account-name "Mr. Admin 1"
                                   :account-type 0
                                   :username "a1"}))
    (def user-la (users/CREATE {:email "la-1@gmail.com"
                                :last-login "never"
                                :account-name "Mr. Assistant 1"
                                :account-type 1
                                :username "l1"}))
    (def user-instr-c1 (users/CREATE {:email "instructor-1@gmail.com"
                                      :last-login "never"
                                      :account-name "Mr. Instructor 1"
                                      :account-type 2
                                      :username "i1"}))
    (def user-instr-na (users/CREATE {:email "instructor-2@gmail.com"
                                      :last-login "never"
                                      :account-name "Mr. Instructor 2"
                                      :account-type 2
                                      :username "i2"}))
    (def user-stud-stud (users/CREATE {:email "student-1@gmail.com"
                                       :last-login "never"
                                       :account-name "Mr. Student 1"
                                       :account-type 3
                                       :username "s1"}))
    (def user-stud-ta (users/CREATE {:email "student-2@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Student 2"
                                     :account-type 3
                                     :username "s2"}))
    (def user-stud-na (users/CREATE {:email "student-3@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Student 3"
                                     :account-type 3
                                     :username "s3"}))
    (def test-coll-one (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id))))
    (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
    (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
    (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
    (def test-user-crse-one (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id user-stud-stud)
                                                                            :course-id (:id test-crse-one)
                                                                            :account-role 2}))) ; student in course
    (def test-user-crse-two (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id user-instr-c1)
                                                                            :course-id (:id test-crse-one)
                                                                            :account-role 0})))
    (def test-user-coll-one (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id user-instr-c1)
                                                                                :collection-id (:id test-coll-one)
                                                                                :account-role 0}))) ; owner of collection
    (def test-user-coll-two (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id user-stud-ta)
                                                                                :collection-id (:id test-coll-one)
                                                                                :account-role 1}))) ; TA for collection
    (def test-coll-crse-one (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                                                                  :course-id (:id test-crse-one)})))
    (def test-coll-cont-one (ut/under-to-hyphen (collection-contents-assoc/CREATE {:collection-id (:id test-coll-one)
                                                                                   :content-id (:id test-cont-one)})))
    (def test-cont-file-one (ut/under-to-hyphen (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                                                             :file-id (:id test-file-one)})))
    (mount.core/start #'y-video-back.handler/app))

  ; (no|with) connection -> (not) connected via many-to-many table

  ; Create content
  (deftest content-create
    (testing "student create content, no connection"
      (let [res (rp/content-post (:id user-stud-stud)
                                 (g/get-random-content-without-id))]
        (is (= 401 (:status res)))))
    (testing "instr create content"
      (let [res (rp/content-post (:id user-instr-c1)
                                 (g/get-random-content-without-id))]
        (is (= 401 (:status res)))))
    (testing "lab assistant create content"
      (let [res (rp/content-post (:id user-la)
                                 (g/get-random-content-without-id))]
        (is (= 200 (:status res)))))
    (testing "admin create content"
      (let [res (rp/content-post (:id user-admin)
                                 (g/get-random-content-without-id))]
        (is (= 200 (:status res))))))

  ; Retrieve content
  (deftest content-read
    (testing "student reading content, no connection"
      (let [res (rp/content-id-get (:id user-stud-na)
                                   (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor reading content, no connection"
      (let [res (rp/content-id-get (:id user-instr-na)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "lab assistant reading content, no connection"
      (let [res (rp/content-id-get (:id user-la)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "admin reading content, no connection"
      (let [res (rp/content-id-get (:id user-admin)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student reading content, with connection (student)"
      (let [res (rp/content-id-get (:id user-stud-stud)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student reading content, with connection (TA)"
      (let [res (rp/content-id-get (:id user-stud-ta)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "instructor reading content, with connection"
      (let [res (rp/content-id-get (:id user-instr-c1)
                                   (:id test-cont-one))]
        (is (= 200 (:status res))))))

  ; Update content
  (deftest content-update
    (testing "student update content, no connection"
      (let [res (rp/content-id-patch (:id user-stud-na)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 401 (:status res)))))
    (testing "instructor update content, no connection"
      (let [res (rp/content-id-patch (:id user-instr-na)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 401 (:status res)))))
    (testing "lab assistant update content, no connection"
      (let [res (rp/content-id-patch (:id user-la)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 200 (:status res)))))
    (testing "admin update content, no connection"
      (let [res (rp/content-id-patch (:id user-admin)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 200 (:status res)))))
    (testing "student update content, with connection (student)"
      (let [res (rp/content-id-patch (:id user-stud-stud)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 401 (:status res)))))
    (testing "student update content, with connection (TA)"
      (let [res (rp/content-id-patch (:id user-stud-ta)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 200 (:status res)))))
    (testing "instructor update content, with connection (owner?)"
      (let [res (rp/content-id-patch (:id user-instr-c1)
                                     (:id test-cont-one)
                                     (g/get-random-content-without-id))]
        (is (= 200 (:status res))))))

  ; Delete content
  (deftest content-delete
    (testing "student delete content, no connection"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-stud-na)
                                        (:id new-content))]
          (is (= 401 (:status res))))))
    (testing "instructor delete content, no connection"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-instr-na)
                                        (:id new-content))]
          (is (= 401 (:status res))))))
    (testing "lab assistant delete content, no connection"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-la)
                                        (:id new-content))]
          (is (= 401 (:status res))))))
    (testing "admin delete content, no connection"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-admin)
                                        (:id new-content))]
          (is (= 200 (:status res))))))
    (testing "student delete content, with connection (student)"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-stud-stud)
                                        (:id new-content))]
          (is (= 401 (:status res))))))
    (testing "student delete content, with connection (TA)"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-stud-ta)
                                        (:id new-content))]
          (is (= 401 (:status res))))))
    (testing "instructor delete content, with connection (owner?)"
      (let [new-content (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id)))]
        (let [res (rp/content-id-delete (:id user-instr-c1)
                                        (:id new-content))]
          (is (= 401 (:status res)))))))

  ; Retrieve all files for content
  (deftest content-get-all-files
    (testing "student get all files by content, no connection"
      (let [res (rp/content-id-files (:id user-stud-na)
                                     (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get all files by content, no connection"
      (let [res (rp/content-id-files (:id user-instr-na)
                                     (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "lab assistant get all files by content, no connection"
      (let [res (rp/content-id-files (:id user-la)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "admin get all files by content, no connection"
      (let [res (rp/content-id-files (:id user-admin)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student get all files by content, with connection (student)"
      (let [res (rp/content-id-files (:id user-stud-stud)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student get all files by content, with connection (TA)"
      (let [res (rp/content-id-files (:id user-stud-ta)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "instructor get all files by content, with connection (owner?)"
      (let [res (rp/content-id-files (:id user-instr-c1)
                                     (:id test-cont-one))]
        (is (= 200 (:status res))))))

  ; Add a view to content
  ; Connect content and file
  (deftest content-add-file
    (testing "student add file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-stud-na)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 401 (:status res))))))
    (testing "instructor add file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-instr-na)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 401 (:status res))))))
    (testing "lab assistant add file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-la)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 200 (:status res))))))
    (testing "admin add file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-admin)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 200 (:status res))))))
    (testing "student add file, with connection (student)"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-stud-stud)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 401 (:status res))))))
    (testing "student add file, with connection (TA)"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-stud-ta)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 200 (:status res))))))
    (testing "instructor add file, with connection (owner?)"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (let [res (rp/content-id-add-file (:id user-instr-c1)
                                          (:id test-cont-one)
                                          (:id new-file))]
          (is (= 200 (:status res)))))))

  ; Disconnect content and file
  (deftest content-remove-file
    (testing "student remove file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-stud-na)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 401 (:status res))))))
    (testing "instructor remove file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-instr-na)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 401 (:status res))))))
    (testing "lab assistant remove file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-la)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 200 (:status res))))))
    (testing "admin remove file, no connection"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-admin)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 200 (:status res))))))
    (testing "student remove file, with connection (student)"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-stud-stud)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 401 (:status res))))))
    (testing "student remove file, with connection (TA)"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-stud-ta)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 200 (:status res))))))
    (testing "instructor remove file, with connection (owner)"
      (let [new-file (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id)))]
        (content-files-assoc/CREATE {:content-id (:id test-cont-one)
                                     :file-id (:id new-file)})
        (let [res (rp/content-id-remove-file (:id user-instr-c1)
                                             (:id test-cont-one)
                                             (:id new-file))]
          (is (= 200 (:status res)))))))

  ; Retrieve all collections for content
  (deftest content-get-all-collections
    (testing "student get collections by content, no connection"
      (let [res (rp/content-id-collections (:id user-stud-na)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get collections by content, no connection"
      (let [res (rp/content-id-collections (:id user-instr-na)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "lab assistant get collections by content, no connection"
      (let [res (rp/content-id-collections (:id user-la)
                                           (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "admin get collections by content, no connection"
      (let [res (rp/content-id-collections (:id user-admin)
                                           (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student get collections by content, with connection (student)"
      (let [res (rp/content-id-collections (:id user-stud-stud)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "student get collections by content, with connection (TA)"
      (let [res (rp/content-id-collections (:id user-stud-ta)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get collections by content, with connection (owner)"
      (let [res (rp/content-id-collections (:id user-instr-c1)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))))
