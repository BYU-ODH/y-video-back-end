(ns y-video-back.routes.content
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.content-subtitles-assoc :as content-subtitles-assoc]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
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
                 (update :resource-id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "content UPDATE"
    (let [cont-one (contents/CREATE (db-pop/get-content))
          cont-two (db-pop/get-content)]
      (let [res (rp/content-id-patch (:id cont-one) cont-two)]
        (is (= 200 (:status res)))
        (is (= (into cont-two {:id (:id cont-one)}) (ut/remove-db-only (contents/READ (:id cont-one))))))))
  (testing "content DELETE"
    (let [cont-one (contents/CREATE (db-pop/get-content))
          res (rp/content-id-delete (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= nil (contents/READ (:id cont-one)))))))

(deftest content-add-subtitle
  (testing "add subtitile to content"
    (let [cont-one (db-pop/add-content)
          rsrc-id (:resource-id cont-one)
          sbtl-one (db-pop/add-subtitle rsrc-id)]
      (is (= '() (content-subtitles-assoc/READ-BY-IDS [(:id cont-one) rsrc-id])))
      (let [res (rp/content-id-add-subtitle (:id cont-one)
                                         (:id sbtl-one))]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (list {:id id
                        :content-id (:id cont-one)
                        :subtitle-id (:id sbtl-one)})
                 (map ut/remove-db-only (content-subtitles-assoc/READ-BY-IDS [(:id cont-one) (:id sbtl-one)]))))))))
  (testing "add sbtl to cont, wrong rsrc"
    (let [cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle)
          res (rp/content-id-add-subtitle (:id cont-one) (:id sbtl-one))]
      (is (= 500 (:status res))))))
(deftest cont-remove-subtitle
  (testing "remove subtitle from content"
    (let [cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle)
          sbtl-cont (content-subtitles-assoc/CREATE (g/get-random-content-subtitles-assoc-without-id (:id cont-one) (:id sbtl-one)))]
      (is (not (= '() (content-subtitles-assoc/READ-BY-IDS [(:id cont-one) (:id sbtl-one)]))))
      (let [res (rp/content-id-remove-subtitle (:id cont-one) (:id sbtl-one))]
        (is (= 200 (:status res)))
        (is (= '() (content-subtitles-assoc/READ-BY-IDS [(:id cont-one) (:id sbtl-one)])))))))
(deftest cont-get-all-subtitles
  (testing "get subtitles for content (0)"
    (let [cont-one (db-pop/add-content)
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= []
             (m/decode-response-body res)))))
  (testing "get subtitles for content (1)"
    (let [cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= [(-> sbtl-one
                  (update :id str)
                  (update :resource-id str))]
             (m/decode-response-body res)))))
  (testing "get subtitles for content (2)"
    (let [cont-one (db-pop/add-content)
          sbtl-one (db-pop/add-subtitle (:resource-id cont-one))
          sbtl-two (db-pop/add-subtitle (:resource-id cont-one))
          sbtl-thr (db-pop/add-subtitle (:resource-id cont-one))
          sbtl-fou (db-pop/add-subtitle)
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-one))
          cont-sbtl-add (db-pop/add-cont-sbtl-assoc (:id cont-one) (:id sbtl-two))
          res (rp/content-id-subtitles (:id cont-one))]
      (is (= 200 (:status res)))
      (is (= (frequencies (map #(-> %
                                    (update :id str)
                                    (update :resource-id str))
                               [sbtl-one sbtl-two]))
             (frequencies (m/decode-response-body res)))))))
