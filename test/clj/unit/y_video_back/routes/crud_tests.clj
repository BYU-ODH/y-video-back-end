(ns y-video-back.routes.crud-tests
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
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

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
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-two)}))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-thr)}))))
  (def test-user-coll-one (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                                                              :collection-id (:id test-coll-one)
                                                                              :account-role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-two)
                                                                              :collection-id (:id test-coll-two)
                                                                              :account-role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-thr)
                                                                              :collection-id (:id test-coll-thr)
                                                                              :account-role 0})))
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
  (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one)))))
  (def test-content-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-two) (:id test-rsrc-two)))))
  (def test-content-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-thr) (:id test-rsrc-thr)))))
  (mount.core/start #'y-video-back.handler/app))

(deftest test-user
  (testing "user CREATE"
    (let [new-user (g/get-random-user-without-id)]
      (let [res (rp/user-post new-user)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-user {:id id}) (ut/remove-db-only (users/READ id))))))))
  (testing "user READ"
    (let [res (rp/user-id-get (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-user-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "user UPDATE"
    (let [new-user (g/get-random-user-without-id)]
      (let [res (rp/user-id-patch (:id test-user-one) new-user)]
        (is (= 200 (:status res)))
        (is (= (into new-user {:id (:id test-user-one)}) (ut/remove-db-only (users/READ (:id test-user-one))))))))
  (testing "user DELETE"
    (let [res (rp/user-id-delete (:id test-user-two))]
      (is (= 200 (:status res)))
      (is (= nil (users/READ (:id test-user-two)))))))

(deftest test-coll
  (testing "coll CREATE"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})]
      (let [res (rp/collection-post new-coll)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-coll {:id id}) (ut/remove-db-only (collections/READ id))))))))
          ;(is (= (list {:user-id (:id test-user-one) :account-role 0 :collection-id id})
          ;       (map #(ut/remove-db-only (dissoc % :id)) (user-collections-assoc/READ-BY-COLLECTION id))))))))
  (testing "coll READ"
    (let [res (rp/collection-id-get (:id test-coll-one))]
      (is (= 200 (:status res)))
      (is (= (-> (ut/remove-db-only test-coll-one)
                 (update :id str)
                 (update :owner str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "coll UPDATE"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})]
      (let [res (rp/collection-id-patch (:id test-coll-one) new-coll)]
        (is (= 200 (:status res)))
        (is (= (into new-coll {:id (:id test-coll-one)}) (ut/remove-db-only (collections/READ (:id test-coll-one))))))))
  (testing "coll DELETE"
    (let [res (rp/collection-id-delete (:id test-coll-two))]
      (is (= 200 (:status res)))
      (is (= nil (collections/READ (:id test-coll-two)))))))

(deftest test-rsrc
  (testing "rsrc CREATE"
    (let [new-rsrc (g/get-random-resource-without-id)]
      (let [res (rp/resource-post new-rsrc)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-rsrc {:id id}) (ut/remove-db-only (resources/READ id))))))))
  (testing "rsrc READ"
    (let [res (rp/resource-id-get (:id test-rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-rsrc-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "rsrc UPDATE"
    (let [new-rsrc (g/get-random-resource-without-id)]
      (let [res (rp/resource-id-patch (:id test-rsrc-one) new-rsrc)]
        (is (= 200 (:status res)))
        (is (= (into new-rsrc {:id (:id test-rsrc-one)}) (ut/remove-db-only (resources/READ (:id test-rsrc-one))))))))
  (testing "rsrc DELETE"
    (let [res (rp/resource-id-delete (:id test-rsrc-two))]
      (is (= 200 (:status res)))
      (is (= nil (resources/READ (:id test-rsrc-two)))))))

(deftest test-crse
  (testing "crse CREATE"
    (let [new-crse (g/get-random-course-without-id)]
      (let [res (rp/course-post new-crse)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-crse {:id id}) (ut/remove-db-only (courses/READ id))))))))
  (testing "crse READ"
    (let [res (rp/course-id-get (:id test-crse-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-crse-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "crse UPDATE"
    (let [new-crse (g/get-random-course-without-id)]
      (let [res (rp/course-id-patch (:id test-crse-one) new-crse)]
        (is (= 200 (:status res)))
        (is (= (into new-crse {:id (:id test-crse-one)}) (ut/remove-db-only (courses/READ (:id test-crse-one))))))))
  (testing "crse DELETE"
    (let [res (rp/course-id-delete (:id test-crse-two))]
      (is (= 200 (:status res)))
      (is (= nil (courses/READ (:id test-crse-two)))))))

(deftest test-file
  (testing "file CREATE"
    (let [new-file (g/get-random-file-without-id (:id test-rsrc-one))]
      (let [res (rp/file-post new-file)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-file {:id id}) (ut/remove-db-only (files/READ id))))))))
  (testing "file READ"
    (let [res (rp/file-id-get (:id test-file-one))]
      (is (= 200 (:status res)))
      (is (= (-> (ut/remove-db-only test-file-one)
                 (update :id str)
                 (update :resource-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "file UPDATE"
    (let [new-file (g/get-random-file-without-id (:id test-rsrc-two))]
      (let [res (rp/file-id-patch (:id test-file-one) new-file)]
        (is (= 200 (:status res)))
        (is (= (into new-file {:id (:id test-file-one)}) (ut/remove-db-only (files/READ (:id test-file-one))))))))
  (testing "file DELETE"
    (let [res (rp/file-id-delete (:id test-file-two))]
      (is (= 200 (:status res)))
      (is (= nil (files/READ (:id test-file-two)))))))

(deftest test-word
  (testing "word CREATE"
    (let [new-word (g/get-random-word-without-id (:id test-user-one))]
      (let [res (rp/word-post new-word)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-word {:id id}) (ut/remove-db-only (words/READ id))))))))
  (testing "word READ"
    (let [res (rp/word-id-get (:id test-word-one))]
      (is (= 200 (:status res)))
      (is (= (update (update (ut/remove-db-only test-word-one) :id str) :user-id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "word UPDATE"
    (let [new-word (g/get-random-word-without-id (:id test-user-one))]
      (let [res (rp/word-id-patch (:id test-word-one) new-word)]
        (is (= 200 (:status res)))
        (is (= (into new-word {:id (:id test-word-one)}) (ut/remove-db-only (words/READ (:id test-word-one))))))))
  (testing "word DELETE"
    (let [res (rp/word-id-delete (:id test-word-two))]
      (is (= 200 (:status res)))
      (is (= nil (words/READ (:id test-word-two)))))))

(deftest test-content
  (testing "content CREATE"
    (let [new-content (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-two))]
      (let [res (rp/content-post new-content)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-content {:id id}) (ut/remove-db-only (contents/READ id))))))))
  (testing "content READ")
  (let [res (rp/content-id-get (:id test-content-one))]
    (is (= 200 (:status res)))
    (is (= (-> test-content-one
               (ut/remove-db-only)
               (update :id str)
               (update :collection-id str)
               (update :resource-id str))
           (ut/remove-db-only (m/decode-response-body res)))))
  (testing "content UPDATE"
    (let [new-content (g/get-random-content-without-id (:collection-id test-content-one) (:resource-id test-content-one))]
      (let [res (rp/content-id-patch (:id test-content-one) new-content)]
        (is (= 200 (:status res)))
        (is (= (into new-content {:id (:id test-content-one)}) (ut/remove-db-only (contents/READ (:id test-content-one))))))))
  (testing "content DELETE"
    (let [res (rp/content-id-delete (:id test-content-two))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id test-content-two)))))))
