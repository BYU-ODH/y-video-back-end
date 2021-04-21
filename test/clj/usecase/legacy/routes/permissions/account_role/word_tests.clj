(ns legacy.routes.permissions.account-role.word-tests
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
      (is (= 403 (:status res-one)))
      (is (= 403 (:status res-two))))))

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
      (is (= 403 (:status res-one)))
      (is (= 403 (:status res-two))))))

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
      (is (= 403 (:status res-one)))
      (is (= 403 (:status res-two))))))

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
      (is (= 403 (:status res-one)))
      (is (= 403 (:status res-two))))))
