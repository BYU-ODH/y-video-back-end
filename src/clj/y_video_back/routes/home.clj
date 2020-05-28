(ns y-video-back.routes.home
    (:require
     [y-video-back.layout :as layout]
     [clojure.java.io :as io]
     [y-video-back.middleware :as middleware]))



(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render request "about.html"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])



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
                       middleware/wrap-formats]}]

        (for [path home-paths]
          [path {:get home-page}])))
