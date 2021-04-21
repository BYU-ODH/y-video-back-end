(ns y-video-back.log
  (:require
   [y-video-back.config :refer [env]]
   [cheshire.core :refer :all]
   [clojure.data.json :as json]
   [taoensso.timbre :as timbre
    :refer [log  trace  debug  info  warn  error  fatal  report
            logf tracef debugf infof warnf errorf fatalf reportf
            spy get-env]]))

(defn json-output [{:keys [level msg_ instant]}]
  (let [event (read-string (force msg_))]
    (json/write-str (into {:timestamp (str instant)}
                          event)
                    :escape-slash false)))

(defn log-media-access
  [message]
  (timbre/with-config {:level :info
                       :appenders {:spit (merge (timbre/spit-appender {:fname (str (get-in env [:FILES :log-path]) (.format (new java.text.SimpleDateFormat "yyyy-MM-dd") (java.util.Date.)) ".log")})
                                                {:output-fn json-output})}}
    (info message)))

(defn log-endpoint-access
  [message]
  (timbre/with-config {:level :info
                       :appenders {:spit (merge (timbre/spit-appender {:fname (str (get-in env [:FILES :endpoint-log-path]) (.format (new java.text.SimpleDateFormat "yyyy-MM-dd") (java.util.Date.)) ".log")})
                                                {:output-fn json-output})}}
    (info message)))
