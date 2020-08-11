(ns y-video-back.routes.permissions.account-role.word-tests
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


;post: /api/word
(deftest word-post
  (testing "instructor, word-post, own word"
    (let [user-one (db-pop/add-user "instructor")
          res (rp/word-post (uc/user-id-to-session-id (:id user-one))
                            (db-pop/get-word (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "student, word-post, own word"
    (let [user-one (db-pop/add-user "student")
          res (rp/word-post (uc/user-id-to-session-id (:id user-one))
                            (db-pop/get-word (:id user-one)))]
      (is (= 200 (:status res)))))
  (testing "instructor/student, word-post, in same class"
    (let [user-one (db-pop/add-user "instructor")
          user-stud (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          user-one-crse (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          user-one-crse (db-pop/add-user-crse-assoc (:id user-stud) (:id crse-one) "student")
          res-one (rp/word-post (uc/user-id-to-session-id (:id user-one))
                                (db-pop/get-word (:id user-stud)))
          res-two (rp/word-post (uc/user-id-to-session-id (:id user-stud))
                                (db-pop/get-word (:id user-one)))]
      (is (= 401 (:status res-one)))
      (is (= 401 (:status res-two))))))

;get: /api/word/{id}
(deftest word-get-by-id
  (testing "instructor, word-get-by-id, own word"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/add-word (:id user-one))
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "student, word-get-by-id, own word"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/add-word (:id user-one))
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "instructor/student, word-get-by-id, in same class"
    (let [user-instr (db-pop/add-user "instructor")
          user-stud (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          user-one-crse (db-pop/add-user-crse-assoc (:id user-instr) (:id crse-one) "instructor")
          user-one-crse (db-pop/add-user-crse-assoc (:id user-stud) (:id crse-one) "student")
          word-instr (db-pop/add-word (:id user-instr))
          word-stud (db-pop/add-word (:id user-stud))
          res-one (rp/word-id-get (uc/user-id-to-session-id (:id user-instr))
                                  (:id word-stud))
          res-two (rp/word-id-get (uc/user-id-to-session-id (:id user-stud))
                                  (:id word-instr))]
      (is (= 401 (:status res-one)))
      (is (= 401 (:status res-two))))))

;delete: /api/word/{id}
(deftest word-delete-by-id
  (testing "instructor, word-delete-by-id, own word"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/add-word (:id user-one))
          res (rp/word-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "student, word-delete-by-id, own word"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/add-word (:id user-one))
          res (rp/word-id-delete (uc/user-id-to-session-id (:id user-one))
                                 (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "instructor/student, word-delete-by-id, in same class"
    (let [user-instr (db-pop/add-user "instructor")
          user-stud (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          user-one-crse (db-pop/add-user-crse-assoc (:id user-instr) (:id crse-one) "instructor")
          user-one-crse (db-pop/add-user-crse-assoc (:id user-stud) (:id crse-one) "student")
          word-instr (db-pop/add-word (:id user-instr))
          word-stud (db-pop/add-word (:id user-stud))
          res-one (rp/word-id-delete (uc/user-id-to-session-id (:id user-instr))
                                     (:id word-stud))
          res-two (rp/word-id-delete (uc/user-id-to-session-id (:id user-stud))
                                     (:id word-instr))]
      (is (= 401 (:status res-one)))
      (is (= 401 (:status res-two))))))

;patch: /api/word/{id}
(deftest word-patch-by-id
  (testing "instructor, word-patch-by-id, own word"
    (let [user-one (db-pop/add-user "instructor")
          word-one (db-pop/add-word (:id user-one))
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "student, word-patch-by-id, own word"
    (let [user-one (db-pop/add-user "student")
          word-one (db-pop/add-word (:id user-one))
          res (rp/word-id-get (uc/user-id-to-session-id (:id user-one))
                              (:id word-one))]
      (is (= 200 (:status res)))))
  (testing "instructor/student, word-patch-by-id, in same class"
    (let [user-instr (db-pop/add-user "instructor")
          user-stud (db-pop/add-user "student")
          crse-one (db-pop/add-course)
          user-one-crse (db-pop/add-user-crse-assoc (:id user-instr) (:id crse-one) "instructor")
          user-one-crse (db-pop/add-user-crse-assoc (:id user-stud) (:id crse-one) "student")
          word-instr (db-pop/add-word (:id user-instr))
          word-stud (db-pop/add-word (:id user-stud))
          res-one (rp/word-id-patch (uc/user-id-to-session-id (:id user-instr))
                                    (:id word-stud)
                                    word-stud)
          res-two (rp/word-id-patch (uc/user-id-to-session-id (:id user-stud))
                                    (:id word-instr)
                                    word-instr)]
      (is (= 401 (:status res-one)))
      (is (= 401 (:status res-two))))))
