(ns legacy.routes.permissions.account-role.subtitle-tests
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

;post: /api/subtitle
(deftest subtitle-post
  (testing "instructor, subtitle-post, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, subtitle-post, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-post, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-post, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-post, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-post, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-post, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-post, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/get-subtitle (:id cont-one))
          res (rp/subtitle-post (uc/user-id-to-session-id (:id user-one))
                                sbtl-one)]
      (is (= 403 (:status res))))))

;get: /api/subtitle/{id}
(deftest subtitle-get-by-id
  (testing "instructor, subtitle-get-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, subtitle-get-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-get-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-get-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-get-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-get-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-get-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-get-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id sbtl-one))]
      (is (= 200 (:status res))))))

;delete: /api/subtitle/{id}
(deftest subtitle-delete-by-id
  (testing "instructor, subtitle-delete-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, subtitle-delete-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-delete-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-delete-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-delete-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-delete-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-delete-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-delete-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id sbtl-one))]
      (is (= 403 (:status res))))))

;patch: /api/subtitle/{id}
(deftest subtitle-patch-by-id
  (testing "instructor, subtitle-patch-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, subtitle-patch-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-patch-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-patch-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 200 (:status res)))))
  (testing "student, subtitle-patch-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-patch-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-patch-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 403 (:status res)))))
  (testing "student, subtitle-patch-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/subtitle-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id sbtl-one)
                                    sbtl-one)]
      (is (= 403 (:status res))))))
