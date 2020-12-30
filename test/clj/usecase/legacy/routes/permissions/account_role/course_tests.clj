(ns legacy.routes.permissions.account-role.course-tests
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [legacy.utils.model-generator :as g]
    [legacy.utils.route-proxy.proxy :as rp]
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
    [legacy.utils.utils :as ut]
    [legacy.utils.db-populator :as db-pop]
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

;get: /api/course/{id}
(deftest course-get-by-id
  (testing "instructor, course-get-by-id, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-get-by-id, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-get-by-id, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-get-by-id, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-get-by-id, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-get-by-id, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-get-by-id, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-get-by-id, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-get-by-id, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-get (uc/user-id-to-session-id (:id user-one))
                                (:id crse-one))]
      (is (= 200 (:status res))))))

;delete: /api/course/{id}
(deftest course-delete-by-id
  (testing "instructor, course-delete-by-id, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, course-delete-by-id, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, course-delete-by-id, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, course-delete-by-id, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-delete-by-id, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-delete-by-id, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-delete-by-id, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-delete-by-id, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-delete-by-id, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-delete (uc/user-id-to-session-id (:id user-one))
                                   (:id crse-one))]
      (is (= 403 (:status res))))))

;patch: /api/course/{id}
(deftest course-patch-by-id
  (testing "instructor, course-patch-by-id, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "instructor, course-patch-by-id, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "instructor, course-patch-by-id, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "instructor, course-patch-by-id, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "student, course-patch-by-id, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "student, course-patch-by-id, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "student, course-patch-by-id, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "student, course-patch-by-id, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res)))))
  (testing "student, course-patch-by-id, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-patch (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one)
                                  crse-one)]
      (is (= 403 (:status res))))))

;get: /api/course/{id}/collections
(deftest course-id-collections
  (testing "instructor, course-id-collections, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-collections, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-collections, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-collections, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-id-collections, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-id-collections, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-id-collections, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-id-collections, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "student, course-id-collections, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-collections (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one))]
      (is (= 200 (:status res))))))

;post: /api/course/{id}/add-user
(deftest course-id-add-user
  (testing "instructor, course-id-add-user, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-add-user, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-add-user, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res)))))
  (testing "instructor, course-id-add-user, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res)))))
  (testing "student, course-id-add-user, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res)))))
  (testing "student, course-id-add-user, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res)))))
  (testing "student, course-id-add-user, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res)))))
  (testing "student, course-id-add-user, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res)))))
  (testing "student, course-id-add-user, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          user-two (db-pop/add-user)
          res (rp/course-id-add-user (uc/user-id-to-session-id (:id user-one))
                                     (:id crse-one)
                                     (:id user-two)
                                     0)] ; role doesn't matter here
      (is (= 403 (:status res))))))

;post: /api/course/{id}/remove-user
(deftest course-id-remove-user
  (testing "instructor, course-id-remove-user, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-remove-user, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-remove-user, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "instructor, course-id-remove-user, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-remove-user, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-remove-user, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-remove-user, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-remove-user, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-remove-user, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one))
          res (rp/course-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                        (:id crse-one)
                                        (:id user-two))]
      (is (= 403 (:status res))))))

;get: /api/course/{id}/users
(deftest course-id-users
  (testing "instructor, course-id-users, owns collection that claims course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-users, connected via user-crse as instructor"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "instructor")
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, course-id-users, connected via user-crse as student"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "instructor, course-id-users, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-users, connected via user-coll as ta"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "ta")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-users, connected via user-coll as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-crse-add (db-pop/add-coll-crse-assoc)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one)
                                                    (:collection-id coll-crse-add)
                                                    "auditing")
          crse-one {:id (:course-id coll-crse-add)}
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-users, connected via user-crse as ta"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "ta")
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-users, connected via user-crse as student"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "student")
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res)))))
  (testing "student, course-id-users, connected via user-crse as auditing"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          coll-crse-add (db-pop/add-user-crse-assoc (:id user-one)
                                                    (:id crse-one)
                                                    "auditing")
          res (rp/course-id-users (uc/user-id-to-session-id (:id user-one))
                                  (:id crse-one))]
      (is (= 403 (:status res))))))
