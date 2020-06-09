(ns y-video-back.handler
  (:require
            [y-video-back.layout :refer [error-page]]
            [y-video-back.routes.home :refer [home-routes]]
            [y-video-back.routes.services :refer [service-routes]]

            [reitit.ring :as ring]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [y-video-back.env :refer [defaults]]
            [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (do
    (println "Starting ring-handler")
    (ring/ring-handler
     (ring/router
      [(home-routes)
       (service-routes)])

     (ring/routes
      (ring/create-resource-handler
       {:path "/"})
      ;; (wrap-content-type
      (wrap-webjars (constantly nil))
      (ring/create-default-handler
       {:unauthorized
        (constantly (error-page {:status 401, :title "401 - Unauthorized",
                                 :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))

        :not-found #(do (println "Not found:" (prn-str %))
                        (error-page {:status 404, :title "404 - Page not found",
                                     :image "missing_planet.jpg", :caption "This page ought to be here... but it isn't."}))
        :method-not-allowed
        (constantly (error-page {:status 405, :title "405 - Not allowed",
                                 :image "anakin_obiwan_mustafar.jpg", :caption "Obi-Wan: Anakin, this is a get method!<br/><br/>Anakin: From my point of view, this is a post method!"}))
        :not-acceptable
        (constantly (error-page {:status 406, :title "406 - Not acceptable"}))})))))                
