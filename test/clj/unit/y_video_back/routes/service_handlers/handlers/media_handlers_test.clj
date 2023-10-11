(ns y-video-back.routes.service-handlers.handlers.media-handlers-test
  (:require
   [clojure.test :refer [use-fixtures deftest is testing]]
   [y-video-back.config :refer [env]]
   [mount.core :as mount]
   [legacy.utils.route-proxy.proxy :as rp]
   [y-video-back.db.migratus :as migratus]
   [legacy.db.test-util :as tcore]
   [legacy.utils.db-populator :as db-pop]
   [legacy.utils.utils :as ut]
)
  )

(tcore/basic-transaction-fixtures
 (mount.core/start)
; (migratus/renew)
 (ut/renew-db)
 (def user-one (db-pop/add-user "admin"))
 (def res (rp/login-current-user "test")))

(deftest stream-partial-media
  (let [file-key nil
        url nil
        filecontent (ut/get-filecontent)
        file-one (dissoc (db-pop/get-file) :filepath :aspect-ratio)
        res (rp/file-post file-one filecontent)]
    (testing "Stream partial media refererced by file-key"
       (is false))
      ))
