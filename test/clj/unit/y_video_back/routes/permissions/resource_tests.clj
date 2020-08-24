(ns y-video-back.routes.permissions.resource-tests
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [y-video-back.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [y-video-back.utils.model-generator :as g]
    [y-video-back.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.db.contents :as contents]
    [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
    [y-video-back.db.users-by-collection :as users-by-collection]
    [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.resources :as resources]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.user-courses-assoc :as user-courses-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.words :as words]
    [y-video-back.utils.db-populator :as db-pop]
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
    (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
    (def test-file-one (db-pop/add-file))
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
                                                                                   :resource-id (:id test-cont-one)})))
    (mount.core/start #'y-video-back.handler/app))

  ; (no|with) connection -> (not) connected via many-to-many table

  ; Create resource
  (deftest resource-create
    (testing "student create resource, no connection"
      (let [res (rp/resource-post (:id user-stud-stud)
                                 (g/get-random-resource-without-id))]
        (is (= 401 (:status res)))))
    (testing "instr create resource"
      (let [res (rp/resource-post (:id user-instr-c1)
                                 (g/get-random-resource-without-id))]
        (is (= 401 (:status res)))))
    (testing "lab assistant create resource"
      (let [res (rp/resource-post (:id user-la)
                                 (g/get-random-resource-without-id))]
        (is (= 200 (:status res)))))
    (testing "admin create resource"
      (let [res (rp/resource-post (:id user-admin)
                                 (g/get-random-resource-without-id))]
        (is (= 200 (:status res))))))

  ; Retrieve resource
  (deftest resource-read
    (testing "student reading resource, no connection"
      (let [res (rp/resource-id-get (:id user-stud-na)
                                   (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor reading resource, no connection"
      (let [res (rp/resource-id-get (:id user-instr-na)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "lab assistant reading resource, no connection"
      (let [res (rp/resource-id-get (:id user-la)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "admin reading resource, no connection"
      (let [res (rp/resource-id-get (:id user-admin)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student reading resource, with connection (student)"
      (let [res (rp/resource-id-get (:id user-stud-stud)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student reading resource, with connection (TA)"
      (let [res (rp/resource-id-get (:id user-stud-ta)
                                   (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "instructor reading resource, with connection"
      (let [res (rp/resource-id-get (:id user-instr-c1)
                                   (:id test-cont-one))]
        (is (= 200 (:status res))))))

  ; Update resource
  (deftest resource-update
    (testing "student update resource, no connection"
      (let [res (rp/resource-id-patch (:id user-stud-na)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 401 (:status res)))))
    (testing "instructor update resource, no connection"
      (let [res (rp/resource-id-patch (:id user-instr-na)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 401 (:status res)))))
    (testing "lab assistant update resource, no connection"
      (let [res (rp/resource-id-patch (:id user-la)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 200 (:status res)))))
    (testing "admin update resource, no connection"
      (let [res (rp/resource-id-patch (:id user-admin)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 200 (:status res)))))
    (testing "student update resource, with connection (student)"
      (let [res (rp/resource-id-patch (:id user-stud-stud)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 401 (:status res)))))
    (testing "student update resource, with connection (TA)"
      (let [res (rp/resource-id-patch (:id user-stud-ta)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 200 (:status res)))))
    (testing "instructor update resource, with connection (owner?)"
      (let [res (rp/resource-id-patch (:id user-instr-c1)
                                     (:id test-cont-one)
                                     (g/get-random-resource-without-id))]
        (is (= 200 (:status res))))))

  ; Delete resource
  (deftest resource-delete
    (testing "student delete resource, no connection"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-stud-na)
                                        (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "instructor delete resource, no connection"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-instr-na)
                                        (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "lab assistant delete resource, no connection"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-la)
                                        (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "admin delete resource, no connection"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-admin)
                                        (:id new-resource))]
          (is (= 200 (:status res))))))
    (testing "student delete resource, with connection (student)"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-stud-stud)
                                        (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "student delete resource, with connection (TA)"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-stud-ta)
                                        (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "instructor delete resource, with connection (owner?)"
      (let [new-resource (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id)))]
        (let [res (rp/resource-id-delete (:id user-instr-c1)
                                        (:id new-resource))]
          (is (= 401 (:status res)))))))

  ; Retrieve all files for resource
  (deftest resource-get-all-files
    (testing "student get all files by resource, no connection"
      (let [res (rp/resource-id-files (:id user-stud-na)
                                     (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get all files by resource, no connection"
      (let [res (rp/resource-id-files (:id user-instr-na)
                                     (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "lab assistant get all files by resource, no connection"
      (let [res (rp/resource-id-files (:id user-la)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "admin get all files by resource, no connection"
      (let [res (rp/resource-id-files (:id user-admin)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student get all files by resource, with connection (student)"
      (let [res (rp/resource-id-files (:id user-stud-stud)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student get all files by resource, with connection (TA)"
      (let [res (rp/resource-id-files (:id user-stud-ta)
                                     (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "instructor get all files by resource, with connection (owner?)"
      (let [res (rp/resource-id-files (:id user-instr-c1)
                                     (:id test-cont-one))]
        (is (= 200 (:status res))))))

  ; Add a view to resource

  ; Retrieve all collections for resource
  (deftest resource-get-all-collections
    (testing "student get collections by resource, no connection"
      (let [res (rp/resource-id-collections (:id user-stud-na)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get collections by resource, no connection"
      (let [res (rp/resource-id-collections (:id user-instr-na)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "lab assistant get collections by resource, no connection"
      (let [res (rp/resource-id-collections (:id user-la)
                                           (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "admin get collections by resource, no connection"
      (let [res (rp/resource-id-collections (:id user-admin)
                                           (:id test-cont-one))]
        (is (= 200 (:status res)))))
    (testing "student get collections by resource, with connection (student)"
      (let [res (rp/resource-id-collections (:id user-stud-stud)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "student get collections by resource, with connection (TA)"
      (let [res (rp/resource-id-collections (:id user-stud-ta)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get collections by resource, with connection (owner)"
      (let [res (rp/resource-id-collections (:id user-instr-c1)
                                           (:id test-cont-one))]
        (is (= 401 (:status res)))))))
