(ns y-video-back.routes.home
  (:require [y-video-back.layout :as layout]
            [y-video-back.middleware :as middleware]))

(defn home-page [request-map]
  (layout/hiccup-render-cljs-base {:username request-map}))

(def ^{:private true} home-paths
  [
   "/"
   "/article"
   "/admin"])


(defn home-routes
  "The basic routes to be handled by the SPA (as rendered by fn `home-page`)"
  []
  (into [""
         {:middleware [middleware/wrap-base
                       middleware/wrap-formats
                       ]}]
        (for [path home-paths]
          [path {:get home-page}])))

