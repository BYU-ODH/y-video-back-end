(ns y-video-back.routes.permissions.account-type.word-tests
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
    (ut/renew-db)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

;post: /api/word
(deftest word-post
  (testing "admin - not owner, word-post"
    (let [user-one (db-pop/add-user "admin")
          word-one (db-pop/get-word)
          res (rp/word-post (uc/user-id-to-session-id (:id user-one))
                            word-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - not owner, word-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          word-one (db-pop/get-word)
          res (rp/word-post (uc/user-id-to-session-id (:id user-one))
                            word-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - not owner, word-post"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/get-word)
          res (rp/word-post (uc/user-id-to-session-id (:id user-one))
                            word-one)]
      (is (= 403 (:status res)))))
  (testing "student - not owner, word-post"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/get-word)
          res (rp/word-post (uc/user-id-to-session-id (:id user-one))
                            word-one)]
      (is (= 403 (:status res))))))

;get: /api/word/{id}
(deftest word-get-by-id
  (testing "admin - not owner, word-get-by-id"
    (let [user-one (db-pop/add-user "admin")
          word-one (db-pop/add-word)
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - not owner, word-get-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          word-one (db-pop/add-word)
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - not owner, word-get-by-id"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/add-word)
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 403 (:status res)))))
  (testing "student - not owner, word-get-by-id"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/add-word)
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 403 (:status res))))))

;delete: /api/word/{id}
(deftest word-delete-by-id
  (testing "admin - not owner, word-delete-by-id"
    (let [user-one (db-pop/add-user "admin")
          word-one (db-pop/add-word)
          res (rp/word-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - not owner, word-delete-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          word-one (db-pop/add-word)
          res (rp/word-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "instructor - not owner, word-delete-by-id"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/add-word)
          res (rp/word-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id word-one))]
      (is (= 403 (:status res)))))
  (testing "student - not owner, word-delete-by-id"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/add-word)
          res (rp/word-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id word-one))]
      (is (= 403 (:status res))))))

;patch: /api/word/{id}
(deftest word-patch-by-id
  (testing "admin - not owner, word-patch-by-id"
    (let [user-one (db-pop/add-user "admin")
          word-one (db-pop/add-word)
          res (rp/word-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id word-one)
                                word-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - not owner, word-patch-by-id"
    (let [user-one (db-pop/add-user "lab-assistant")
          word-one (db-pop/add-word)
          res (rp/word-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id word-one)
                                word-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - not owner, word-patch-by-id"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/add-word)
          res (rp/word-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id word-one)
                                word-one)]
      (is (= 403 (:status res)))))
  (testing "student - not owner, word-patch-by-id"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/add-word)
          res (rp/word-id-patch (uc/user-id-to-session-id (:id user-one))
                                (:id word-one)
                                word-one)]
      (is (= 403 (:status res))))))
