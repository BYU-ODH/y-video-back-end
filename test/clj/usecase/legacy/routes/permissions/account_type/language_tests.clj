(ns legacy.routes.permissions.account-type.language-tests
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

;post: /api/language
(deftest language-post
  (testing "admin - no connection, language-post"
    (let [user-one (db-pop/add-user "admin")
          lang-one (db-pop/get-language)
          res (rp/language-post (uc/user-id-to-session-id (:id user-one))
                                lang-one)]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, language-post"
    (let [user-one (db-pop/add-user "lab-assistant")
          lang-one (db-pop/get-language)
          res (rp/language-post (uc/user-id-to-session-id (:id user-one))
                                lang-one)]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, language-post"
    (let [user-one (db-pop/add-user "instructor")
          lang-one (db-pop/get-language)
          res (rp/language-post (uc/user-id-to-session-id (:id user-one))
                                lang-one)]
      (is (= 403 (:status res)))))
  (testing "student - no connection, language-post"
    (let [user-one (db-pop/add-user "student")
          lang-one (db-pop/get-language)
          res (rp/language-post (uc/user-id-to-session-id (:id user-one))
                                lang-one)]
      (is (= 403 (:status res))))))

;delete: /api/language/{id}
(deftest language-id-delete
  (testing "admin - no connection, language-id-delete"
    (let [user-one (db-pop/add-user "admin")
          lang-one (db-pop/add-language)
          res (rp/language-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id lang-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, language-id-delete"
    (let [user-one (db-pop/add-user "lab-assistant")
          lang-one (db-pop/add-language)
          res (rp/language-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id lang-one))]
      (is (= 403 (:status res)))))
  (testing "instructor - no connection, language-id-delete"
    (let [user-one (db-pop/add-user "instructor")
          lang-one (db-pop/add-language)
          res (rp/language-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id lang-one))]
      (is (= 403 (:status res)))))
  (testing "student - no connection, language-id-delete"
    (let [user-one (db-pop/add-user "student")
          lang-one (db-pop/add-language)
          res (rp/language-id-delete (uc/user-id-to-session-id (:id user-one))
                                     (:id lang-one))]
      (is (= 403 (:status res))))))

;get: /api/languages
(deftest language-get-all
  (testing "admin - no connection, language-get-all"
    (let [user-one (db-pop/add-user "admin")
          lang-one (db-pop/add-language)
          res (rp/language-get-all (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "lab assistant - no connection, language-get-all"
    (let [user-one (db-pop/add-user "lab-assistant")
          lang-one (db-pop/add-language)
          res (rp/language-get-all (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "instructor - no connection, language-get-all"
    (let [user-one (db-pop/add-user "instructor")
          lang-one (db-pop/add-language)
          res (rp/language-get-all (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "student - no connection, language-get-all"
    (let [user-one (db-pop/add-user "student")
          lang-one (db-pop/add-language)
          res (rp/language-get-all (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res))))))
