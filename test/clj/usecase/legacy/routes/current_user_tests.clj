(ns legacy.routes.current-user-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.model-generator :as g]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.db.users :as users]
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
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (mount.core/start #'y-video-back.handler/app))


(deftest get-current-user-logged-in
  (testing "get user, only one in database"
    (let [user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id)))
          res (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))]
      (is (= 200 (:status res)))
      (is (= (-> user-one
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "get user, multiple in database"
    (let [user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id)))
          user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id)))
          user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id)))
          res-two (rp/get-current-user (uc/user-id-to-session-id (:id user-two)))
          res-one (rp/get-current-user (uc/user-id-to-session-id (:id user-one)))
          res-thr (rp/get-current-user (uc/user-id-to-session-id (:id user-thr)))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= 200 (:status res-thr)))
      (is (= (-> user-one
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> user-two
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res-two))))
      (is (= (-> user-thr
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res-thr)))))))

(deftest get-all-collections-by-logged-in
  (testing "get collections - owner only"
    (let [coll-one (db-pop/add-collection)
          res (rp/collections-by-logged-in (uc/user-id-to-session-id (:owner coll-one)))]
      (is (= 200 (:status res)))
      (is (= [(-> coll-one
                   (ut/remove-db-only)
                   (update :id str)
                   (update :owner str)
                   (assoc :content '[])
                   (assoc :expired-content '[]))]
             (m/decode-response-body res)))))
  (testing "get collections - owner only, online resource"
    (let [coll-one (db-pop/add-collection)
          cont-one (db-pop/add-content (:id coll-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000") (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/collections-by-logged-in (uc/user-id-to-session-id (:owner coll-one)))]
      (is (= 200 (:status res)))
      (is (= [(-> coll-one
                   (ut/remove-db-only)
                   (update :id str)
                   (update :owner str)
                   (assoc :content [(-> cont-one
                                        (ut/remove-db-only)
                                        (update :id str)
                                        (update :collection-id str)
                                        (update :resource-id str)
                                        (update :file-id str))])
                   (assoc :expired-content '[]))]
             (m/decode-response-body res)))))
  (testing "get collections - direct user-coll assoc only"
          ; Create user
    (let [user-one (users/CREATE (g/get-random-user-without-id))
          user-two (db-pop/add-user)
          user-id (:id user-one)
          username (:username user-one)
          ; Create collections
          coll-one (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          coll-two (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          ; Create content for coll-two
          rsrc-one (db-pop/add-resource)
          res-acc (db-pop/add-resource-access (:username user-two) (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-two) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          ; Connect user to collections
          user-coll-one (user-collections-assoc/CREATE {:collection-id (:id coll-one)
                                                        :username username
                                                        :account-role 1})
          user-coll-two (user-collections-assoc/CREATE {:collection-id (:id coll-two)
                                                        :username username
                                                        :account-role 1})
          ; Read collections by session-id
          res (rp/collections-by-logged-in (uc/user-id-to-session-id user-id))]
      (is (= 200 (:status res)))
      (is (= (frequencies [(-> coll-one
                               (ut/remove-db-only)
                               (update :id str)
                               (update :owner str)
                               (assoc :content '[])
                               (assoc :expired-content []))

                           (-> coll-two
                               (ut/remove-db-only)
                               (update :id str)
                               (update :owner str)
                               (assoc :content [(-> cont-one
                                                    (ut/remove-db-only)
                                                    (update :id str)
                                                    (update :collection-id str)
                                                    (update :resource-id str)
                                                    (update :file-id str)
                                                    )])
                               (assoc :expired-content '[]))])
             (frequencies (map ut/remove-db-only (m/decode-response-body res)))))))

  (testing "get collections - direct user-coll assoc and owner of same"
          ; Create user
    (let [user-one (users/CREATE (g/get-random-user-without-id))
          user-id (:id user-one)
          username (:username user-one)
          ; Create collections
          coll-one (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id user-id)))
          coll-two (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id user-id)))
          ; Create content for coll-two
          rsrc-one (db-pop/add-resource)
          res-acc (db-pop/add-resource-access (:username user-one) (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-two) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          ; Connect user to collections
          user-coll-one (user-collections-assoc/CREATE {:collection-id (:id coll-one)
                                                        :username username
                                                        :account-role 1})
          user-coll-two (user-collections-assoc/CREATE {:collection-id (:id coll-two)
                                                        :username username
                                                        :account-role 1})
          ; Read collections by session-id
          res (rp/collections-by-logged-in (uc/user-id-to-session-id user-id))]
      (is (= 200 (:status res)))
      (is (= (frequencies [(-> coll-one
                               (ut/remove-db-only)
                               (update :id str)
                               (update :owner str)
                               (assoc :content '[])
                               (assoc :expired-content '[]))
                           (-> coll-two
                                        (ut/remove-db-only)
                                        (update :id str)
                                        (update :owner str)
                                        (assoc :content [(-> cont-one
                                                             (ut/remove-db-only)
                                                             (update :id str)
                                                             (update :collection-id str)
                                                             (update :resource-id str)
                                                             (update :file-id str))])
                                        (assoc :expired-content '[]))])
             (frequencies (map ut/remove-db-only (m/decode-response-body res)))))))

  (testing "get collections - indirect via course only"
    (let [user-one (users/CREATE (g/get-random-user-without-id))
          user-two (db-pop/add-user)
          user-id (:id user-one)
          ; Create collections
          coll-one (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          coll-two (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          ; Create content for coll-two
          rsrc-one (db-pop/add-resource)
          res-acc (db-pop/add-resource-access (:username user-two) (:id rsrc-one))
          cont-one (db-pop/add-content (:id coll-two) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          ; Create courses
          crse-one (courses/CREATE (g/get-random-course-without-id))
          crse-two (courses/CREATE (g/get-random-course-without-id))
          ; Connect user to courses
          user-crse-one (user-courses-assoc/CREATE {:user-id user-id
                                                    :course-id (:id crse-one)})
          user-crse-two (user-courses-assoc/CREATE {:user-id user-id
                                                    :course-id (:id crse-two)})
          ; Connect courses to collections
          coll-crse-one (collection-courses-assoc/CREATE {:course-id (:id crse-one)
                                                          :collection-id (:id coll-one)})
          coll-crse-two (collection-courses-assoc/CREATE {:course-id (:id crse-two)
                                                          :collection-id (:id coll-two)})
          ; Read collections by session-id
          res (rp/collections-by-logged-in (uc/user-id-to-session-id user-id))]
        (is (= 200 (:status res)))
        (is (= (frequencies [(-> coll-one
                                 (ut/remove-db-only)
                                 (update :id str)
                                 (update :owner str)
                                 (assoc :content '[])
                                 (assoc :expired-content '[]))
                             (-> coll-two
                                          (ut/remove-db-only)
                                          (update :id str)
                                          (update :owner str)
                                          (assoc :content [(-> cont-one
                                                               (ut/remove-db-only)
                                                               (update :id str)
                                                               (update :collection-id str)
                                                               (update :resource-id str)
                                                               (update :file-id str))])
                                          (assoc :expired-content '[]))])
               (frequencies (map ut/remove-db-only (m/decode-response-body res)))))))
  (testing "get collections - user-coll and via course"
    (let [user-one (users/CREATE (g/get-random-user-without-id))
          user-two (db-pop/add-user)
          user-id (:id user-one)
          username (:username user-one)
          ; Create collections
          coll-one (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          coll-two (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          ; Connect user to collections
          user-coll-one (user-collections-assoc/CREATE {:collection-id (:id coll-one)
                                                        :username username
                                                        :account-role 1})
          user-coll-two (user-collections-assoc/CREATE {:collection-id (:id coll-two)
                                                        :username username
                                                        :account-role 1})
          coll-thr (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          coll-fou (ut/under-to-hyphen (collections/CREATE (g/get-random-collection-without-id (:id user-two))))
          ; Create courses
          crse-one (courses/CREATE (g/get-random-course-without-id))
          crse-two (courses/CREATE (g/get-random-course-without-id))
          ; Connect user to courses
          user-crse-one (user-courses-assoc/CREATE {:user-id user-id
                                                    :course-id (:id crse-one)})
          user-crse-two (user-courses-assoc/CREATE {:user-id user-id
                                                    :course-id (:id crse-two)})
          ; Connect courses to collections
          coll-crse-one (collection-courses-assoc/CREATE {:course-id (:id crse-one)
                                                          :collection-id (:id coll-thr)})
          coll-crse-two (collection-courses-assoc/CREATE {:course-id (:id crse-two)
                                                          :collection-id (:id coll-fou)})
          res (rp/collections-by-logged-in (uc/user-id-to-session-id user-id))]
      (let [updated-directs (map #(-> %
                                      (ut/remove-db-only)
                                      (update :id str)
                                      (update :owner str)
                                      (assoc :content '[])
                                      (assoc :expired-content '[]))
                                 [coll-one coll-two])
            updated-indirects (map #(-> %
                                        (ut/remove-db-only)
                                        (update :id str)
                                        (update :owner str)
                                        (assoc :content '[])
                                        (assoc :expired-content '[]))
                                   [coll-thr coll-fou])]
        (is (= (frequencies (concat updated-directs updated-indirects))
               (frequencies (m/decode-response-body res))))))))
