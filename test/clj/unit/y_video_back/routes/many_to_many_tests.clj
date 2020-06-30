; Operations testing in this file:
; 1. connect x and y
; 2. disconnect x and y
; 3. find all x connected to y
; 4. find all y connected to x

(ns y-video-back.routes.many-to-many-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy :as rp]
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
      [y-video-back.routes.service-handlers.db-utils :as dbu]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-fou (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-two)}))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-thr)}))))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-rsrc-two (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-rsrc-thr (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-one)))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-two)))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-thr)))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-thr)))))

  (def test-user-coll-one (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                                                              :collection-id (:id test-coll-one)
                                                                              :account-role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-two)
                                                                              :collection-id (:id test-coll-two)
                                                                              :account-role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-thr)
                                                                              :collection-id (:id test-coll-thr)
                                                                              :account-role 0})))
  (def test-user-crse-one (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-one)
                                                                          :course-id (:id test-crse-one)
                                                                          :account-role 0})))
  (def test-user-crse-two (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-two)
                                                                          :course-id (:id test-crse-two)
                                                                          :account-role 0})))
  (def test-user-crse-thr (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-thr)
                                                                          :course-id (:id test-crse-thr)
                                                                          :account-role 0})))
  (def test-user-crse-fou (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-fou)
                                                                          :course-id (:id test-crse-one)
                                                                          :account-role 2})))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one)))))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-two) (:id test-rsrc-two)))))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-thr) (:id test-rsrc-thr)))))
  (def test-coll-crse-one (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                                                                 :course-id (:id test-crse-one)})))
  (def test-coll-crse-two (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-two)
                                                                                 :course-id (:id test-crse-two)})))
  (def test-coll-crse-thr (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-thr)
                                                                                 :course-id (:id test-crse-thr)})))



  (mount.core/start #'y-video-back.handler/app))

(comment (deftest get-all-child-ids)
  (testing "get-all-child-ids test"
    ; TA for test-coll-two
    (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                    :collection-id (:id test-coll-two)
                                    :account-role 1})
    ()
    (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one)))))

    (is (= (set [(:id test-user-one)
                 (:id test-crse-one)
                 (:id test-coll-one)
                 (:id test-coll-two)
                 (:id test-rsrc-one)
                 (:id test-file-one)
                 (:id test-rsrc-two)
                 (:id test-file-two)
                 (:id test-word-one)
                 (:id test-cont-one)])
           (dbu/get-all-child-ids (:id test-user-one))))))
(comment (deftest get-all-child-ids-roles)
  (testing "get-all-child-ids with roles"
    ; TA for test-coll-two
    (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                    :collection-id (:id test-coll-two)
                                    :account-role 1})
    (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one)))))

    (is (= (set [(:id test-user-fou)
                 (:id test-crse-one)])
           (dbu/get-all-child-ids (:id test-user-fou) 0)))))


