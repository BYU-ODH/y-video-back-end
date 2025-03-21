(ns legacy.routes.language)
;;   (:require
;;    [clojure.test :refer :all]
;;    [y-video-back.handler :refer :all]
;;    [legacy.db.test-util :as tcore]
;;    [muuntaja.core :as m]
;;    [clojure.java.jdbc :as jdbc]
;;    [mount.core :as mount]
;;    [legacy.utils.route-proxy.proxy :as rp]
;;    [y-video-back.db.core :refer [*db*] :as db]
;;    [legacy.utils.utils :as ut]
;;    [legacy.utils.db-populator :as db-pop]
;;    [y-video-back.db.languages :as languages]
;;    [migratus.core :as migratus]
;;    [y-video-back.db.migratus :as yv-migratus]))

;; (declare ^:dynamic *txn*)

;; (use-fixtures
;;   :once
;;   (fn [f]
;;     (mount/start #'y-video-back.config/env
;;                  #'y-video-back.handler/app
;;                  #'y-video-back.db.core/*db*)
;;     (ut/renew-db)
;;     (f)))

;; (tcore/basic-transaction-fixtures
;;  (mount.core/start #'y-video-back.handler/app)
;;  (def all-languages (languages/READ)))

;; (deftest test-lang
;;   (testing "lang CREATE"
;;     (let [lang-one (db-pop/get-language)]
;;       (let [res (rp/language-post lang-one)]
;;         (is (= 200 (:status res)))
;;         (let [id (:id (m/decode-response-body res))]
;;           (is (= lang-one
;;                  (ut/remove-db-only (languages/READ id))))))))
;;   (testing "language DELETE"
;;     (let [lang-one (languages/CREATE (db-pop/get-language))
;;           res (rp/language-id-delete (:id lang-one))]
;;       (is (= 200 (:status res)))
;;       (is (= nil (languages/READ (:id lang-one)))))))

;; (deftest lang-get-all-0
;;   (testing "get all languages from db (0)")
;;   (let [res (rp/language-get-all)]
;;     (is (= 200 (:status res)))
;;     (is (= (count all-languages)
;;            (count (m/decode-response-body res))))))

;; (deftest lang-get-all-1
;;   (testing "get all languages from db (1)")
;;   (let [lang-one (db-pop/add-language)
;;         lang-one-id (:id lang-one)
;;         res (rp/language-get-all)]
;;     (is (= 200 (:status res)) "Response returns ok")
;;     (is (= (select-keys (frequencies (m/decode-response-body res)) [lang-one-id])
;;            (frequencies (map #(:id %) [lang-one]))
;;            ) "The created item is present in the response")))
;; (comment
;;   (let [in-map {"foo" 321} ]
;;     (select-keys in-map ["foo"])
;;       ) ;; => {"foo" 321}
;;   )

;; (deftest lang-get-all-3
;;   (testing "get all languages from db (3)")
;;   (let [lang-one (db-pop/add-language)
;;         lang-two (db-pop/add-language)
;;         lang-thr (db-pop/add-language)
;;         ids-of-123 (map :id [lang-one lang-two lang-thr])
;;         res (rp/language-get-all)]
;;     (is (= 200 (:status res)))
;;     (is (= (frequencies ids-of-123)
;;            (select-keys (frequencies (m/decode-response-body res)) ids-of-123))
;;         "given languages are present just once")))


