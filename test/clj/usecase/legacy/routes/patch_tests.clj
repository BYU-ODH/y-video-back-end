; For each model, test:
; 1) update fields one at a time
; 2) update some, but not all, fields at once
; 3) update all fields at once
; 1-3 for updating to self as well

(ns legacy.routes.patch-tests
  (:require
    [clojure.test :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.db.test-util :as tcore]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [legacy.utils.model-generator :as g]
    [legacy.utils.route-proxy.proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.db.contents :as contents]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.resources :as resources]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user-collections-assoc]
    [y-video-back.db.users :as users]
    [y-video-back.db.words :as words]
    [legacy.utils.db-populator :as db-pop]
    [legacy.utils.utils :as ut]))

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
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-two)}))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-thr)}))))
  (def test-user-coll-one (ut/under-to-hyphen (user-collections-assoc/CREATE {:username (:username test-user-one)
                                                                              :collection-id (:id test-coll-one)
                                                                              :account-role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user-collections-assoc/CREATE {:username (:id test-user-two)
                                                                              :collection-id (:username test-coll-two)
                                                                              :account-role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user-collections-assoc/CREATE {:username (:id test-user-thr)
                                                                              :collection-id (:username test-coll-thr)
                                                                              :account-role 0})))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-rsrc-two (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-rsrc-thr (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (db-pop/add-file (:id test-rsrc-one)))
  (def test-file-two (db-pop/add-file (:id test-rsrc-two)))
  (def test-file-thr (db-pop/add-file (:id test-rsrc-thr)))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-thr)))))
  (def test-content-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000")))))
  (def test-content-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-two) (:id test-rsrc-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000")))))
  (def test-content-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-thr) (:id test-rsrc-thr) (ut/to-uuid "00000000-0000-0000-0000-000000000000")))))
  (mount.core/start #'y-video-back.handler/app))

(deftest test-user-patch
  (testing "user fields one at a time"
    (let [new-user (g/get-random-user-without-id)
          id (:id test-user-one)]
      (is (= (ut/remove-db-only test-user-one) (ut/remove-db-only (users/READ id))))
      (doseq [val (seq new-user)]
             [(do
                (let [res (rp/user-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-user) ((get val 0) (users/READ id)))))])
      (is (= (into new-user {:id id}) (ut/remove-db-only (users/READ id))))))
  (testing "user multiple fields at once"
    (let [new-user (g/get-random-user-without-id)
          id (:id test-user-two)]
      (is (= (ut/remove-db-only test-user-two) (ut/remove-db-only (users/READ id))))
      (let [fields-to-change (ut/random-submap new-user)]
        (let [res (rp/user-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-user-two fields-to-change))
               (ut/remove-db-only (users/READ id)))))))
  (testing "user all fields at once"
    (let [new-user (g/get-random-user-without-id)
          id (:id test-user-thr)]
      (is (= (ut/remove-db-only test-user-thr) (ut/remove-db-only (users/READ id))))
      (let [res (rp/user-id-patch id new-user)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-user-thr new-user))
             (ut/remove-db-only (users/READ id))))))
  (testing "user to self, one field at a time"
    (let [new-user (ut/under-to-hyphen (g/get-random-user-without-id))
          add-user (users/CREATE new-user)
          id (:id add-user)]
      (doseq [val (seq new-user)]
             [(do
                (let [res (rp/user-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-user) ((get val 0) (users/READ id)))))])
      (is (= (into new-user {:id id}) (ut/remove-db-only (users/READ id))))))
  (testing "user to self, multiple fields at once"
    (let [new-user (g/get-random-user-without-id)
          add-user (users/CREATE new-user)
          id (:id add-user)]
      (let [fields-to-change (ut/random-submap new-user)]
        (let [res (rp/user-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge new-user fields-to-change {:id id}))
               (ut/remove-db-only (users/READ id)))))))
  (testing "user to self, all fields at once"
    (let [new-user (g/get-random-user-without-id)
          add-user (users/CREATE new-user)
          id (:id add-user)]
      (let [res (rp/user-id-patch id new-user)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge new-user {:id id}))
             (ut/remove-db-only (users/READ id)))))))