(deftest test-user-collection-assoc
  (testing "connect user and collection"
    (let [new-user-coll-assoc (g/get-random-user-collections-assoc-without-id (:id test-user-one) (:id test-coll-two))]
      (let [res (rp/collection-id-add-user (:collection-id new-user-coll-assoc) (:id test-user-one) (:account-role new-user-coll-assoc))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list (into new-user-coll-assoc {:id id}))
                 (map ut/remove-db-only (user-collections-assoc/READ-BY-IDS [(:collection-id new-user-coll-assoc) (:user-id new-user-coll-assoc)]))))))))
  (testing "disconnect user and collection"
    (let [res (rp/collection-id-remove-user (:id test-coll-one) (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= '()
             (user-collections-assoc/READ-BY-IDS [(:id test-coll-one) (:id test-user-one)])))))
  (testing "find all users by collection"
    (let [res (rp/collection-id-users (:id test-coll-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-user-thr
                 (update :id str)
                 (into {:collection-id (str (:id test-coll-thr)) :account-role 0})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all collections by user"
    (let [res (rp/user-id-collections (:id test-user-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-coll-thr
                 (update :id str)
                 (update :owner str)
                 (into {:user-id (str (:id test-user-thr)) :account-role 0})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))
(deftest test-user-course-assoc
  (testing "connect user and course"
    (let [new-user-crse-assoc (g/get-random-user-courses-assoc-without-id (:id test-user-one) (:id test-crse-two))]
      (let [res (rp/course-id-add-user (:course-id new-user-crse-assoc) (:id test-user-one) (:account-role new-user-crse-assoc))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list (into new-user-crse-assoc {:id id}))
                 (map ut/remove-db-only (user-courses-assoc/READ-BY-IDS [(:course-id new-user-crse-assoc) (:user-id new-user-crse-assoc)]))))))))
  (testing "disconnect user and course"
    (let [res (rp/course-id-remove-user (:id test-crse-one) (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= '()
             (user-courses-assoc/READ-BY-IDS [(:id test-crse-one) (:id test-user-one)])))))
  (testing "find all users by course"
    (let [res (rp/course-id-users (:id test-crse-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-user-thr
                 (update :id str)
                 (into {:course-id (str (:id test-crse-thr)) :account-role 0})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all courses by user"
    (let [res (rp/user-id-courses (:id test-user-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-crse-thr
                 (update :id str)
                 (into {:user-id (str (:id test-user-thr)) :account-role 0})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest test-cont-collection
  (testing "find all contents by collection"
    (let [new-user (users/CREATE (g/get-random-user-without-id))
          new-coll (g/get-random-collection-without-id (:id new-user))
          new-coll-id (:id (collections/CREATE new-coll));))
          empty-coll (collections/CREATE (g/get-random-collection-without-id (:id new-user)))
          cont-one (g/get-random-content-without-id new-coll-id ut/nil-uuid)
          cont-two (g/get-random-content-without-id new-coll-id ut/nil-uuid)
          cont-one-add (contents/CREATE cont-one)
          cont-two-add (contents/CREATE cont-two)
          res (rp/collection-id-contents new-coll-id)
          empty-res (rp/collection-id-contents (:id empty-coll))]
      (is (= 200 (:status res)))
      (is (= 200 (:status empty-res)))
      (is (= (map #(-> %
                       (update :id str)
                       (update :resource-id str)
                       (update :collection-id str))
                  [(into cont-one {:id (:id cont-one-add)})
                   (into cont-two {:id (:id cont-two-add)})])
             (map #(-> %
                       (update :resource-id str)) ; nil -> ""
                  (m/decode-response-body res))))
      (is (= '()
             (m/decode-response-body empty-res)))
      (is true))))

(deftest test-collection-resource
  (testing "find all collections by resource"
    (let [res (rp/resource-id-collections (:id test-rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (map #(-> %
                       (ut/remove-db-only)
                       (update :id str)
                       (update :owner str))
                  [test-coll-one])
             (m/decode-response-body res))))))

(deftest test-content-resource
  (testing "find all contents by resource"
    (let [res (rp/resource-id-contents (:id test-rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (map #(-> %
                       (ut/remove-db-only)
                       (update :id str)
                       (update :collection-id str)
                       (update :resource-id str))
                  [test-cont-one])
             (m/decode-response-body res))))))




(deftest test-coll-course-assoc
  (testing "connect collection and course"
    (let [new-collection-course-assoc (g/get-random-collection-courses-assoc-without-id (:id test-coll-one) (:id test-crse-two))]
      (let [res (rp/collection-id-add-course (:collection-id new-collection-course-assoc) (:course-id new-collection-course-assoc))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list (into new-collection-course-assoc {:id id}))
                 (map ut/remove-db-only (collection-courses-assoc/READ-BY-IDS [(:collection-id new-collection-course-assoc) (:course-id new-collection-course-assoc)]))))))))
  (testing "disconnect collection and course"
    (let [res (rp/collection-id-remove-course (:id test-coll-one) (:id test-crse-one))]
      (is (= 200 (:status res)))
      (is (= '()
             (collection-courses-assoc/READ-BY-IDS [(:id test-coll-one) (:id test-crse-one)])))))
  (testing "find all collections by course"
    (let [res (rp/course-id-collections (:id test-crse-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-coll-thr
                 (update :id str)
                 (update :owner str)
                 (into {:course-id (str (:id test-crse-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all courses by collection"
    (let [res (rp/collection-id-courses (:id test-coll-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-crse-thr
                 (update :id str)
                 (into {:collection-id (str (:id test-coll-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest test-rsrc-file
  (testing "find files by resource (1 file)"
    (let [res (rp/resource-id-files (:id test-rsrc-one))]
      (is (= 200 (:status res)))
      (is (= [(-> test-file-one
                  (ut/remove-db-only)
                  (update :id str)
                  (update :resource-id str))]
             (m/decode-response-body res)))))
  (testing "find files by resource (3 files)"
    (let [new-file-one (g/get-random-file-without-id (:id test-rsrc-one))
          new-file-two (g/get-random-file-without-id (:id test-rsrc-one))
          file-one-res (files/CREATE new-file-one)
          file-two-res (files/CREATE new-file-two)
          res (rp/resource-id-files (:id test-rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (map #(-> %
                       (ut/remove-db-only)
                       (update :id str)
                       (update :resource-id str))
                  [test-file-one
                   (assoc new-file-one :id (:id file-one-res))
                   (assoc new-file-two :id (:id file-two-res))])
             (m/decode-response-body res))))))
