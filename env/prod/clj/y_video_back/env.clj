(ns y-video-back.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[y-video-back started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[y-video-back has shut down successfully]=-"))
   :middleware identity})
