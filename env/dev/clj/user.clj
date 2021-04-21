(ns user
  (:require [mount.core :as mount]
            ; [y-video-back.figwheel :refer [start-fw stop-fw cljs]]
            ; [y-video-back.core]
            ))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn restart []
  (stop)
  (start))
