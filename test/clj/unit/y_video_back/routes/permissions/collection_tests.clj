(ns y-video-back.routes.permissions.collection-tests
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
    [y-video-back.db.resource-files-assoc :as resource-files-assoc]
    [y-video-back.db.resources :as resources]
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
    (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
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
    (def test-coll-cont-one (ut/under-to-hyphen (collection-contents-assoc/CREATE {:collection-id (:id test-coll-one)}
                                                                                   :resource-id (:id test-cont-one))))
    (mount.core/start #'y-video-back.handler/app))

  ; (no|with) connection -> (not) connected via many-to-many table

  ; Create collection
  (deftest collection-create
    (testing "student create collection, no connection"
      (let [res (rp/collection-post (:id user-stud-stud)
                                    (g/get-random-collection-without-id)
                                    (:id user-stud-stud))]
        (is (= 401 (:status res)))))
    (testing "instr create collection"
      (let [res (rp/collection-post (:id user-instr-c1)
                                    (g/get-random-collection-without-id)
                                    (:id user-instr-c1))]
        (is (= 200 (:status res)))))
    (testing "lab assistant create collection"
      (let [res (rp/collection-post (:id user-la)
                                    (g/get-random-collection-without-id)
                                    (:id user-instr-c1))]
        (is (= 200 (:status res)))))
    (testing "admin create collection"
      (let [res (rp/collection-post (:id user-admin)
                                    (g/get-random-collection-without-id)
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
                                        (g/get-random-collection-without-id))]
        (is (= 401 (:status res)))))
    (testing "instructor update collection, no connection"
      (let [res (rp/collection-id-patch (:id user-instr-na)
                                        (:id test-coll-one)
                                        (g/get-random-collection-without-id))]
        (is (= 401 (:status res)))))
    (testing "lab assistant update collection, no connection"
      (let [res (rp/collection-id-patch (:id user-la)
                                        (:id test-coll-one)
                                        (g/get-random-collection-without-id))]
        (is (= 200 (:status res)))))
    (testing "admin update collection, no connection"
      (let [res (rp/collection-id-patch (:id user-admin)
                                        (:id test-coll-one)
                                        (g/get-random-collection-without-id))]
        (is (= 200 (:status res)))))
    (testing "student update collection, with connection (student)"
      (let [res (rp/collection-id-patch (:id user-stud-stud)
                                        (:id test-coll-one)
                                        (g/get-random-collection-without-id))]
        (is (= 401 (:status res)))))
    (testing "student update collection, with connection (TA)"
      (let [res (rp/collection-id-patch (:id user-stud-ta)
                                        (:id test-coll-one)
                                        (g/get-random-collection-without-id))]
        (is (= 200 (:status res)))))
    (testing "instructor update collection, with connection (owner?)"
      (let [res (rp/collection-id-patch (:id user-instr-c1)
                                        (:id test-coll-one)
                                        (g/get-random-collection-without-id))]
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
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-stud-na)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 401 (:status res))))))
    (testing "instructor add user, no connection"
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-instr-na)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 401 (:status res))))))
    (testing "lab assistant add user, no connection"
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-la)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 200 (:status res))))))
    (testing "admin add user, no connection"
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-admin)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 200 (:status res))))))
    (testing "student add user, with connection (student)"
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-stud-stud)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 401 (:status res))))))
    (testing "student add user, with connection (TA)"
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-stud-ta)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 401 (:status res))))))
    (testing "instructor add user, with connection"
      (let [test-add-user (users/CREATE (g/get-random-user-without-id))]
        (let [res (rp/collection-id-add-user (:id user-instr-c1)
                                             (:id test-coll-one)
                                             (:id test-add-user)
                                             0)]
          (is (= 200 (:status res)))))))

  ; Disconnects user and collection
  (deftest collection-remove-user
    (testing "student remove user, no connection"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-stud-na)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 401 (:status res))))))
    (testing "instructor remove user, no connection"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-instr-na)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 401 (:status res))))))
    (testing "lab assistant remove user, no connection"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-la)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 200 (:status res))))))
    (testing "admin remove user, no connection"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-admin)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 200 (:status res))))))
    (testing "student remove user, with connection (student)"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-stud-stud)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 401 (:status res))))))
    (testing "student remove user, with connection (TA)"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-stud-ta)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 401 (:status res))))))
    (testing "instructor remove user, with connection"
      (let [test-remove-user (users/CREATE (g/get-random-user-without-id))]
        (user-collections-assoc/CREATE {:user-id (:id test-remove-user)
                                        :collection-id (:id test-coll-one)
                                        :account-role 0})
        (let [res (rp/collection-id-remove-user (:id user-instr-c1)
                                             (:id test-coll-one)
                                             (:id test-remove-user))]
          (is (= 200 (:status res)))))))

  ; Connect resource and collection
  (deftest collection-add-resource
    (testing "student add resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-stud-na)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "instructor add resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-instr-na)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "lab assistant add resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-la)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 200 (:status res))))))
    (testing "admin add resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-admin)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 200 (:status res))))))
    (testing "student add resource, with connection (student)"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-stud-stud)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "student add resource, with connection (TA)"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-stud-ta)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "instructor add resource, with connection (owner?)"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (let [res (rp/collection-id-add-resource (:id user-instr-c1)
                                                (:id test-coll-one)
                                                (:id new-resource))]
          (is (= 200 (:status res)))))))

  ; Disconnects resource and collection
  (deftest collection-remove-resource
    (testing "student remove resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-stud-na)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "instructor remove resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-instr-na)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "lab assistant remove resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-la)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 200 (:status res))))))
    (testing "admin remove resource, no connection"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-admin)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 200 (:status res))))))
    (testing "student remove resource, with connection (student)"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-stud-stud)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "student remove resource, with connection (TA)"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-stud-ta)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 401 (:status res))))))
    (testing "instructor remove resource, with connection (owner?)"
      (let [new-resource (resources/CREATE (g/get-random-resource-without-id))]
        (collection-contents-assoc/CREATE {:resource-id (:id new-resource)}
                                           :collection-id (:id test-coll-one))
        (let [res (rp/collection-id-remove-resource (:id user-instr-c1)
                                                   (:id test-coll-one)
                                                   (:id new-resource))]
          (is (= 200 (:status res)))))))

  ; Retrieve all resources for collection
  (deftest collection-get-all-resources
    (testing "student get resources, no connection"
      (let [res (rp/collection-id-resources (:id user-stud-na)
                                           (:id test-coll-one))]
        (is (= 401 (:status res)))))
    (testing "instructor get resources, no connection"
      (let [res (rp/collection-id-resources (:id user-instr-na)
                                           (:id test-coll-one))]
        (is (= 401 (:status res)))))
    (testing "lab assistant get resources, no connection"
      (let [res (rp/collection-id-resources (:id user-la)
                                           (:id test-coll-one))]
        (is (= 200 (:status res)))))
    (testing "admin get resources, no connection"
      (let [res (rp/collection-id-resources (:id user-admin)
                                           (:id test-coll-one))]
        (is (= 200 (:status res)))))
    (testing "student get resources, with connection (student)"
      (let [res (rp/collection-id-resources (:id user-stud-stud)
                                           (:id test-coll-one))]
        (is (= 200 (:status res)))))
    (testing "student get resources, with connection (TA)"
      (let [res (rp/collection-id-resources (:id user-stud-ta)
                                           (:id test-coll-one))]
        (is (= 200 (:status res)))))
    (testing "instructor get resources, with connection (owner)"
      (let [res (rp/collection-id-resources (:id user-instr-c1)
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
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-stud-na)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "instructor add course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-instr-na)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "lab assistant add course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-la)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 200 (:status res))))))
    (testing "admin add course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-admin)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 200 (:status res))))))
    (testing "student add course, with connection (student)"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-stud-stud)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "student add course, with connection (TA)"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-stud-ta)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "instructor add course, with connection (owner)"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (let [res (rp/collection-id-add-course (:id user-instr-c1)
                                               (:id test-coll-one)
                                               (:id new-course))]
          (is (= 200 (:status res)))))))

  ; Disconnects course and collection
  (deftest collection-remove-course
    (testing "student remove course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-stud-na)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "instructor remove course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-instr-na)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "lab assistant remove course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-la)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 200 (:status res))))))
    (testing "admin remove course, no connection"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-admin)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 200 (:status res))))))
    (testing "student remove course, with connection (student)"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-stud-stud)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "student remove course, with connection (TA)"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-stud-ta)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 401 (:status res))))))
    (testing "instructor remove course, with connection (owner)"
      (let [new-course (courses/CREATE (g/get-random-course-without-id))]
        (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                          :course-id (:id new-course)})
        (let [res (rp/collection-id-remove-course (:id user-instr-c1)
                                                  (:id test-coll-one)
                                                  (:id new-course))]
          (is (= 200 (:status res))))))))
