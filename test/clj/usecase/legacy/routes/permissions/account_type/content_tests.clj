(ns legacy.routes.permissions.account-type.content-tests
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

;post: /api/content
(deftest content-post
  (testing "admin - no connection, content-post, other user owns collection without access"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-two))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "admin - no connection, content-post, other user owns collection with access"
    (let [user-one (db-pop/add-user "admin")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-two))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one))
          rsrc-acc (db-pop/add-resource-access (:username user-two) (:id rsrc-one))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 200 (:status res)))))
  (testing "lab-assistant - no connection, content-post, other user owns collection without access"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-two))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "lab-assistant - no connection, content-post, other user owns collection with access"
    (let [user-one (db-pop/add-user "lab-assistant")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-two))
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/get-content (:id coll-one) (:id rsrc-one))
          rsrc-acc (db-pop/add-resource-access (:username user-two) (:id rsrc-one))
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, content-post"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/get-content)
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-post"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/get-content)
          res (rp/content-post (uc/user-id-to-session-id (:id user-one))
                               cont-one)]
      (is (= 403 (:status res))))))

;get: /api/content/{id}
(deftest content-get-by-id
  (testing "admin - no connection, content-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          cont-one (db-pop/add-content)
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, content-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          cont-one (db-pop/add-content)
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, content-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/add-content)
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-get-by-id"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/add-content)
          res (rp/content-id-get (uc/user-id-to-session-id (:id user-one))
                                 (:id cont-one))]
      (is (= 403 (:status res))))))

;delete: /api/content/{id}
(deftest content-delete-by-id
  (testing "admin - no connection, content-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          cont-one (db-pop/add-content)
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, content-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          cont-one (db-pop/add-content)
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, content-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/add-content)
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/add-content)
          res (rp/content-id-delete (uc/user-id-to-session-id (:id user-one))
                                    (:id cont-one))]
      (is (= 403 (:status res))))))

;patch: /api/content/{id}
(deftest content-patch-by-id
  (testing "admin - no connection, content-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          cont-one (db-pop/add-content)
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, content-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          cont-one (db-pop/add-content)
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, content-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/add-content)
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/add-content)
          res (rp/content-id-patch (uc/user-id-to-session-id (:id user-one))
                                   (:id cont-one)
                                   cont-one)]
      (is (= 403 (:status res))))))

;post: /api/content/{id}/add-view
(deftest content-id-add-view
  (testing "admin - no connection, content-id-add-view"
    (let [user-one (db-pop/add-user "admin")
          cont-one (db-pop/add-content)
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, content-id-add-view"
    (let [user-one (db-pop/add-user "lab-assistant")
          cont-one (db-pop/add-content)
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, content-id-add-view"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/add-content)
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-id-add-view"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/add-content)
          res (rp/content-id-add-view (uc/user-id-to-session-id (:id user-one))
                                      (:id cont-one))]
      (is (= 403 (:status res))))))

;post: /api/content/{id}/add-subtitle
(comment (deftest content-id-add-subtitle)
  (testing "admin - no connection, content-id-add-subtitle"
    (let [user-one (db-pop/add-user "admin")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, content-id-add-subtitle"
    (let [user-one (db-pop/add-user "lab-assistant")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, content-id-add-subtitle"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-id-add-subtitle"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 403 (:status res))))))

;post: /api/content/{id}/remove-subtitle
(comment (deftest content-id-remove-subtitle)
  (testing "admin - no connection, content-id-remove-subtitle"
    (let [user-one (db-pop/add-user "admin")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))

          res (rp/content-id-add-subtitle (uc/user-id-to-session-id (:id user-one))
                                          (:id cont-one)
                                          (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, content-id-remove-subtitle"
    (let [user-one (db-pop/add-user "lab-assistant")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          sbtl-add-res (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, content-id-remove-subtitle"
    (let [user-one (db-pop/add-user "instructor")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          sbtl-add-res (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, content-id-remove-subtitle"
    (let [user-one (db-pop/add-user "student")
          cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          sbtl-add-res (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-remove-subtitle (uc/user-id-to-session-id (:id user-one))
                                             (:id cont-one)
                                             (:id sbtl-one))]
      (is (= 403 (:status res))))))
