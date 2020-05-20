(ns y-video-back.env
  (:require 
            [clojure.tools.logging :as log]
            [y-video-back.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[y-video-back started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[y-video-back has shut down successfully]=-"))
   :middleware wrap-dev})
