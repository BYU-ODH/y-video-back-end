(ns legacy.routes.resource
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [muuntaja.core :as m]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.resource-access :as resource-access]))

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

(deftest test-rsrc
  (testing "rsrc CREATE"
    (let [rsrc-one (db-pop/get-resource)]
      (let [res (rp/resource-post rsrc-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into rsrc-one {:id id})
                 (ut/remove-db-only (resources/READ id))))))))
  (testing "rsrc READ"
    (let [rsrc-one (ut/under-to-hyphen (resources/CREATE (db-pop/get-resource)))
          res (rp/resource-id-get (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (-> rsrc-one
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "resource UPDATE"
    (let [rsrc-one (resources/CREATE (db-pop/get-resource))
          rsrc-two (db-pop/get-resource)]
      (let [res (rp/resource-id-patch (:id rsrc-one) rsrc-two)]
        (is (= 200 (:status res)))
        (is (= (into rsrc-two {:id (:id rsrc-one)}) (ut/remove-db-only (resources/READ (:id rsrc-one))))))))
  (testing "resource DELETE"
    (let [rsrc-one (resources/CREATE (db-pop/get-resource))
          res (rp/resource-id-delete (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (= nil (resources/READ (:id rsrc-one)))))))

(deftest rsrc-all-colls
  (testing "find all collections by resource"
    (let [rsrc-one (db-pop/add-resource)
          coll-one (db-pop/add-collection)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          res (rp/resource-id-collections (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (= (-> coll-one
                 (update :id str)
                 (update :owner str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest rsrc-all-conts
  (testing "find all contents by resource"
    (let [cont-one (db-pop/add-content)
          rsrc-id (:resource-id cont-one)
          res (rp/resource-id-contents rsrc-id)]
      (is (= 200 (:status res)))
      (is (= (-> cont-one
                 (update :id str)
                 (update :collection-id str)
                 (update :resource-id str)
                 (update :file-id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest rsrc-all-sbtls
  (testing "find all subtitles by resource (1 content)"
    (let [cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/resource-id-subtitles (:resource-id cont-one))]
      (is (= 200 (:status res)))
      (is (= (map #(-> %
                       (update :id str)
                       (update :content-id str))
                  [sbtl-one])
             (m/decode-response-body res)))))
  (testing "find all subtitles by resource (2 contents)"
    (let [cont-one (db-pop/add-content)
          coll-one (db-pop/add-collection)
          cont-two (db-pop/add-content (:id coll-one) (:resource-id cont-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          sbtl-two (db-pop/add-subtitle (:id cont-two))
          res (rp/resource-id-subtitles (:resource-id cont-one))]
      (is (= 200 (:status res)))
      (is (= (frequencies (map #(-> %
                                    (update :id str)
                                    (update :content-id str))
                               [sbtl-one sbtl-two]))
             (frequencies (m/decode-response-body res)))))))

(deftest rsrc-all-files
  (testing "find all files by resource"
    (let [file-one (db-pop/add-file)
          rsrc-id (:resource-id file-one)
          res (rp/resource-id-files rsrc-id)]
      (is (= 200 (:status res)))
      (is (= (-> file-one
                 (update :id str)
                 (update :resource-id str)
                 (list))
             (map ut/remove-db-only (m/decode-response-body res)))))))

(deftest rsrc-access
  (testing "add access to resource"
    (let [rsrc-one (db-pop/add-resource)
          res (rp/resource-add-access "testuser" (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (resource-access/EXISTS-USERNAME-RESOURCE? "testuser" (:id rsrc-one)))))
  (testing "remove access to resource"
    (let [rsrc-one (db-pop/add-resource)
          username "testuser"
          rsrc-acc (db-pop/add-resource-access username (:id rsrc-one))]
      (is (resource-access/EXISTS-USERNAME-RESOURCE? username (:id rsrc-one)))
      (let [res (rp/resource-remove-access username (:id rsrc-one))]
        (is (= 200 (:status res)))
        (is (not (resource-access/EXISTS-USERNAME-RESOURCE? username (:id rsrc-one)))))))
  (testing "read all users with access to resource"
    (let [rsrc-one (db-pop/add-resource)
          username-one "testuser"
          username-two "seconduser"
          username-thr "another!"
          res-zer (rp/resource-read-all-access (:id rsrc-one))
          acc-one (db-pop/add-resource-access username-one (:id rsrc-one))
          res-one (rp/resource-read-all-access (:id rsrc-one))
          rsrc-acc-one {:username username-two
                        :resource-id (:id rsrc-one)
                        :last-verified (java.sql.Timestamp/valueOf "2004-10-19 10:23:54")}
          rsrc-acc-one-create (resource-access/CREATE rsrc-acc-one)
          acc-thr (db-pop/add-resource-access username-thr (:id rsrc-one))
          res-two (rp/resource-read-all-access (:id rsrc-one))]
      (is (= 200 (:status res-zer)))
      (is (= 200 (:status res-one)))
      (is (= 200 (:status res-two)))
      (is (= '() (m/decode-response-body res-zer)))
      (is (= (frequencies (list {:valid true :username username-one}))
             (frequencies (m/decode-response-body res-one))))
      (is (= (frequencies (list {:valid true :username username-one}
                                {:valid false :username username-two}
                                {:valid true :username username-thr}))
             (frequencies (m/decode-response-body res-two))))))
  (testing "read all users from nonexistant resource"
    (let [fake-id (java.util.UUID/randomUUID)
          res-one (rp/resource-read-all-access fake-id)]
      (is (= 404 (:status res-one)))))
  (testing "renew expired access by adding again"
    (let [rsrc-one (db-pop/add-resource)
          username "testuser"
          rsrc-acc-one {:username username
                        :resource-id (:id rsrc-one)
                        :last-verified (java.sql.Timestamp/valueOf "2004-10-19 10:23:54")}
          rsrc-acc-one-create (resource-access/CREATE rsrc-acc-one)
          db-acc-one (resource-access/READ-BY-USERNAME-RESOURCE username (:id rsrc-one))
          res (rp/resource-add-access username (:id rsrc-one))
          db-acc-two (resource-access/READ-BY-USERNAME-RESOURCE username (:id rsrc-one))]
      (is (= 200 (:status res)))
      (is (< (inst-ms (:last-verified db-acc-one)) (inst-ms (:last-verified db-acc-two)))))))
