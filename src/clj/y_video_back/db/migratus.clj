(ns y-video-back.db.migratus
  (:require
   [migratus.core :as migratus]
   [y-video-back.config :refer [env]]))

(defn adapt-hikari-to-migratus [config]
  {:store :database
   :migration-dir "migrations/"
   :init-script "init.sql"
   :migration-table-name "migrations"
   :db {:classname "org.postgresql.Driver"
        :subprotocol (:adapter config)
        :subname (str "//"
                      (:server-name config)
                      ":"
                      (:port-number config)
                      "/"
                      (:database-name config))
        :user (:username config)
        :password (:password config)}})

(defn config [] (adapt-hikari-to-migratus (-> env :y-video-back :db)))

(defn init []
  (migratus/init (config)))

(defn reset [] (migratus/reset (config)))

(defn renew []
  (init)
  (reset))

#_(renew)
