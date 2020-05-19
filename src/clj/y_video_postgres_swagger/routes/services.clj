(ns y-video-postgres-swagger.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [y-video-postgres-swagger.middleware.formats :as formats]
    [y-video-postgres-swagger.middleware.exception :as exception]
    [y-video-postgres-swagger.dbaccess.access :as db-access]
    [y-video-postgres-swagger.routes.service_handlers :as service-handlers]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]))


(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
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
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/ping"
    {:get (constantly (ok {:message "pong"}))}]


   ["/echo"
    {:swagger {:tags ["echo"]}}

    [""
     {:get {:summary "echo parameter get"
            :parameters {:query {:echo string?}}
            :responses {200 {:body {:echo string?}}}
            :handler (fn [{{{:keys [echo]} :query} :parameters}]
                       {:status 200
                        :body {:echo echo}})}
      :post {:summary "echo parameter post"
             :parameters {:body {:echo string?}}
             :responses {200 {:body {:echo string?}}}
             :handler (fn [{{{:keys [echo]} :body} :parameters}]
                        {:status 200
                         :body {:echo echo}})}}]]

   ["/user"
    {:swagger {:tags ["user"]}}
    [""
     {:post service-handlers/user-create}]
    ["/{id}"
       {:get service-handlers/user-get-by-id

        :patch service-handlers/user-update
        :delete service-handlers/user-delete}]]

   ["/collections"
    {:swagger {:tags ["collections"]}}

    [""
      {:get service-handlers/collection-get-all-by-user}]]

   ["/collection"
    {:swagger {:tags ["collection"]}}
    [""
     {:post service-handlers/collection-create}]
    ["/{id}"
     {:get service-handlers/collection-get-by-id


      :patch service-handlers/collection-update
      :delete service-handlers/collection-delete}]

    ["/{id}/content"
     {:get service-handlers/collection-get-all-contents}]]
   [ "/course"
    {:swagger {:tags ["course"]}}
    [""
     {:post service-handlers/course-create}]
    ["/{id}"
     {:get service-handlers/course-get-by-id

      :patch service-handlers/course-update
      :delete service-handlers/course-delete}]]

   ["/content"
    {:swagger {:tags ["content"]}}
    [""
     {:post service-handlers/content-create}]

    ["/{id}"
      {:get service-handlers/content-get-by-id

       :patch service-handlers/content-update
       :delete service-handlers/content-delete}]]])
