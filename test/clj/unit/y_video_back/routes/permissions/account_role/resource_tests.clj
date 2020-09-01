(ns y-video-back.routes.permissions.account-role.resource-tests
  (:require
    [y-video-back.config :refer [env]]
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
    [y-video-back.utils.utils :as ut]
    [y-video-back.utils.db-populator :as db-pop]
    [y-video-back.user-creator :as uc]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

;get: /api/resource/{id}
(deftest resource-get-by-id
  (testing "instructor, resource-get-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, resource-get-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-get-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-get-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-get-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-get-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-get-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-get-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res))))))

;delete: /api/resource/{id}
(deftest resource-delete-by-id
  (testing "instructor, resource-delete-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, resource-delete-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-delete-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-delete-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-delete-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-delete-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-delete-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-delete-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res))))))

;patch: /api/resource/{id}
(deftest resource-patch-by-id
  (testing "instructor, resource-patch-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "instructor, resource-patch-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-patch-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-patch-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-patch-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-patch-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-patch-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-patch-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res))))))

;get: /api/resource/{id}/files
(deftest resource-id-files
  (testing "instructor, resource-id-files, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, resource-id-files, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-id-files, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-id-files, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-id-files, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-id-files, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-id-files, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student, resource-id-files, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res))))))

;get: /api/resource/{id}/collections
(deftest resource-id-collections
  (testing "instructor, resource-id-collections, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, resource-id-collections, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-collections, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-collections, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-collections, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-collections, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-collections, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-collections, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res))))))

;get: /api/resource/{id}/contents
(deftest resource-id-contents
  (testing "instructor, resource-id-contents, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, resource-id-contents, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-contents, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-contents, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-contents, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-contents, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-contents, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student, resource-id-contents, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one))
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res))))))
