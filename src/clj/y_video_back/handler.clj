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
      (wrap-webjars (constantly nil))
      (ring/create-default-handler
       {:unauthorized
        (constantly (error-page {:status 401, :title "401 - Unauthorized",
                                 :caption "Your session id has expired. Please sign in again."}))

        :not-found
        (constantly (error-page {:status 404, :title "404 - Page not found",
                                 :caption "We couldn't find the page you were looking for. Double check the URL and try again."}))
        :method-not-allowed
        (constantly (error-page {:status 405, :title "405 - Not allowed",
                                 :caption "Sorry, this action is not allowed for this page. Please try another request."}))
        :not-acceptable
        (constantly (error-page {:status 406, :title "406 - Not acceptable"
                                 :caption "We're sorry, your request is not supported by this page. Try another request."}))})))))
