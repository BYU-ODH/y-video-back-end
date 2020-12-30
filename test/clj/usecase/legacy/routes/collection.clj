(ns legacy.routes.collection
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
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.users :as users]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
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

(deftest test-session-id-header
  (testing "coll CREATE - session id header"
    (let [coll-one (db-pop/get-collection)]
      (let [res (rp/collection-post coll-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
              ;header (m/decode-response-body res)]
          (is (= (into coll-one {:id id})
                 (ut/remove-db-only (collections/READ id)))))))))

(deftest test-coll
  (testing "coll CREATE"
    (let [coll-one (db-pop/get-collection)]
      (let [res (rp/collection-post coll-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into coll-one {:id id})
                 (ut/remove-db-only (collections/READ id))))))))
  (testing "coll READ"
    (let [coll-one (ut/under-to-hyphen (collections/CREATE (db-pop/get-collection)))
          res (rp/collection-id-get (:id coll-one))]
      (is (= 200 (:status res)))
      (is (= (-> coll-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :owner str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "collection UPDATE"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          coll-two (db-pop/get-collection)]
      (let [res (rp/collection-id-patch (:id coll-one) coll-two)]
        (is (= 200 (:status res)))
        (is (= (into coll-two {:id (:id coll-one)}) (ut/remove-db-only (collections/READ (:id coll-one))))))))
  (testing "collection DELETE"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          res (rp/collection-id-delete (:id coll-one))]
      (is (= 200 (:status res)))
      (is (= nil (collections/READ (:id coll-one)))))))

(deftest coll-add-user
  (testing "add user to collection, user already in db"
    (let [coll-one (db-pop/add-collection)
          user-one (db-pop/add-user)]
      (is (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)])))
      (let [res (rp/collection-id-add-user (:id coll-one)
                                           (:username user-one)
                                           0)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list {:id id
                        :collection-id (:id coll-one)
                        :username (:username user-one)
                        :account-role 0})
                 (map ut/remove-db-only (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)]))))))))
  (testing "add user to collection, already connected"
    (let [coll-one (db-pop/add-collection)
          user-one (db-pop/add-user)
          user-one-add (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one) 1)]
      (is (= [{:username (:username user-one)
               :collection-id (:id coll-one)
               :account-role 1}]
             (map #(-> %
                       (ut/remove-db-only)
                       (dissoc :id))
                  (user-collections-assoc/READ-BY-COLLECTION (:id coll-one)))))
      (let [res (rp/collection-id-add-user (:id coll-one)
                                           (:username user-one)
                                           0)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list {:id id
                        :collection-id (:id coll-one)
                        :username (:username user-one)
                        :account-role 0})
                 (map ut/remove-db-only (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)])))))))))
  ; (testing "add user to collection, user not in db"
  ;   ; Add collection, connect to username
  ;   (let [coll-one (db-pop/add-collection)
  ;         user-one {:username (get-in env [:test-user :username])}]
  ;     (is (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)])))
  ;     (is (= '() (users/READ-BY-USERNAME [(:username user-one)])))
  ;     (let [res (rp/collection-id-add-user (:id coll-one)
  ;                                          (:username user-one)
  ;                                          0)]
  ;       (is (= 200 (:status res)))
  ;       (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
  ;         (is (= (list {:id id
  ;                       :collection-id (:id coll-one)
  ;                       :username (:username user-one)
  ;                       :account-role 0})
  ;                (map ut/remove-db-only (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)]))))))
  ;     ; Check user was created
  ;     (is (= (:username user-one) (:username (first (users/READ-BY-USERNAME [(:username user-one)])))))
  ;     ; Check if collection in get collections by logged in
  ;     (let [user-one-res (first (users/READ-BY-USERNAME [(:username user-one)]))
  ;           res (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-one-res)))]
  ;       (is (= [(-> coll-one
  ;                   (ut/remove-db-only)
  ;                   (update :id str)
  ;                   (update :owner str)
  ;                   (assoc :content []))]
  ;              (m/decode-response-body res)))))))

