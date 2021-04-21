(ns legacy.routes.permissions.account-type.word-tests
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
