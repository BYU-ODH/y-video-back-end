(ns y-video-back.routes.permissions.account-type.language-tests
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
    [y-video-back.user-creator :as uc]
    [y-video-back.db.migratus :as migratus]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (migratus/renew)
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
