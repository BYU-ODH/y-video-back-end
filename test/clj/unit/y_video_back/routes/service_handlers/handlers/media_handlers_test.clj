(ns y-video-back.routes.service-handlers.handlers.media-handlers-test
)
;;   (:require
;;    [clojure.test :refer [use-fixtures deftest is testing]]
;;    [y-video-back.config :refer [env]]
;;    [mount.core :as mount]
;;    [legacy.utils.route-proxy.proxy :as rp]
;;    [y-video-back.db.migratus :as migratus]
;;    [legacy.db.test-util :as tcore]
;;    [legacy.utils.db-populator :as db-pop]
;;    [legacy.utils.utils :as ut]
;;    [y-video-back.db.files :as files]
;;    [muuntaja.core :as m]))

;; (tcore/basic-transaction-fixtures
;;  (mount.core/start)
;; ; (migratus/renew)
;;  (ut/renew-db)
;;  (def user-one (db-pop/add-user "admin"))
;;  #_(def res (rp/login-current-user "test")))

;; (deftest stream-partial-media-route
;;   (let [file-key nil ;; TODO where do I get the file-key? (rp/get-file-key (:id (db-pop/add-file)))
;;         url (str "/api/partial-media/stream-media/" file-key)
;;         file-content (ut/get-filecontent)
;;         file-one (db-pop/get-file) ;; what's the difference between dbpop/get- and dbpop/add- file?
;;         resource (rp/file-post file-one file-content)]    
;;     (testing "file READ"
;;     (let [file-one (ut/under-to-hyphen (files/CREATE (db-pop/get-file)))
;;           res (rp/file-id-get (:id file-one))]
;;       (is (= 200 (:status res)))
;;       (is (= (-> file-one
;;                  (ut/remove-db-only)
;;                  (update :id str)
;;                  (update :resource-id str))
;;              (ut/remove-db-only (m/decode-response-body res))))))
;;       ))
