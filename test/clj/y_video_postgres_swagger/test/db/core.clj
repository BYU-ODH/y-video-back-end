(ns y-video-postgres-swagger.test.db.core
  (:require
   [y-video-postgres-swagger.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [y-video-postgres-swagger.config :refer [env]]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'y-video-postgres-swagger.config/env
     #'y-video-postgres-swagger.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-collections
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (db/add-collection!
              t-conn
              {:id         "1"
               :name "Sam"
               :published  false
               :archived      false})))
    (is (= {:id         "1"
            :name "Sam"
            :published  false
            :archived      false}
           (db/get-collection t-conn {:id "1"})))))
