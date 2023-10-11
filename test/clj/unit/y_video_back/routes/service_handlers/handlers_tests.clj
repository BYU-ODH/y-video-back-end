(ns y-video-back.routes.service-handlers.handlers-tests
  (:require
   [clojure.test :refer [use-fixtures deftest is testing]]
   [y-video-back.config :refer [env]]
   [mount.core :as mount]
   [legacy.utils.route-proxy.proxy :as rp]
   [y-video-back.db.migratus :as migratus]
   [legacy.db.test-util :as tcore]
   [legacy.utils.db-populator :as db-pop]
))

#_(tcore/basic-transaction-fixtures
 (mount.core/start)
 (migratus/renew)
)

