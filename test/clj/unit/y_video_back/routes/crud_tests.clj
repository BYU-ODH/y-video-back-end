(ns y-video-back.routes.crud-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model_generator :as g]
      [y-video-back.utils.route_proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.annotations :as annotations]
      [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.content-files-assoc :as content_files_assoc]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user_collections_assoc]
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
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get_random_user_without_id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-user-coll-one (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-one)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-two)
                                                                              :collection_id (:id test-coll-two)
                                                                              :account_role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id test-user-thr)
                                                                              :collection_id (:id test-coll-thr)
                                                                              :account_role 0})))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get_random_content_without_id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get_random_file_without_id))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get_random_file_without_id))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get_random_file_without_id))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get_random_word_without_id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get_random_word_without_id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get_random_word_without_id (:id test-user-thr)))))
  (def test-annotation-one (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-one) (:id test-cont-one)))))
  (def test-annotation-two (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-two) (:id test-cont-two)))))
  (def test-annotation-thr (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-thr) (:id test-cont-thr)))))
  (mount.core/start #'y-video-back.handler/app))

(deftest test-user
  (testing "user CREATE"
    (let [new_user (g/get_random_user_without_id)]
      (let [res (rp/user-post new_user)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_user {:id id}) (ut/remove-db-only (users/READ id))))))))
  (testing "user READ"
    (let [res (rp/user-id-get (:id test-user-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-user-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "user UPDATE"
    (let [new_user (g/get_random_user_without_id)]
      (let [res (rp/user-id-patch (:id test-user-one) new_user)]
        (is (= 200 (:status res)))
        (is (= (into new_user {:id (:id test-user-one)}) (ut/remove-db-only (users/READ (:id test-user-one))))))))
  (testing "user DELETE"
    (let [res (rp/user-id-delete (:id test-user-two))]
      (is (= 200 (:status res)))
      (is (= nil (users/READ (:id test-user-two)))))))

(deftest test-coll
  (testing "coll CREATE"
    (let [new_coll (g/get_random_collection_without_id)]
      (let [res (rp/collection-post new_coll (:id test-user-one))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_coll {:id id}) (ut/remove-db-only (collections/READ id))))
          (is (= (list {:user-id (:id test-user-one) :account-role 0 :collection-id id})
                 (map #(ut/remove-db-only (dissoc % :id)) (user_collections_assoc/READ-BY-COLLECTION id))))))))
  (testing "coll READ"
    (let [res (rp/collection-id-get (:id test-coll-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-coll-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "coll UPDATE"
    (let [new_coll (g/get_random_collection_without_id)]
      (let [res (rp/collection-id-patch (:id test-coll-one) new_coll)]
        (is (= 200 (:status res)))
        (is (= (into new_coll {:id (:id test-coll-one)}) (ut/remove-db-only (collections/READ (:id test-coll-one))))))))
  (testing "coll DELETE"
    (let [res (rp/collection-id-delete (:id test-coll-two))]
      (is (= 200 (:status res)))
      (is (= nil (collections/READ (:id test-coll-two)))))))

(deftest test-cont
  (testing "cont CREATE"
    (let [new_cont (g/get_random_content_without_id)]
      (let [res (rp/content-post new_cont)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_cont {:id id}) (ut/remove-db-only (contents/READ id))))))))
  (testing "cont READ"
    (let [res (rp/content-id-get (:id test-cont-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-cont-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "cont UPDATE"
    (let [new_cont (g/get_random_content_without_id)]
      (let [res (rp/content-id-patch (:id test-cont-one) new_cont)]
        (is (= 200 (:status res)))
        (is (= (into new_cont {:id (:id test-cont-one)}) (ut/remove-db-only (contents/READ (:id test-cont-one))))))))
  (testing "cont DELETE"
    (let [res (rp/content-id-delete (:id test-cont-two))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id test-cont-two)))))))

(deftest test-crse
  (testing "crse CREATE"
    (let [new_crse (g/get_random_course_without_id)]
      (let [res (rp/course-post new_crse)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_crse {:id id}) (ut/remove-db-only (courses/READ id))))))))
  (testing "crse READ"
    (let [res (rp/course-id-get (:id test-crse-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-crse-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "crse UPDATE"
    (let [new_crse (g/get_random_course_without_id)]
      (let [res (rp/course-id-patch (:id test-crse-one) new_crse)]
        (is (= 200 (:status res)))
        (is (= (into new_crse {:id (:id test-crse-one)}) (ut/remove-db-only (courses/READ (:id test-crse-one))))))))
  (testing "crse DELETE"
    (let [res (rp/course-id-delete (:id test-crse-two))]
      (is (= 200 (:status res)))
      (is (= nil (courses/READ (:id test-crse-two)))))))

(deftest test-file
  (testing "file CREATE"
    (let [new_file (g/get_random_file_without_id)]
      (let [res (rp/file-post new_file)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_file {:id id}) (ut/remove-db-only (files/READ id))))))))
  (testing "file READ"
    (let [res (rp/file-id-get (:id test-file-one))]
      (is (= 200 (:status res)))
      (is (= (update (ut/remove-db-only test-file-one) :id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "file UPDATE"
    (let [new_file (g/get_random_file_without_id)]
      (let [res (rp/file-id-patch (:id test-file-one) new_file)]
        (is (= 200 (:status res)))
        (is (= (into new_file {:id (:id test-file-one)}) (ut/remove-db-only (files/READ (:id test-file-one))))))))
  (testing "file DELETE"
    (let [res (rp/file-id-delete (:id test-file-two))]
      (is (= 200 (:status res)))
      (is (= nil (files/READ (:id test-file-two)))))))

(deftest test-word
  (testing "word CREATE"
    (let [new_word (g/get_random_word_without_id (:id test-user-one))]
      (let [res (rp/word-post new_word)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_word {:id id}) (ut/remove-db-only (words/READ id))))))))
  (testing "word READ"
    (let [res (rp/word-id-get (:id test-word-one))]
      (is (= 200 (:status res)))
      (is (= (update (update (ut/remove-db-only test-word-one) :id str) :user-id str)
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "word UPDATE"
    (let [new_word (g/get_random_word_without_id (:id test-user-one))]
      (let [res (rp/word-id-patch (:id test-word-one) new_word)]
        (is (= 200 (:status res)))
        (is (= (into new_word {:id (:id test-word-one)}) (ut/remove-db-only (words/READ (:id test-word-one))))))))
  (testing "word DELETE"
    (let [res (rp/word-id-delete (:id test-word-two))]
      (is (= 200 (:status res)))
      (is (= nil (words/READ (:id test-word-two)))))))

(deftest test-annotation
  (testing "annotation CREATE"
    (let [new_annotation (g/get_random_annotation_without_id (:id test-coll-one) (:id test-cont-two))]
      (let [res (rp/annotation-post new_annotation)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into new_annotation {:id id}) (ut/remove-db-only (annotations/READ id))))))))
  (testing "annotation read by ids (one)"
    (let [res (rp/annotation-get-by-ids (:collection-id test-annotation-one)
                                        (:content-id test-annotation-one))]
      (is (= 200 (:status res)))
      (is (= [(-> test-annotation-one
                  (ut/remove-db-only)
                  (update :id str)
                  (update :collection-id str)
                  (update :content-id str))]
             (map ut/remove-db-only (m/decode-response-body res))))))

  (testing "annotation read by ids (multiple)"
    (let [new-annotation (ut/under-to-hyphen (annotations/CREATE (g/get_random_annotation_without_id (:id test-coll-two) (:id test-cont-two))))]
      (let [res (rp/annotation-get-by-ids (:collection-id test-annotation-two)
                                          (:content-id test-annotation-two))]
        (is (= 200 (:status res)))
        (is (= [(-> test-annotation-two
                    (ut/remove-db-only)
                    (update :id str)
                    (update :collection-id str)
                    (update :content-id str))
                (-> new-annotation
                            (ut/remove-db-only)
                            (update :id str)
                            (update :collection-id str)
                            (update :content-id str))]
               (map ut/remove-db-only (m/decode-response-body res)))))))
  (testing "annotation read by ids (none)"
    (let [res (rp/annotation-get-by-ids (:id test-coll-one)
                                        (:id test-cont-thr))]
      (is (= 404 (:status res))))

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
    (let [new_annotation (g/get_random_annotation_without_id (:collection-id test-annotation-one) (:content-id test-annotation-one))]
      (let [res (rp/annotation-id-patch (:id test-annotation-one) new_annotation)]
        (is (= 200 (:status res)))
        (is (= (into new_annotation {:id (:id test-annotation-one)}) (ut/remove-db-only (annotations/READ (:id test-annotation-one))))))))
  (testing "annotation DELETE"
    (let [res (rp/annotation-id-delete (:id test-annotation-two))]
      (is (= 200 (:status res)))
      (is (= nil (annotations/READ (:id test-annotation-two)))))))
