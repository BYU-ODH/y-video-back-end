(ns legacy.routes.permissions.account-role.media-tests
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

;get: /api/media/get-file-key/{file-id}
(deftest media-get-file-key
  (testing "instructor, media-get-file-key, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "instructor")
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, media-get-file-key, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student, media-get-file-key, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "ta")
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student, media-get-file-key, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student, media-get-file-key, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "student")
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student, media-get-file-key, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student, media-get-file-key, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) "auditing")
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res)))))
  (testing "student, media-get-file-key, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          rsrc-one (db-pop/add-resource)
          file-one (db-pop/add-file (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (:id file-one))
          res (rp/get-file-key (uc/user-id-to-session-id (:id user-one))
                               (:id file-one))]
      (is (= 200 (:status res))))))