(deftest test-coll-patch
  (testing "coll fields one at a time"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})
          id (:id test-coll-one)]
      (is (= (ut/remove-db-only test-coll-one) (ut/remove-db-only (collections/READ id))))
      (doseq [val (seq new-coll)]
             [(do
                (let [res (rp/collection-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-coll) ((get val 0) (collections/READ id)))))])
      (is (= (into new-coll {:id id}) (ut/remove-db-only (collections/READ id))))))
  (testing "coll multiple fields at once"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})
          id (:id test-coll-two)]
      (is (= (ut/remove-db-only test-coll-two) (ut/remove-db-only (collections/READ id))))
      (let [fields-to-change (ut/random-submap new-coll)]
        (let [res (rp/collection-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-coll-two fields-to-change))
               (ut/remove-db-only (collections/READ id)))))))
  (testing "coll all fields at once"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})
          id (:id test-coll-thr)]
      (is (= (ut/remove-db-only test-coll-thr) (ut/remove-db-only (collections/READ id))))
      (let [res (rp/collection-id-patch id new-coll)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-coll-thr new-coll))
             (ut/remove-db-only (collections/READ id))))))
  (testing "coll to self, fields one at a time"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})
          add-coll (collections/CREATE new-coll)
          id (:id add-coll)]
      (doseq [val (seq new-coll)]
             [(do
                (let [res (rp/collection-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-coll) ((get val 0) (collections/READ id)))))])
      (is (= (into new-coll {:id id}) (ut/remove-db-only (collections/READ id))))))
  (testing "coll to self, multiple fields at once"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})
          add-coll (collections/CREATE new-coll)
          id (:id add-coll)]
      (let [fields-to-change (ut/random-submap new-coll)]
        (let [res (rp/collection-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge new-coll fields-to-change {:id id}))
               (ut/remove-db-only (collections/READ id)))))))
  (testing "coll to self, all fields at once"
    (let [new-coll (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)})
          add-coll (collections/CREATE new-coll)
          id (:id add-coll)]
      (let [res (rp/collection-id-patch id new-coll)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge new-coll {:id id}))
             (ut/remove-db-only (collections/READ id)))))))

(deftest test-rsrc-patch
  (testing "cont fields one at a time"
    (let [new-cont (g/get-random-resource-without-id)
          id (:id test-rsrc-one)]
      (is (= (ut/remove-db-only test-rsrc-one) (ut/remove-db-only (resources/READ id))))
      (doseq [val (seq new-cont)]
             [(do
                (let [res (rp/resource-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-cont) ((get val 0) (resources/READ id)))))])
      (is (= (into new-cont {:id id}) (ut/remove-db-only (resources/READ id))))))
  (testing "cont multiple fields at once"
    (let [new-cont (g/get-random-resource-without-id)
          id (:id test-rsrc-two)]
      (is (= (ut/remove-db-only test-rsrc-two) (ut/remove-db-only (resources/READ id))))
      (let [fields-to-change (ut/random-submap new-cont)]
        (let [res (rp/resource-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-rsrc-two fields-to-change))
               (ut/remove-db-only (resources/READ id)))))))
  (testing "cont all fields at once"
    (let [new-cont (g/get-random-resource-without-id)
          id (:id test-rsrc-thr)]
      (is (= (ut/remove-db-only test-rsrc-thr) (ut/remove-db-only (resources/READ id))))
      (let [res (rp/resource-id-patch id new-cont)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-rsrc-thr new-cont))
             (ut/remove-db-only (resources/READ id))))))
  (testing "cont to self, fields one at a time"
    (let [new-cont (g/get-random-resource-without-id)
          add-cont (resources/CREATE new-cont)
          id (:id add-cont)]
      (doseq [val (seq new-cont)]
             [(do
                (let [res (rp/resource-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-cont) ((get val 0) (resources/READ id)))))])
      (is (= (into new-cont {:id id}) (ut/remove-db-only (resources/READ id))))))
  (testing "cont to self, multiple fields at once"
    (let [new-cont (g/get-random-resource-without-id)
          add-cont (resources/CREATE new-cont)
          id (:id add-cont)]
      (let [fields-to-change (ut/random-submap new-cont)]
        (let [res (rp/resource-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge new-cont fields-to-change {:id id}))
               (ut/remove-db-only (resources/READ id)))))))
  (testing "cont to self, all fields at once"
    (let [new-cont (g/get-random-resource-without-id)
          add-cont (resources/CREATE new-cont)
          id (:id add-cont)]
      (let [res (rp/resource-id-patch id new-cont)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge new-cont {:id id}))
             (ut/remove-db-only (resources/READ id)))))))

