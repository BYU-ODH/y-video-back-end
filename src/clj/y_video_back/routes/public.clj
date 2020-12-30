(ns y-video-back.routes.public)
;   (:require
;     [reitit.swagger :as swagger]
;     [reitit.swagger-ui :as swagger-ui]
;     [reitit.ring.coercion :as coercion]
;     [reitit.coercion.spec :as spec-coercion]
;     [reitit.ring.middleware.muuntaja :as muuntaja]
;     [reitit.ring.middleware.multipart :as multipart]
;     [reitit.ring.middleware.parameters :as parameters]
;     [y-video-back.middleware.formats :as formats]
;     [y-video-back.middleware.exception :as exception]
;     [y-video-back.routes.public-handlers.handlers :as public-handlers]
;     [ring.util.http-response :as response]
;     [ring.middleware.multipart-params :refer [wrap-multipart-params]]))
; (defn public-routes []
;    ["/public"
;     {:coercion spec-coercion/coercion
;      :muuntaja formats/instance
;      :swagger {:id ::api}
;      :middleware [;; query-params & form-params
;                   parameters/parameters-middleware
;                   ;; resource-negotiation
;                   muuntaja/format-negotiate-middleware
;                   ;; encoding response body
;                   muuntaja/format-response-middleware
;                   ;; exception handling
;                   exception/exception-middleware
;                   ;; decoding request body
;                   muuntaja/format-request-middleware
;                   ;; coercing response bodys
;                   coercion/coerce-response-middleware
;                   ;; coercing request parameters
;                   coercion/coerce-request-middleware
;                   ;; multipart
;                   multipart/multipart-middleware
;                   wrap-multipart-params]}
;                   ;; CAS
;                   ;middleware/wrap-cas-no-redirect
;
;    ;; swagger documentation
;     ["" {:no-doc true
;          :swagger {:info {:title "my-api"
;                           :description "https://cljdoc.org/d/metosin/reitit"}}}
;
;      ["/swagger.json"
;       {:get (swagger/create-swagger-handler)}]
;
;      ["/docs/*"
;       {:get (swagger-ui/create-swagger-ui-handler
;              {:url "/public/swagger.json"
;               :config {:validator-url nil}})}]
;      ["/docs"
;       {:get (swagger-ui/create-swagger-ui-handler
;              {:url "/api/sw agger.json"
;               :config {:validator-url nil}})}]]
;
;
;     [""
;      {:swagger {:tags ["public"]}}
;      ["/ping"
;       {:get (constantly (response/ok {:message "pong"}))}]
;      ["/collection/{id}"
;       {:get public-handlers/public-collection-get-by-id}]
;      ["/collections"
;       {:get public-handlers/public-collection-get-all}]
;      ["/content/{id}"
;       {:get public-handlers/public-content-get-by-id}]
;      ["/contents"
;       {:get public-handlers/public-content-get-all}]
;      ["/resource/{id}"
;       {:get public-handlers/public-resource-get-by-id}]
;      ["/resources"
;       {:get public-handlers/public-resource-get-all}]]])
