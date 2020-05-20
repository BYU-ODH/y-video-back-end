(ns y-video-back.db.core
  (:require
   [cheshire.generate]
   [cheshire.core :refer [generate-string parse-string]]
   [clojure.java.jdbc :as jdbc]
   [hikari-cp.core :as hik]
   [y-video-back.config :refer [env]]
   [mount.core :refer [defstate]]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :refer [transform-keys]] 
   [honeysql.core :as sql]
   [clojure.string :refer [join]]
   [tick.alpha.api :as t])
  (:import org.postgresql.util.PGobject
           java.sql.Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql
            BatchUpdateException
            Date
            Timestamp
            PreparedStatement]
           java.text.SimpleDateFormat))

(defstate ^:dynamic *db*
  :start #_{:datasource (hik/make-datasource (-> env :y-video-back :db))}
  {} 
  :stop #_(hik/close-datasource (:datasource *db*))
  identity)

(defn to-date [^java.sql.Date sql-date]
  (-> sql-date (.getTime) (t/new-duration :millis) (t/instant)))

(extend-protocol jdbc/IResultSetReadColumn
  Date
  (result-set-read-column [v - -] (to-date v))

  Timestamp
  (result-set-read-column [v - -] (to-date v))

  Array
  (result-set-read-column [v - -] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj -metadata -index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

;;serializes java.time.Instant as json 
(extend-protocol cheshire.generate/JSONable
  java.time.Instant
  (to-json [dt gen]
    (cheshire.generate/write-string gen (str dt))))

;;serializes java.time.Instant as java.sql.Timestamp
(extend-type java.time.Instant
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt ^long idx]
    (.setTimestamp stmt idx (Timestamp. (.toEpochMilli v)))))

(defn string-to-int [s]
  (if (= java.lang.String (type s))
    (Integer/valueOf s)
    s))

(defn to-pg-type [type val]
  (doto (PGobject.)
    (.setType type)
    (.setValue val)))

(defn to-pg-json [value]
  (to-pg-type "jsonb" (generate-string value)))

(defn to-pg-state [value]
  (to-pg-type "state" value))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_)
                           (apply str (rest type-name)))]
        (.setObject stmt idx
                    (.createArrayOf conn elem-type
                                             (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))

;;added by scott griffin on 26 oct 2019. potentially could be removed
#_(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [value]
    (doto (PGobject.)
      (.setType "json")
      (.setValue (json/write-str value)))))

;;added by scott griffin on 26 oct 2019. potentially could be removed
#_(extend-protocol jdbc/IResultSetReadColumn
  PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (json/read-str value :key-fn keyword)
        :else value))))

#_(defn value-to-json-pgobject [value]
  (doto (PGobject.)
    (.setType "json")
      (.setValue (json/write-str value))))

#_(extend-protocol jdbc/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [value] (value-to-json-pgobject value))

  clojure.lang.IPersistentVector
  (sql-value [value] (value-to-json-pgobject value)))



;; CRUD
(defn dbc!
  [table-key entrymap]
  (jdbc/insert! *db* table-key entrymap))
(defn dbr [s]
  (map (partial transform-keys csk/->kebab-case-keyword)
       (jdbc/query *db* s)))
(defn dbu! [table-key valmap where-vec]
  (jdbc/update! *db* table-key valmap where-vec))
(defn dbd! [table-key s]
  (jdbc/delete! *db* table-key s))
(defn dbdo! [s] (jdbc/execute! *db* s))

(defn CREATE
  "Generic item creation"
  [table-keyword valmap]
  (first (dbc! (csk/->snake_case_keyword table-keyword)
               (transform-keys csk/->snake_case_keyword valmap))))

(defn READ
  "Get anything from table by id"
  [table-keyword &[id select-field-keys]]
  (cond-> {:select (or select-field-keys [:*])
           :from [table-keyword]}
    id (assoc :where [:= :id id])
    1 sql/format
    1 dbr
    id first
    (= 1 (count select-field-keys))
    (#((first select-field-keys) %))))

(defn UPDATE
  "Update anything from table by id"
  [table-keyword id valmap]
  (let [tk (csk/->snake_case_keyword table-keyword)
        valmap (transform-keys csk/->snake_case_keyword valmap)]
    (dbu! tk valmap ["id = ?" id])
    (transform-keys csk/->kebab-case-keyword
                    (READ tk id))))

(defn DELETE
  "Generic delete"
  [table-keyword id]
  (dbd! (csk/->snake_case_keyword table-keyword) ["id = ?" id]))

(defn mark-deleted
  "Update delete status in table by id"
  [table-keyword id]
  (let [entry (READ table-keyword id)
        val-map (-> entry
                    (dissoc :id)
                    (assoc :deleted true))]
    (UPDATE table-keyword id val-map)))

(defn CLONE
  "Make a clone"
  [table-key id]
  (let [to-be-cloned (READ table-key id)
        clone (dissoc to-be-cloned :id)]
    (CREATE table-key clone)))

(defn page-map
  "Get anything from table by id, or all without id"
  [table-keyword {:keys [current-screen screen-amount select-field-keys]}]
  (if-not screen-amount (throw (ex-info "Missing key: screen-amount" {:cause :missing-key}))
          (let [off (* screen-amount current-screen) ]
            {:select (or select-field-keys [:*])
             :from  [table-keyword]
             :offset off
             :limit screen-amount})))

(defn READ-PAGE
  "Get anything from table by id, or all without id"
  [table-keyword {:as pm}]
  (-> (page-map table-keyword pm)
      sql/format
      dbr))

(defn pg->
  "Postgres json -> operator"
  [parent fieldkey]
  (sql/raw (str (name parent) "->'" (name fieldkey) "'")))

(defn pg->>
  "Postgres json ->> operator"
  [parent fieldkey]
  (sql/raw (str (name parent) "->>'" (name fieldkey) "'")))

(defn pg-intersect-arrays
  "Determine whether a Postgres array field interesects a provided collection"
  [db-array-field coll]
  (let [stringy-coll (map #(str "'" % "'") coll)
        array-format (str "["
                          (clojure.string/join "," stringy-coll)
                          "]")]
    (sql/raw (str (name db-array-field) " && ARRAY" array-format))))


