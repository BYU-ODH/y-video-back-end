(ns user
  (:require [mount.core :as mount]
            [garden-gnome.watcher :as garden-gnome]
            [y-video-back.figwheel :refer [start-fw stop-fw cljs]]
            [y-video-back.core]
            ))

(mount/defstate garden
  :start (garden-gnome/start! (garden-gnome/default-config))
  :stop (garden-gnome/stop! garden))

(defn start []
  (mount/start))

(defn stop []
  (mount/stop))

(defn restart []
  (stop)
  (start))