(deftest coll-add-users
  (testing "add list of users to collection"
    (let [coll-one (db-pop/add-collection)
          user-one (db-pop/add-user)
          user-two (db-pop/add-user)
          user-thr (db-pop/add-user)]
      (is (= '() (user-collections-assoc/READ-BY-COLLECTION (:id coll-one))))
      (let [res (rp/collection-id-add-users (:id coll-one)
                                            [(:username user-one) (:username user-two) (:username user-thr)]
                                            0)]
        (is (= 200 (:status res)))
        (is (= (frequencies (map #(into {}
                                        {:collection-id (:id coll-one)
                                         :username (:username %)
                                         :account-role 0})
                                 [user-one user-two user-thr]))
               (frequencies (map #(-> %
                                      (ut/remove-db-only)
                                      (dissoc :id))
                                 (user-collections-assoc/READ-BY-COLLECTION (:id coll-one)))))))))
  (testing "add list of users to collection, not in db"
    (let [coll-one (db-pop/add-collection)
          user-one (db-pop/get-user)
          user-two (db-pop/add-user)
          user-thr (db-pop/get-user)
          user-fou (db-pop/add-user)
          user-fou-add (db-pop/add-user-coll-assoc (:username user-fou) (:id coll-one) 1)]
      (is (= [{:username (:username user-fou)
               :collection-id (:id coll-one)
               :account-role 1}]
             (map #(-> %
                       (ut/remove-db-only)
                       (dissoc :id))
                  (user-collections-assoc/READ-BY-COLLECTION (:id coll-one)))))
      (is (= '() (users/READ-BY-USERNAME [(:username user-one)])))
      (is (= '() (users/READ-BY-USERNAME [(:username user-thr)])))
      (let [res (rp/collection-id-add-users (:id coll-one)
                                            [(:username user-one) (:username user-two) (:username user-thr) (:username user-fou)]
                                            0)]
        (is (= 200 (:status res)))
        (is (= (frequencies (map #(into {}
                                        {:collection-id (:id coll-one)
                                         :username (:username %)
                                         :account-role 0})
                                 [user-one user-two user-thr user-fou]))
               (frequencies (map #(-> %
                                      (ut/remove-db-only)
                                      (dissoc :id))
                                 (user-collections-assoc/READ-BY-COLLECTION (:id coll-one)))))))
      (is (= (:username user-one) (:username (first (users/READ-BY-USERNAME [(:username user-one)])))))
      (is (= (:username user-thr) (:username (first (users/READ-BY-USERNAME [(:username user-thr)])))))
      (let [user-one-res (first (users/READ-BY-USERNAME [(:username user-one)]))
            user-thr-res (first (users/READ-BY-USERNAME [(:username user-thr)]))
            res-one (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-one-res)))
            res-two (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-two)))
            res-thr (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-thr-res)))
            res-fou (rp/collections-by-logged-in (uc/user-id-to-session-id (:id user-fou)))]
        (is (= [{:username (:username user-fou)
                 :collection-id (:id coll-one)
                 :account-role 0}]
               (map #(-> %
                         (ut/remove-db-only)
                         (dissoc :id))
                    (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-fou)]))))
        (is (= [(-> coll-one
                    (ut/remove-db-only)
                    (update :id str)
                    (update :owner str)
                    (assoc :content []))]
               (m/decode-response-body res-one)))
        (is (= [(-> coll-one
                    (ut/remove-db-only)
                    (update :id str)
                    (update :owner str)
                    (assoc :content []))]
               (m/decode-response-body res-two)))
        (is (= [(-> coll-one
                    (ut/remove-db-only)
                    (update :id str)
                    (update :owner str)
                    (assoc :content []))]
               (m/decode-response-body res-thr)))
        (is (= [(-> coll-one
                    (ut/remove-db-only)
                    (update :id str)
                    (update :owner str)
                    (assoc :content []))]
               (m/decode-response-body res-fou)))))))

(deftest coll-remove-user
  (testing "remove user from collection"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          user-one (users/CREATE (db-pop/get-user))
          user-coll (user-collections-assoc/CREATE (g/get-random-user-collections-assoc-without-id (:username user-one) (:id coll-one)))]
      (is (not (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)]))))
      (let [res (rp/collection-id-remove-user (:id coll-one) (:username user-one))]
        (is (= 200 (:status res)))
        (is (= '() (user-collections-assoc/READ-BY-IDS [(:id coll-one) (:username user-one)])))))))

(deftest coll-add-crse
  (testing "add course to collection"
    (let [coll-one (db-pop/add-collection)
          crse-one (db-pop/get-course)]
      (is (= '() (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)])))
      (let [res (rp/collection-id-add-course (:id coll-one)
                                             (:department crse-one)
                                             (:catalog-number crse-one)
                                             (:section-number crse-one))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (assoc crse-one :id id)
                 (ut/remove-db-only (courses/READ id))))
          (is (= 1
                 (count (map ut/remove-db-only (collection-courses-assoc/READ-BY-IDS [(:id coll-one) id]))))))))))

(deftest coll-remove-crse
  (testing "remove course from collection"
    (let [coll-one (collections/CREATE (db-pop/get-collection))
          crse-one (courses/CREATE (db-pop/get-course))
          crse-coll (collection-courses-assoc/CREATE (g/get-random-collection-courses-assoc-without-id (:id coll-one) (:id crse-one)))]
      (is (not (= '() (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)]))))
      (let [res (rp/collection-id-remove-course (:id coll-one) (:id crse-one))]
        (is (= 200 (:status res)))
        (is (= '() (collection-courses-assoc/READ-BY-IDS [(:id coll-one) (:id crse-one)])))))))


(deftest coll-all-users
  (testing "find all users by collection"
    (let [user-one (db-pop/add-user)
          user-two (db-pop/add-user)
          coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          ; Connect one-one, two-two
          user-coll-one (db-pop/add-user-coll-assoc (:username user-one) (:id coll-one))
          user-coll-two (db-pop/add-user-coll-assoc (:username user-two) (:id coll-two))
          res-one (rp/collection-id-users (:id coll-one))
          res-two (rp/collection-id-users (:id coll-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> user-one
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-one)) :account-role (:account-role user-coll-one)})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> user-two
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-two)) :account-role (:account-role user-coll-two)})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))

(deftest coll-all-conts
  (testing "find all contents by collection"
    (let [cont-one (db-pop/add-content)
          coll-id (:collection-id cont-one)
          res (rp/collection-id-contents coll-id)]
      (is (= 200 (:status res)))
      (is (= (-> cont-one
                 (update :id str)
                 (update :collection-id str)
                 (update :resource-id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest coll-all-crses
  (testing "find all courses by collection"
    (let [crse-one (db-pop/add-course)
          crse-two (db-pop/add-course)
          coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          ; Connect one-one, two-two
          crse-coll-one (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-coll-two (db-pop/add-coll-crse-assoc (:id coll-two) (:id crse-two))
          res-one (rp/collection-id-courses (:id coll-one))
          res-two (rp/collection-id-courses (:id coll-two))]
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= (-> crse-one
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-one))})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-one))))
      (is (= (-> crse-two
                 (update :id str)
                 ;(into {:collection-id (str (:id coll-two))})
                 (list))
             (map ut/remove-db-only (m/decode-response-body res-two)))))))
