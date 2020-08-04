(ns y-video-back.routes.public
  (:require
    [y-video-back.config :refer [env]]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    ;[reitit.ring :as ring]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [y-video-back.middleware.formats :as formats]
    [y-video-back.middleware.exception :as exception]
    [y-video-back.middleware :as middleware]
;    [y-video-back.dbaccess.access :as db-access]
    [y-video-back.routes.public-handlers.handlers :as public-handlers]
    [ring.util.http-response :as response]
    [clojure.java.io :as io]
    ;[y-video-back.user-creator :as uc]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]))
(defn public-routes []
   [""
    {:coercion spec-coercion/coercion
     :muuntaja formats/instance
     :swagger {:id ::api}
     :middleware [;; query-params & form-params
                  parameters/parameters-middleware
                  ;; resource-negotiation
                  muuntaja/format-negotiate-middleware
                  ;; encoding response body
                  muuntaja/format-response-middleware
                  ;; exception handling
                  exception/exception-middleware
                  ;; decoding request body
                  muuntaja/format-request-middleware
                  ;; coercing response bodys
                  coercion/coerce-response-middleware
                  ;; coercing request parameters
                  coercion/coerce-request-middleware
                  ;; multipart
                  multipart/multipart-middleware
                  wrap-multipart-params]}
                  ;; CAS
                  ;middleware/wrap-cas-no-redirect

    ["/public"
     {:swagger {:tags ["public"]}}
     ["/ping"
      {:get (constantly (response/ok {:message "pong"}))}]
     ["/collection/{id}"
      {:get public-handlers/public-collection-get-by-id}]
     ["/collections"
      {:get public-handlers/public-collection-get-all}]
     ["/content/{id}"
      {:get public-handlers/public-content-get-by-id}]
     ["/contents"
      {:get public-handlers/public-content-get-all}]
     ["/resource/{id}"
      {:get public-handlers/public-resource-get-by-id}]
     ["/resources"
      {:get public-handlers/public-resource-get-all}]]])
