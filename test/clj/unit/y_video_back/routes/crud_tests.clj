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
      [y-video-back.db.annotations :as annotations]
      [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.content-files-assoc :as content-files-assoc]
      [y-video-back.db.contents :as contents]
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
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-thr)))))
  (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-one) (:id test-cont-one)))))
  (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-two) (:id test-cont-two)))))
  (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get-random-annotation-without-id (:id test-coll-thr) (:id test-cont-thr)))))
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
  (testing "update collection to self"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-one-res (collections/CREATE coll-one)
          res (rp/collection-id-patch (:id coll-one-res) coll-one)]
      (is (= 200 (:status res)))
      (is (= (into coll-one {:id (:id coll-one-res)}) (ut/remove-db-only (collections/READ (:id coll-one-res)))))))
  (testing "update collection name and owner to own name and owner"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-one-res (collections/CREATE coll-one)
          res (rp/collection-id-patch (:id coll-one-res) {:collection-name (:collection-name coll-one)
                                                          :owner (:owner coll-one)})]
      (is (= 200 (:status res)))
      (is (= (into coll-one {:id (:id coll-one-res)}) (ut/remove-db-only (collections/READ (:id coll-one-res)))))))
  (testing "update collection owner to own owner"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-one-res (collections/CREATE coll-one)
          res (rp/collection-id-patch (:id coll-one-res) {:owner (:owner coll-one)})]
      (is (= 200 (:status res)))
      (is (= (into coll-one {:id (:id coll-one-res)}) (ut/remove-db-only (collections/READ (:id coll-one-res)))))))
  (testing "update collection name to own name"
    (let [coll-one (into (g/get-random-collection-without-id-no-owner)
                         {:owner (:id test-user-one)})
          coll-one-res (collections/CREATE coll-one)
          res (rp/collection-id-patch (:id coll-one-res) {:collection-name (:collection-name coll-one)})]
      (is (= 200 (:status res)))
      (is (= (into coll-one {:id (:id coll-one-res)}) (ut/remove-db-only (collections/READ (:id coll-one-res)))))))
  (testing "coll DELETE"
    (let [res (rp/collection-id-delete (:id test-coll-two))]
      (is (= 200 (:status res)))
      (is (= nil (collections/READ (:id test-coll-two)))))))

(deftest test-cont
  (testing "cont CREATE"
    (let [new-cont (g/get-random-content-without-id)]
      (let [res (rp/content-post new-cont)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-cont {:id id}) (ut/remove-db-only (contents/READ id))))))))
  (testing "cont CREATE duplicate content"
    (let [new-cont (g/get-random-content-without-id)]
      (let [res (rp/content-post new-cont)
            res-dup (rp/content-post new-cont)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-cont {:id id}) (ut/remove-db-only (contents/READ id)))))
        (is (= 200 (:status res-dup)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res-dup)))]
          (is (= (into new-cont {:id id}) (ut/remove-db-only (contents/READ id))))))))
  (testing "cont READ"
    (let [res (rp/content-id-get (:id test-cont-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-cont-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "cont UPDATE"
    (let [new-cont (g/get-random-content-without-id)]
      (let [res (rp/content-id-patch (:id test-cont-one) new-cont)]
        (is (= 200 (:status res)))
        (is (= (into new-cont {:id (:id test-cont-one)}) (ut/remove-db-only (contents/READ (:id test-cont-one))))))))
  (testing "update content to taken name (all fields)"
    (let [cont-one (g/get-random-content-without-id)
          cont-two (g/get-random-content-without-id)
          cont-one-res (contents/CREATE cont-one)
          cont-two-res (contents/CREATE cont-two)
          res (rp/content-id-patch (:id cont-two-res) cont-one)]
      (is (= 200 (:status res)))
      (is (= (into cont-one {:id (:id cont-two-res)}) (ut/remove-db-only (contents/READ (:id cont-two-res)))))))
  (testing "update content to taken name (name only)"
    (let [cont-one (g/get-random-content-without-id)
          cont-two (g/get-random-content-without-id)
          cont-one-res (contents/CREATE cont-one)
          cont-two-res (contents/CREATE cont-two)
          res (rp/content-id-patch (:id cont-two-res) {:content-name (:content-name cont-one)})]
      (is (= 200 (:status res)))
      (is (= (-> cont-two
               (into {:id (:id cont-two-res)})
               (dissoc :content-name)
               (into {:content-name (:content-name cont-one)}))
             (ut/remove-db-only (contents/READ (:id cont-two-res)))))))
  (testing "cont UPDATE to self"
    (let [new-cont (g/get-random-content-without-id)]
      (let [res (rp/content-id-patch (:id test-cont-one) new-cont)]
        (is (= 200 (:status res)))
        (is (= (into new-cont {:id (:id test-cont-one)}) (ut/remove-db-only (contents/READ (:id test-cont-one))))))))
  (testing "cont DELETE"
    (let [res (rp/content-id-delete (:id test-cont-two))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id test-cont-two)))))))

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
    (let [new-file (g/get-random-file-without-id)]
      (let [res (rp/file-post new-file)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-file {:id id}) (ut/remove-db-only (files/READ id))))))))
  (testing "file READ"
    (let [res (rp/file-id-get (:id test-file-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-file-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "file UPDATE"
    (let [new-file (g/get-random-file-without-id)]
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

(deftest test-annotation
  (testing "annotation CREATE"
    (let [new-annotation (g/get-random-annotation-without-id (:id test-coll-one) (:id test-cont-two))]
      (let [res (rp/annotation-post new-annotation)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new-annotation {:id id}) (ut/remove-db-only (annotations/READ id))))))))
  (testing "annotation read by ids"
    (let [res (rp/annotation-get-by-ids (:collection-id test-annotation-one)
                                        (:content-id test-annotation-one))]
      (is (= 200 (:status res)))
      (is (= [(-> test-annotation-one
                  (ut/remove-db-only)
                  (update :id str)
                  (update :collection-id str)
                  (update :content-id str))]
             (map ut/remove-db-only (m/decode-response-body res)))))

    (testing "annotation READ")
    (let [res (rp/annotation-id-get (:id test-annotation-one))]
      (is (= 200 (:status res)))
      (is (= (-> test-annotation-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :collection-id str)
                 (update :content-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "annotation UPDATE"
    (let [new-annotation (g/get-random-annotation-without-id (:collection-id test-annotation-one) (:content-id test-annotation-one))]
      (let [res (rp/annotation-id-patch (:id test-annotation-one) new-annotation)]
        (is (= 200 (:status res)))
        (is (= (into new-annotation {:id (:id test-annotation-one)}) (ut/remove-db-only (annotations/READ (:id test-annotation-one))))))))
  (testing "annotation DELETE"
    (let [res (rp/annotation-id-delete (:id test-annotation-two))]
      (is (= 200 (:status res)))
      (is (= nil (annotations/READ (:id test-annotation-two)))))))
