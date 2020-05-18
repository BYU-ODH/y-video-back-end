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

   ["/collections"
    {:swagger {:tags ["collections"]}}

    [""
      {:get {:summary "Retrieves collection info for all collections available to the current user_id"
             :parameters {:query {:user_id string?}}
             :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
                         404 {:body {:message string?}}}
             :handler (fn [{{{:keys [user_id]} :query} :parameters}]
                       (let [collection_result (db-access/get_collections user_id)]
                        (if (nil? collection_result)
                          {:status 404
                           :body {:message "requested collection not found"}}
                          {:status 200
                           :body collection_result})))}

       :post {:summary "Adds a new collection with the current user as an owner"
              :parameters {:body {:id string? :name string? :published boolean? :archived boolean?
                                  :assoc_courses [{:id string? :department string? :catalog_number string? :section_number string?}]
                                  :assoc_user [{:id string? :role string?}]
                                  :assoc_content [{:id string? :name string? :thumbnail string? :published boolean?
                                                   :allow_definitions boolean? :allow_notes boolean? :allow_captions string?}]}}
                          :responses {200 {:body {:message string?}}}
                          :handler (fn [{{{:keys [id name published archived]} :body} :parameters}]
                                    {:status 200
                                     :body (db-access/add_collection id name published archived)})}}]]
   ["/collection"
    {:swagger {:tags ["collection"]}}
    [""
     {:get {:summary "Retrieves the specified collection"
            :parameters {:query {:collection_id string?}}
            :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
                        404 {:body {:message string?}}}
            :handler (fn [{{{:keys [collection_id]} :query} :parameters}]
                      (let [collection_result (db-access/get_collections collection_id)]
                       (if (nil? collection_result)
                         {:status 404
                          :body {:message "requested collection not found"}}
                         {:status 200
                          :body collection_result})))}

      :post {:summary "Adds a new collection with the current user as an owner"
             :parameters {:body {:id string? :name string? :published boolean? :archived boolean?
                                 :assoc_courses [{:id string? :department string? :catalog_number string? :section_number string?}]
                                 :assoc_user [{:id string? :role string?}]
                                 :assoc_content [{:id string? :name string? :thumbnail string? :published boolean?
                                                  :allow_definitions boolean? :allow_notes boolean? :allow_captions string?}]}}
                         :responses {200 {:body {:message string?}}}
                         :handler (fn [{{{:keys [id name published archived]} :body} :parameters}]
                                   {:status 200
                                    :body (db-access/add_collection id name published archived)})}}]]





   (comment ["/math"
             {:swagger {:tags ["math"]}}

             ["/plus"
              {:get {:summary "plus with spec query parameters"
                     :parameters {:query {:x int?, :y int?}}
                     :responses {200 {:body {:total pos-int?}}}
                     :handler (fn [{{{:keys [x y]} :query} :parameters}]
                                {:status 200
                                 :body {:total (+ x y)}})}
               :post {:summary "plus with spec body parameters"
                      :parameters {:body {:x int?, :y int?}}
                      :responses {200 {:body {:total pos-int?}}}
                      :handler (fn [{{{:keys [x y]} :body} :parameters}]
                                 {:status 200
                                  :body {:total (+ x y)}})}}]])

   (comment ["/files"
             {:swagger {:tags ["files"]}}

             ["/upload"
              {:post {:summary "upload a file"
                      :parameters {:multipart {:file multipart/temp-file-part}}
                      :responses {200 {:body {:name string?, :size int?}}}
                      :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                 {:status 200
                                  :body {:name (:filename file)
                                         :size (:size file)}})}}]

             ["/download"
              {:get {:summary "downloads a file"
                     :swagger {:produces ["image/png"]}
                     :handler (fn [_]
                                {:status 200
                                 :headers {"Content-Type" "image/png"}
                                 :body (-> "public/img/warning_clojure.png"
                                           (io/resource)
                                           (io/input-stream))})}}]])])
