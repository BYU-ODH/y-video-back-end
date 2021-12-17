(ns legacy.routes.content
    (:require
      [clojure.test :refer :all]
      [y-video-back.handler :refer :all]
      [legacy.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [legacy.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [legacy.utils.utils :as ut]
      [legacy.utils.db-populator :as db-pop]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.subtitles :as subtitles]))

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

(deftest test-cont
  (testing "cont CREATE"
    (let [cont-one (db-pop/get-content)]
      (let [res (rp/content-post cont-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into cont-one {:id id})
                 (ut/remove-db-only (contents/READ id))))))))
  (testing "cont READ"
    (let [cont-one (ut/under-to-hyphen (contents/CREATE (db-pop/get-content)))
          res (rp/content-id-get (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= (-> cont-one
                 (ut/remove-db-only)
                 (update :id str)
                 (update :collection-id str)
                 (update :resource-id str)
                 (update :file-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "content UPDATE"
    (let [cont-one (contents/CREATE (db-pop/get-content))
          cont-two (db-pop/get-content)]
      (let [res (rp/content-id-patch (:id cont-one) cont-two)]
        (is (= 200 (:status res)))
        (is (= 
             (into cont-two {:id (:id cont-one)}) 
             (ut/remove-db-only (contents/READ (:id cont-one))))))))
  (testing "content DELETE" ;this is not used in the application
    (let [cont-one (contents/CREATE (db-pop/get-content))
          res (rp/content-id-delete (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id cont-one)))))))

(deftest cont-get-all-subtitles
  (testing "get subtitles for content (0)"
    (let [cont-one (db-pop/add-content)
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= []
             (m/decode-response-body res)))))
  (testing "get subtitles for content (1)"
    (let [cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= [(-> sbtl-one
                  (update :id str)
                  (update :content-id str))]
             (m/decode-response-body res)))))
  (testing "get subtitles for content (2)"
    (let [cont-one (db-pop/add-content)
          cont-two (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          sbtl-two (db-pop/add-subtitle (:id cont-one))
          sbtl-thr (db-pop/add-subtitle (:id cont-two))
          sbtl-fou (db-pop/add-subtitle)
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= (frequencies (map #(-> %
                                    (update :id str)
                                    (update :content-id str))
                               [sbtl-one sbtl-two]))
             (frequencies (m/decode-response-body res)))))))

(deftest content-clone-subtitle
  (testing "clone subtitle to content with 0 subtitles"
    (let [coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          cont-two (db-pop/add-content (:id coll-two) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          res (rp/content-id-clone-subtitle (:id cont-two) (:id sbtl-one))]
      (is (= 200 (:status res)))
      (let [new-id (ut/to-uuid (:id (m/decode-response-body res)))]
        (is (= (-> sbtl-one
                   (dissoc :id)
                   (dissoc :content-id)
                   (assoc :id new-id)
                   (assoc :content-id (:id cont-two)))
               (ut/remove-db-only (subtitles/READ new-id)))))))
  (testing "clone subtitle to content with 1 subtitle already"
    (let [coll-one (db-pop/add-collection)
          coll-two (db-pop/add-collection)
          rsrc-one (db-pop/add-resource)
          cont-one (db-pop/add-content (:id coll-one) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          cont-two (db-pop/add-content (:id coll-two) (:id rsrc-one) (ut/to-uuid "00000000-0000-0000-0000-000000000000"))
          sbtl-one (db-pop/add-subtitle (:id cont-one))
          sbtl-two (db-pop/add-subtitle (:id cont-two))
          res (rp/content-id-clone-subtitle (:id cont-two) (:id sbtl-one))]
      (is (= 200 (:status res)))
      (let [new-id (ut/to-uuid (:id (m/decode-response-body res)))]
        (is (= (-> sbtl-one
                   (dissoc :id)
                   (dissoc :content-id)
                   (assoc :id new-id)
                   (assoc :content-id (:id cont-two)))
               (ut/remove-db-only (subtitles/READ new-id))))))))


