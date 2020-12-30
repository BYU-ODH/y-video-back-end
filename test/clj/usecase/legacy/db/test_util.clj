(ns legacy.db.test-util
  (:require [y-video-back.db.core :refer [*db*] :as db]
            [clojure.test :refer [use-fixtures deftest is testing] :as t]
            [mount.core :as mount]
            [clojure.java.jdbc :as jdbc]
            ; [y-video-back.db.migratus]
            [y-video-back.config :refer [env]])
  (:import (clojure.lang IDeref)))

(defmacro with-transaction
  "Runs the body in a transaction where t-conn is the name of the transaction connection.
   The body will be evaluated within a binding where conn is set to the transactional
   connection. The isolation level and readonly status of the transaction may also be specified.
   (with-transaction [conn {:isolation level :read-only? true}]
     ... t-conn ...)
   See jdbc/db-transaction* for more details on the semantics of the :isolation and
   :read-only? options."
  [[dbsym & opts] & body]
  `(if (instance? IDeref ~dbsym)
     (jdbc/with-db-transaction [t-conn# (deref ~dbsym) ~@opts]
       (binding [~dbsym (delay t-conn#)]
         ~@body))
     (jdbc/with-db-transaction [t-conn# ~dbsym ~@opts]
       (binding [~dbsym t-conn#]
         ~@body))))

(defmacro basic-transaction-fixtures
  [& forms]
  `(t/use-fixtures
     :each
     (fn [f#]
       (mount/start
        #'y-video-back.config/env
        #'y-video-back.db.core/*db*)
       (with-transaction [db/*db*]
         (jdbc/db-set-rollback-only! db/*db*)
         (do ~@forms)
         (f#)))))
