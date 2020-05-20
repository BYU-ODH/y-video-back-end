(ns y-video-back.routes.swagger
  "The API routes that are the primary CRUD functionality of this app"
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    ;; [reitit.ring.coercion :as coercion]
    ;; [reitit.coercion.spec :as spec-coercion]
    ;; [reitit.ring.middleware.muuntaja :as muuntaja]
    ;; [reitit.ring.middleware.multipart :as multipart]
    ;; [reitit.ring.middleware.parameters :as parameters]
    ;; [y-video-back.middleware.formats :as formats]
    ;; [y-video-back.middleware.exception :as exception]
    ;; [y-video-back.dbaccess.access :as db-access]
    ;; [y-video-back.routes.service-handlers :as service-handlers]
    ;; [ring.util.http-response :refer :all]
    ;; [clojure.java.io :as io]
    ))

(def swagger-routes
  ["" {:no-doc true}
   ["/swagger.json" {:get (swagger/create-swagger-handler)}]
   ["/api-docs/*" {:get (swagger-ui/create-swagger-ui-handler)}]])
