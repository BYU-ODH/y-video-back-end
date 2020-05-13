(ns y-video-postgres-swagger.routes.home
  (:require
   [y-video-postgres-swagger.layout :as layout]
   [y-video-postgres-swagger.db.core :as db]
   [clojure.java.io :as io]
   [y-video-postgres-swagger.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

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