(deftest test-crse-patch
  (testing "crse fields one at a time"
    (let [new-crse (g/get-random-course-without-id)
          id (:id test-crse-one)]
      (is (= (ut/remove-db-only test-crse-one) (ut/remove-db-only (courses/READ id))))
      (doseq [val (seq new-crse)]
             [(do
                (let [res (rp/course-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-crse) ((get val 0) (courses/READ id)))))])
      (is (= (into new-crse {:id id}) (ut/remove-db-only (courses/READ id))))))
  (testing "crse multiple fields at once"
    (let [new-crse (g/get-random-course-without-id)
          id (:id test-crse-two)]
      (is (= (ut/remove-db-only test-crse-two) (ut/remove-db-only (courses/READ id))))
      (let [fields-to-change (ut/random-submap new-crse)]
        (let [res (rp/course-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-crse-two fields-to-change))
               (ut/remove-db-only (courses/READ id)))))))
  (testing "crse all fields at once"
    (let [new-crse (g/get-random-course-without-id)
          id (:id test-crse-thr)]
      (is (= (ut/remove-db-only test-crse-thr) (ut/remove-db-only (courses/READ id))))
      (let [res (rp/course-id-patch id new-crse)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-crse-thr new-crse))
             (ut/remove-db-only (courses/READ id))))))
  (testing "update course to self (all fields)"
    (let [crse-one (into (g/get-random-course-without-id))
          crse-one-res (courses/CREATE crse-one)
          res (rp/course-id-patch (:id crse-one-res) crse-one)]
      (is (= 200 (:status res)))
      (is (= (into crse-one {:id (:id crse-one-res)}) (ut/remove-db-only (courses/READ (:id crse-one-res)))))))
  (testing "update course to self (multiple fields)"
    (let [new-crse (g/get-random-course-without-id)
          crse-one-res (courses/CREATE new-crse)
          id (:id crse-one-res)]
      (let [fields-to-change (ut/random-submap new-crse)]
        (let [res (rp/course-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge (into new-crse {:id id}) fields-to-change))
               (ut/remove-db-only (courses/READ id)))))))
  (testing "update course to self (one field at a time)"
    (let [new-crse (g/get-random-course-without-id)
          crse-one-res (courses/CREATE new-crse)
          id (:id crse-one-res)]
      (doseq [val (seq new-crse)]
             [(do
                (let [res (rp/course-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-crse) ((get val 0) (courses/READ id)))))])
      (is (= (into new-crse {:id id}) (ut/remove-db-only (courses/READ id)))))))


(deftest test-file-patch
  (testing "file fields one at a time"
    (let [new-file (db-pop/get-file (:id test-rsrc-two))
          id (:id test-file-one)]
      (is (= (ut/remove-db-only test-file-one) (ut/remove-db-only (files/READ id))))
      (doseq [val (seq new-file)]
             [(do
                (let [res (rp/file-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-file) ((get val 0) (files/READ id)))))])
      (is (= (into new-file {:id id}) (ut/remove-db-only (files/READ id))))))
  (testing "file multiple fields at once"
    (let [new-file (db-pop/get-file (:id test-rsrc-thr))
          id (:id test-file-two)]
      (is (= (ut/remove-db-only test-file-two) (ut/remove-db-only (files/READ id))))
      (let [fields-to-change (ut/random-submap new-file)]
        (let [res (rp/file-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-file-two fields-to-change))
               (ut/remove-db-only (files/READ id)))))))
  (testing "file all fields at once"
    (let [new-file (db-pop/get-file (:id test-rsrc-one))
          id (:id test-file-thr)]
      (is (= (ut/remove-db-only test-file-thr) (ut/remove-db-only (files/READ id))))
      (let [res (rp/file-id-patch id new-file)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-file-thr new-file))
             (ut/remove-db-only (files/READ id))))))
  (testing "file to self (each field)"
    (let [new-file (db-pop/get-file (:id test-rsrc-one))
          new-file-res (files/CREATE new-file)
          id (:id new-file-res)]
      (doseq [val (seq new-file)]
             [(do
                (let [res (rp/file-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-file) ((get val 0) (files/READ id)))))])
      (is (= (into new-file {:id id}) (ut/remove-db-only (files/READ id))))))
  (testing "file to self (some fields)"
    (let [new-file (db-pop/get-file (:id test-rsrc-one))
          new-file-res (files/CREATE new-file)
          id (:id new-file-res)]
      (let [fields-to-change (ut/random-submap new-file)]
        (let [res (rp/file-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (into (merge new-file fields-to-change) {:id id}))
               (ut/remove-db-only (files/READ id)))))))
  (testing "file to self (all field)"
      (let [new-file (db-pop/get-file (:id test-rsrc-one))
            new-file-res (files/CREATE new-file)
            id (:id new-file-res)]
        (let [res (rp/file-id-patch id new-file)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (into new-file {:id id}))
               (ut/remove-db-only (files/READ id)))))))

