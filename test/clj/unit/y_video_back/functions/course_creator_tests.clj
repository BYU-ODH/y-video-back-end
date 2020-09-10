(ns y-video-back.functions.course-creator-tests
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
      [y-video-back.user-creator :as uc]
      [y-video-back.course-creator :as cc]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.utils.account-permissions :as ac]
      [y-video-back.apis.student-schedule :as schedule-api]))

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

(defn remove-course-db-fields
  [course]
  (-> course
      (ut/remove-db-only)
      (dissoc :id :account-role :user-id)))
;
; (defn check-against-test-user
;   [user-id]
;   (is (= (frequencies (get-in env [:test-user
;                                    :courses]))
;          (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER user-id))))))
;
; (deftest create-courses
;   (testing "existing user, no courses"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)]
;       (is (= [] (user-courses-assoc/READ-COURSES-BY-USER (:id user-one-add))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (check-against-test-user (:id user-one-add)))))
;
; (deftest create-courses-2
;   (testing "existing user, missing courses"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           test-crse-one (first (get-in env [:test-user :courses]))
;           crse-one (db-pop/add-course (:department test-crse-one)
;                                       (:catalog-number test-crse-one)
;                                       (:section-number test-crse-one))
;           user-crse (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-one) "student")]
;       (is (= (frequencies [(remove-course-db-fields crse-one)])
;              (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER (:id user-one-add))))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (check-against-test-user (:id user-one-add)))))
;
; (deftest delete-courses
;   (testing "existing user, all wrong courses"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           crse-one (db-pop/add-course)
;           crse-two (db-pop/add-course)
;           crse-thr (db-pop/add-course)
;           user-crse-one (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-one) "student")
;           user-crse-two (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-two) "student")
;           user-crse-thr (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-thr) "student")]
;       (is (= (frequencies (map remove-course-db-fields [crse-one crse-two crse-thr]))
;              (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER (:id user-one-add))))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (check-against-test-user (:id user-one-add)))))
;
; (deftest delete-courses-2
;   (testing "existing user, 1 correct course"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           test-crse-one (first (get-in env [:test-user :courses]))
;           crse-one (db-pop/add-course (:department test-crse-one)
;                                       (:catalog-number test-crse-one)
;                                       (:section-number test-crse-one))
;           crse-two (db-pop/add-course)
;           crse-thr (db-pop/add-course)
;           user-crse-one (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-one) "student")
;           user-crse-two (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-two) "student")
;           user-crse-thr (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-thr) "student")]
;       (is (= (frequencies (map remove-course-db-fields [crse-one crse-two crse-thr]))
;              (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER (:id user-one-add))))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (check-against-test-user (:id user-one-add)))))
;
; (deftest auditing-course
;   (testing "existing user, auditing non-related course"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           crse-one (db-pop/add-course)
;           user-crse-one (db-pop/add-user-crse-assoc (:id user-one-add) (:id crse-one) "auditing")]
;       (is (= (frequencies (map remove-course-db-fields [crse-one]))
;              (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER (:id user-one-add))))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (is (= (frequencies (into (get-in env [:test-user
;                                              :courses])
;                                 [{:department (:department crse-one)
;                                   :catalog-number (:catalog-number crse-one)
;                                   :section-number (:section-number crse-one)}]))
;              (frequencies (map remove-course-db-fields (user-courses-assoc/READ-COURSES-BY-USER (:id user-one-add)))))))))
;
; (deftest user-shares-course
;   (testing "user in db already connected to test course"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           test-crse-one (first (get-in env [:test-user :courses]))
;           crse-one (db-pop/add-course (:department test-crse-one)
;                                       (:catalog-number test-crse-one)
;                                       (:section-number test-crse-one))
;           user-two (db-pop/add-user)
;           user-crse-two (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one) "student")]
;       (is (= (frequencies [(into crse-one {:account-role (ac/to-int-role "student") :user-id (:id user-two)})])
;              (frequencies (map ut/remove-db-only (user-courses-assoc/READ-COURSES-BY-USER (:id user-two))))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (is (= (frequencies [(into crse-one {:account-role (ac/to-int-role "student") :user-id (:id user-two)})])
;              (frequencies (map ut/remove-db-only (user-courses-assoc/READ-COURSES-BY-USER (:id user-two))))))
;       (check-against-test-user (:id user-one-add)))))
;
; (deftest user-shares-course-student-drops
;   (testing "user in db and test user both connected to cource (not test course)"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           test-crse-one (first (get-in env [:test-user :courses]))
;           crse-one (db-pop/add-course)
;           user-two (db-pop/add-user)
;           user-crse-two (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
;           user-crse-two (db-pop/add-user-crse-assoc (:id user-two) (:id crse-one) "student")]
;       (is (= (frequencies [(into crse-one {:account-role (ac/to-int-role "student") :user-id (:id user-two)})])
;              (frequencies (map ut/remove-db-only (user-courses-assoc/READ-COURSES-BY-USER (:id user-two))))))
;       (cc/check-courses-with-api (:username user-one) true)
;       (is (= (frequencies [(into crse-one {:account-role (ac/to-int-role "student") :user-id (:id user-two)})])
;              (frequencies (map ut/remove-db-only (user-courses-assoc/READ-COURSES-BY-USER (:id user-two))))))
;       (check-against-test-user (:id user-one-add)))))
;
; (deftest coll-claims-course
;   (testing "collection in db already connected to course"
;     (let [user-one (-> (db-pop/get-user)
;                        (dissoc :username)
;                        (assoc :username (get-in env [:test-user :username]))
;                        (assoc :byu-person-id (get-in env [:test-user :byu-person-id])))
;           user-one-add (users/CREATE user-one)
;           test-crse-one (first (get-in env [:test-user :courses]))
;           crse-one (db-pop/add-course (:department test-crse-one)
;                                       (:catalog-number test-crse-one)
;                                       (:section-number test-crse-one))
;           coll-one (db-pop/add-collection)
;           coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))]
;       (is (collection-courses-assoc/EXISTS-COLL-CRSE? (:id coll-one) (:id crse-one)))
;       (cc/check-courses-with-api (:username user-one) true)
;       (is (collection-courses-assoc/EXISTS-COLL-CRSE? (:id coll-one) (:id crse-one)))
;       (check-against-test-user (:id user-one-add)))))
