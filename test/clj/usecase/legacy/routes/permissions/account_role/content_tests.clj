(ns legacy.routes.permissions.account-role.content-tests
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.test :refer :all]
    [legacy.db.test-util :as tcore]
    [legacy.utils.db-populator :as db-pop]
    [legacy.utils.route-proxy.proxy :as rp]
    [legacy.utils.utils :as ut]
    [mount.core :as mount]
    [y-video-back.config :refer [env]]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.handler :refer :all]
    [y-video-back.user-creator :as uc]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (ut/renew-db)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

;post: /api/content
(deftest content-post
  (testing "instructor, content-post, owns collection, has resource access"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, content-post, owns collection, online only resource"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          cont-one (db-pop/get-content (:id coll-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000") (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 200 (:status res)))))
  ; TODO finish this test when we check for resource access via user-coll (i.e. TA access)
  ; (testing "instructor, content-post, instructor via user-coll, has resource access"
  ;   (let [user-one (db-pop/add-user "instructor")
  ;         coll-one (db-pop/add-collection)
  ;         rsrc-one (db-pop/add-resource)
  ;         rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
  ;         cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one))
  ;         user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
  ;                                                   (:id coll-one)
  ;                                                   "instructor")
  ;         res (rp/content-post (uc/user-id-to-session-id (:id user-one))
  ;                              cont-one)]
  ;     (is (= 200 (:status res)))))
  (testing "instructor, content-post, owns collection, no resource access"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "instructor, content-post, instructor via user-coll, no resource access"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:id coll-one)
                                                    "instructor")
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "instructor, content-post, student via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:id coll-one)
                                                    "student")
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "student, content-post, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:id coll-one)
                                                    "ta")
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "student, content-post, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:id coll-one)
                                                    "student")
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "student, content-post, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:id coll-one)
                                                    "auditing")
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res))))))

;get: /api/content/{id}
(deftest content-get-by-id
  (testing "instructor, content-get-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, content-get-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-get-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-get-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-get-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-get-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-get-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-get-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res))))))

;delete: /api/content/{id}
(deftest content-delete-by-id
  (testing "instructor, content-delete-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, content-delete-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-delete-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-delete-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-delete-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-delete-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-delete-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-delete-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res))))))

;patch: /api/content/{id}
(deftest content-patch-by-id
  (testing "instructor, content-patch-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          coll-two (db-pop/add-collection (:id user-one))
          rsrc-two (db-pop/add-resource)
          cont-two (db-pop/get-content (:id coll-two) (:id rsrc-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-two))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-two)]
      (is (= 200 (:status res)))))
  (testing "instructor, content-patch-by-id, instructor via user-coll, change to online only resource"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          coll-two (db-pop/add-collection (:id user-one))
          cont-two (db-pop/get-content (:id coll-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000") (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-two)]
      (is (= 200 (:status res)))))
  (testing "instructor, content-patch-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          coll-two (db-pop/add-collection (:id user-one))
          rsrc-two (db-pop/add-resource)
          cont-two (db-pop/get-content (:id coll-two) (:id rsrc-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-two))
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-two)]
      (is (= 200 (:status res)))))
  (testing "student, content-patch-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          coll-two (db-pop/add-collection (:id user-one))
          rsrc-two (db-pop/add-resource)
          cont-two (db-pop/get-content (:id coll-two) (:id rsrc-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-two))
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-two)]
      (is (= 200 (:status res)))))
  (testing "student, content-patch-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection (:id user-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          coll-two (db-pop/add-collection (:id user-one))
          rsrc-two (db-pop/add-resource)
          cont-two (db-pop/get-content (:id coll-two) (:id rsrc-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          rsrc-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-two))
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-two)]
      (is (= 200 (:status res)))))
  (testing "student, content-patch-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 403 (:status res)))))
  (testing "student, content-patch-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 403 (:status res)))))
  (testing "student, content-patch-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 403 (:status res)))))
  (testing "student, content-patch-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 403 (:status res))))))

;post: /api/content/{id}/add-view
(deftest content-id-add-view
  (testing "instructor, content-id-add-view, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, content-id-add-view, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-view, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-view, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-view, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-view, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-view, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-view, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res))))))

;post: /api/content/{id}/add-subtitle
(comment (deftest content-id-add-subtitle)
  (testing "instructor, content-id-add-subtitle, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, content-id-add-subtitle, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-subtitle, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-subtitle, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-add-subtitle, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-id-add-subtitle, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-id-add-subtitle, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-id-add-subtitle, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 403 (:status res))))))

;post: /api/content/{id}/remove-subtitle
(comment (deftest content-id-remove-subtitle)
  (testing "instructor, content-id-remove-subtitle, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, content-id-remove-subtitle, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-remove-subtitle, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-remove-subtitle, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, content-id-remove-subtitle, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-id-remove-subtitle, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-id-remove-subtitle, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, content-id-remove-subtitle, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id rsrc-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 403 (:status res))))))
