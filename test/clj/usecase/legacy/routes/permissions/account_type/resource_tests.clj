(ns legacy.routes.permissions.account-type.resource-tests
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

;post: /api/resource
(deftest resource-post
  (testing "admin, resource-post"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/get-resource)
          res (rp/resource-post (uc/user-id-to-session-id (:id user-one))
                                rsrc-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant, resource-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/get-resource)
          res (rp/resource-post (uc/user-id-to-session-id (:id user-one))
                                rsrc-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, resource-post"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/get-resource)
          res (rp/resource-post (uc/user-id-to-session-id (:id user-one))
                                rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student, resource-post"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/get-resource)
          res (rp/resource-post (uc/user-id-to-session-id (:id user-one))
                                rsrc-one)]
      (is (= 403 (:status res))))))

;get: /api/resource/{id}
(deftest resource-get-by-id
  (testing "admin - no connection, resource-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, resource-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, resource-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student - no connection, resource-get-by-id"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-get (uc/user-id-to-session-id (:id user-one))
                                  (:id rsrc-one))]
      (is (= 403 (:status res))))))

;delete: /api/resource/{id}
(deftest resource-delete-by-id
  (testing "admin - no connection, resource-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, resource-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, resource-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, resource-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id rsrc-one))]
      (is (= 403 (:status res))))))

;patch: /api/resource/{id}
(deftest resource-patch-by-id
  (testing "admin - no connection, resource-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, resource-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, resource-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, resource-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-patch (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one)
                                    rsrc-one)]
      (is (= 403 (:status res))))))

;get: /api/resource/{id}/files
(deftest resource-id-files
  (testing "admin - no connection, resource-id-files"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, resource-id-files"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, resource-id-files"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "student - no connection, resource-id-files"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-files (uc/user-id-to-session-id (:id user-one))
                                    (:id rsrc-one))]
      (is (= 403 (:status res))))))

;get: /api/resource/{id}/collections
(deftest resource-id-collections
  (testing "admin - no connection, resource-id-collections"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, resource-id-collections"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, resource-id-collections"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, resource-id-collections"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-collections (uc/user-id-to-session-id (:id user-one))
                                          (:id rsrc-one))]
      (is (= 403 (:status res))))))

;get: /api/resource/{id}/contents
(deftest resource-id-contents
  (testing "admin - no connection, resource-id-contents"
    (let [user-one (db-pop/add-user "admin")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, resource-id-contents"
    (let [user-one (db-pop/add-user "lab-assistant")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, resource-id-contents"
    (let [user-one (db-pop/add-user "instructor")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, resource-id-contents"
    (let [user-one (db-pop/add-user "student")
          rsrc-one (db-pop/add-resource)
          res (rp/resource-id-contents (uc/user-id-to-session-id (:id user-one))
                                       (:id rsrc-one))]
      (is (= 403 (:status res))))))