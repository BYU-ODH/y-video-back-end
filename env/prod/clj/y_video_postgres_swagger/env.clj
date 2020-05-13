(ns y-video-postgres-swagger.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[y-video-postgres-swagger started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[y-video-postgres-swagger has shut down successfully]=-"))
   :middleware identity})