(deftest test-word-patch
  (testing "word fields one at a time"
    (let [new-word (g/get-random-word-without-id (:id test-user-one))
          id (:id test-word-one)]
      (is (= (ut/remove-db-only test-word-one) (ut/remove-db-only (words/READ id))))
      (doseq [val (seq new-word)]
             [(do
                (let [res (rp/word-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-word) ((get val 0) (words/READ id)))))])
      (is (= (into new-word {:id id}) (ut/remove-db-only (words/READ id))))))
  (testing "word multiple fields at once"
    (let [new-word (g/get-random-word-without-id (:id test-user-two))
          id (:id test-word-two)]
      (is (= (ut/remove-db-only test-word-two) (ut/remove-db-only (words/READ id))))
      (let [fields-to-change (ut/random-submap new-word)]
        (let [res (rp/word-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-word-two fields-to-change))
               (ut/remove-db-only (words/READ id)))))))
  (testing "word all fields at once"
    (let [new-word (g/get-random-word-without-id (:id test-user-thr))
          id (:id test-word-thr)]
      (is (= (ut/remove-db-only test-word-thr) (ut/remove-db-only (words/READ id))))
      (let [res (rp/word-id-patch id new-word)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-word-thr new-word))
             (ut/remove-db-only (words/READ id))))))
  (testing "word fields one at a time"
    (let [new-word (g/get-random-word-without-id (:id test-user-one))
          add-word (words/CREATE new-word)
          id (:id add-word)]
      (doseq [val (seq new-word)]
             [(do
                (let [res (rp/word-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-word) ((get val 0) (words/READ id)))))])
      (is (= (into new-word {:id id}) (ut/remove-db-only (words/READ id))))))
  (testing "word multiple fields at once"
    (let [new-word (g/get-random-word-without-id (:id test-user-two))
          add-word (words/CREATE new-word)
          id (:id add-word)]
      (let [fields-to-change (ut/random-submap new-word)]
        (let [res (rp/word-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge new-word fields-to-change {:id id}))
               (ut/remove-db-only (words/READ id)))))))
  (testing "word all fields at once"
    (let [new-word (g/get-random-word-without-id (:id test-user-thr))
          add-word (words/CREATE new-word)
          id (:id add-word)]
      (let [res (rp/word-id-patch id new-word)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge new-word {:id id}))
             (ut/remove-db-only (words/READ id)))))))

(deftest test-content-patch
  (testing "content fields one at a time"
    (let [new-content (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          id (:id test-content-one)]
      (is (= (ut/remove-db-only test-content-one) (ut/remove-db-only (contents/READ id))))
      (doseq [val (seq (dissoc new-content :resource-id :collection-id))]
             [(do
                (let [res (rp/content-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-content) ((get val 0) (contents/READ id)))))])
      (is (= (into new-content {:id id}) (ut/remove-db-only (contents/READ id))))))
  (testing "content multiple fields at once"
    (let [new-content (g/get-random-content-without-id (:id test-coll-two) (:id test-rsrc-two) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          id (:id test-content-two)]
      (is (= (ut/remove-db-only test-content-two) (ut/remove-db-only (contents/READ id))))
      (let [fields-to-change (ut/random-submap new-content)]
        (let [res (rp/content-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge test-content-two fields-to-change))
               (ut/remove-db-only (contents/READ id)))))))
  (testing "content all fields at once"
    (let [new-content (g/get-random-content-without-id (:id test-coll-thr) (:id test-rsrc-thr) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          id (:id test-content-thr)]
      (is (= (ut/remove-db-only test-content-thr) (ut/remove-db-only (contents/READ id))))
      (let [res (rp/content-id-patch id new-content)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge test-content-thr new-content))
             (ut/remove-db-only (contents/READ id))))))
  (testing "content fields one at a time"
    (let [new-content (g/get-random-content-without-id (:id (collections/CREATE (g/get-random-collection-without-id (:id test-user-one)))) (:id test-rsrc-thr) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          add-ann (contents/CREATE new-content)
          id (:id add-ann)]
      (doseq [val (seq (dissoc new-content :resource-id :collection-id))]
             [(do
                (let [res (rp/content-id-patch id {(get val 0) (get val 1)})]
                  (is (= 200 (:status res))))
                (is (= ((get val 0) new-content) ((get val 0) (contents/READ id)))))])
      (is (= (into new-content {:id id}) (ut/remove-db-only (contents/READ id))))))
  (testing "content multiple fields at once"
    (let [new-content (g/get-random-content-without-id (:id (collections/CREATE (g/get-random-collection-without-id (:id test-user-one)))) (:id test-rsrc-thr) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          add-ann (contents/CREATE new-content)
          id (:id add-ann)]
      (let [fields-to-change (ut/random-submap new-content)]
        (let [res (rp/content-id-patch id fields-to-change)]
          (is (= 200 (:status res))))
        (is (= (ut/remove-db-only (merge new-content fields-to-change {:id id}))
               (ut/remove-db-only (contents/READ id)))))))
  (testing "content all fields at once"
    (let [new-content (g/get-random-content-without-id (:id (collections/CREATE (g/get-random-collection-without-id (:id test-user-one)))) (:id test-rsrc-thr) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          add-ann (contents/CREATE new-content)
          id (:id add-ann)]
      (let [res (rp/content-id-patch id new-content)]
        (is (= 200 (:status res))))
      (is (= (ut/remove-db-only (merge new-content {:id id}))
             (ut/remove-db-only (contents/READ id)))))))




