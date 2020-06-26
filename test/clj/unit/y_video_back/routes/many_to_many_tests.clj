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
      [y-video-back.db.resource-files-assoc :as resource-files-assoc]
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
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-cont-two (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-cont-thr (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
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
  (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-cont-one)))))
  (def test-content-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-two) (:id test-cont-two)))))
  (def test-content-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-thr) (:id test-cont-thr)))))
  (def test-coll-crse-one (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                                                                 :course-id (:id test-crse-one)})))
  (def test-coll-crse-two (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-two)
                                                                                 :course-id (:id test-crse-two)})))
  (def test-coll-crse-thr (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-thr)
                                                                                 :course-id (:id test-crse-thr)})))

  (def test-cont-file-one (ut/under-to-hyphen (resource-files-assoc/CREATE {:resource-id (:id test-cont-one)
                                                                           :file-id (:id test-file-one)})))
  (def test-cont-file-two (ut/under-to-hyphen (resource-files-assoc/CREATE {:resource-id (:id test-cont-two)
                                                                           :file-id (:id test-file-two)})))
  (def test-cont-file-thr (ut/under-to-hyphen (resource-files-assoc/CREATE {:resource-id (:id test-cont-thr)
                                                                           :file-id (:id test-file-thr)})))





  (mount.core/start #'y-video-back.handler/app))

(comment (deftest get-all-child-ids)
  (testing "get-all-child-ids test"
    ; TA for test-coll-two
    (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                    :collection-id (:id test-coll-two)
                                    :account-role 1})
    ()
    (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-cont-one)))))

    (is (= (set [(:id test-user-one)
                 (:id test-crse-one)
                 (:id test-coll-one)
                 (:id test-coll-two)
                 (:id test-cont-one)
                 (:id test-file-one)
                 (:id test-cont-two)
                 (:id test-file-two)
                 (:id test-word-one)
                 (:id test-content-one)])
           (dbu/get-all-child-ids (:id test-user-one))))))
(comment (deftest get-all-child-ids-roles)
  (testing "get-all-child-ids with roles"
    ; TA for test-coll-two
    (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                    :collection-id (:id test-coll-two)
                                    :account-role 1})
    (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-cont-one)))))

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



(deftest test-content
  (testing "add resource to collection (i.e. connect via content)"
    (let [res (rp/collection-id-add-resource (:id test-coll-one) (:id test-cont-two))]
      (is (= 200 (:status res)))
      (let [ann-id (:id (m/decode-response-body res))]
        (is (= {:id ann-id
                :collection-id (:id test-coll-one)
                :resource-id (:id test-cont-two)
                :metadata ""}
               (-> (contents/READ-BY-IDS [(:id test-coll-one) (:id test-cont-two)])
                   (first)
                   (ut/remove-db-only)
                   (update :id str)))))))
  (testing "remove resource from collection (i.e. delete content)"
    (let [res (rp/collection-id-remove-resource (:id test-coll-two) (:id test-cont-two))]
      (is (= 200 (:status res)))
      (is (= '()
             (contents/READ-BY-IDS [(:id test-coll-two) (:id test-cont-two)])))))
  (testing "find all collections by resource"
    (let [res (rp/resource-id-collections (:id test-cont-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-coll-thr
                 (update :id str)
                 (update :owner str)
                 (into {:resource-id (str (:id test-cont-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all resources by collection"
    (let [res (rp/collection-id-resources (:id test-coll-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-cont-thr
                 (update :id str)
                 (into {:collection-id (str (:id test-coll-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

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

(deftest test-cont-file-assoc
  (testing "connect resource and file"
    (let [new-resource-file-assoc (g/get-random-resource-files-assoc-without-id (:id test-cont-one) (:id test-file-two))]
      (let [res (rp/resource-id-add-file (:resource-id new-resource-file-assoc) (:file-id new-resource-file-assoc))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list (into new-resource-file-assoc {:id id}))
                 (map ut/remove-db-only (resource-files-assoc/READ-BY-IDS [(:resource-id new-resource-file-assoc) (:file-id new-resource-file-assoc)])))))))
    (testing "disconnect resource and file")
    (let [res (rp/resource-id-remove-file (:id test-cont-one) (:id test-file-one))]
      (is (= 200 (:status res)))
      (is (= '()
             (resource-files-assoc/READ-BY-IDS [(:id test-cont-one) (:id test-file-one)])))))
  (testing "find all resources by file"
    (let [res (rp/file-id-resources (:id test-file-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-cont-thr
                 (update :id str)
                 (into {:file-id (str (:id test-file-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res))))))
  (testing "find all files by resource"
    (let [res (rp/resource-id-files (:id test-cont-thr))]
      (is (= 200 (:status res)))
      (is (= (-> test-file-thr
                 (update :id str)
                 (into {:resource-id (str (:id test-cont-thr))})
                 (ut/remove-db-only)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))
