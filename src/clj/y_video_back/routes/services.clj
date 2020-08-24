(ns y-video-back.routes.services
  (:require
    [y-video-back.config :refer [env]]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [y-video-back.middleware.formats :as formats]
    [y-video-back.middleware.exception :as exception]
    [y-video-back.middleware :as middleware]
    [y-video-back.routes.service-handlers.handlers :as service-handlers]
    [ring.util.http-response :as response]
    [y-video-back.user-creator :as uc]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]))


(defn service-routes []
   ["/api"
    {:coercion spec-coercion/coercion
     :muuntaja formats/instance
     :swagger {:id ::api}
     :middleware [middleware/wrap-api
                  ;; query-params & form-params
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
                  wrap-multipart-params
                  ;; CAS
                  ;middleware/wrap-cas-no-redirect
                  middleware/wrap-api-post]}

    ;; swagger documentation
    ["" {:no-doc true
         :swagger {:info {:title "my-api"
                          :description "https://cljdoc.org/d/metosin/reitit"}}}

     ["/swagger.json"
      {:get (swagger/create-swagger-handler)}]

     ["/docs/*"
      {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]
     ["/docs"
      {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

    ["/get-session-id/{username}/{password}"
     {:swagger {:tags ["auth"]}}

     [""
      {:get {:summary "gets session id for username"
             :parameters {:path {:username string?
                                 :password string?}}
             :responses {200 {:body {:session-id string?}}
                         403 {:body {:message string?}}}
             :handler (fn [{{{:keys [username password]} :path} :parameters}]
                        (if-not (= (:NEW-USER-PASSWORD env) password)
                          {:status 403
                           :body {:message "incorrect password"}}
                          {:status 200
                           :body {:session-id (str (uc/get-session-id username))}}))}}]]
    ["/ping"
     {:get {:summary "ping, requires valid session-id"
            :permission-level "admin"
            :bypass-permission true
            :responses {200 {:body {:message string?}}}
            :handler (fn [req]
                       {:status 200
                        :body {:message "pong"}})}}]
     ;{:get (constantly (response/ok {:message "pong"}))}]
    ["/auth-ping"
     {:get {:summary "ping, requires valid session-id"
            :permission-level "admin"
            :parameters {:header {:session-id uuid?}}
            :responses {200 {:body {:message string?}}}
            :handler (fn [req]
                       {:status 200
                        :body {:message "ping"}})}}]

    ["/surely-a-get-method"
     {:post (constantly (response/ok {:message "pong"}))}]

    ["/jedi-council"
     {:get {:validate false
            :permission-level -1
            :handler (fn [] "doesn't matter")}}]

    ["/echo"
     {:swagger {:tags ["echo"]}}

     [""
      {:get {:summary "echo parameter get"
             :permission-level (:admin env)
             :parameters {:query {:echo string?}}
             :responses {200 {:body {:echo string?}}}
             :handler (fn [{{{:keys [echo]} :query} :parameters}]
                        (println (str "In the echo get route with query: " echo))
                        {:status 200
                         :body {:echo echo}})}
       :post {:summary "echo parameter post"
              :permission-level (:admin env)
              :parameters {:header {:session-id uuid?}
                           :body {:echo string?}}
                           ;:multipart {"file" multipart/temp-file-part}}
                           ;:multipart {:data {:echo string?}}}
              :responses {200 {:body {:echo string?}}}
              :handler (fn [req]
                         ;(println "req=" req)
                         {:status 200
                          :body (get-in req [:parameters :body])})}
       :patch service-handlers/echo-patch}]
     ["/:word"
      {:get {:summary "echo parameter get"
             :permission-level (:admin env)
             :parameters {:path {:word string?}
                          :query {:second string?}}
             :responses {200 {:body {:echo string?
                                     :second string?}}}
             :handler (fn [{{{:keys [word]} :path {:keys [second]} :query} :parameters}]
                        {:status 200
                         :body {:echo word
                                :second second}})}}]]
    ;["/cas-testing"
    ; {:swagger {:tags ["cas-testing"]}}

    ; [""
    ;  {:get {:summary "return username"
    ;         :parameters {}
    ;         :responses {200 {:body {:username string?}}}
    ;         :handler (fn [{{{:keys [username]} :body} :parameters}]
    ;                    {:status 200
    ;                     :body {:username username}})}}]]
    ["/user"
     {:swagger {:tags ["user"]}}
     [""
      {:post service-handlers/user-create
       :get service-handlers/user-get-logged-in}]
     ["/{id}"
      {:get service-handlers/user-get-by-id
       :patch service-handlers/user-update
       :delete service-handlers/user-delete}]
     ["/{id}/collections"
      {:get service-handlers/user-get-all-collections}]
     ["/{id}/courses"
      {:get service-handlers/user-get-all-courses}]

     ["/{id}/words"
      {:get service-handlers/user-get-all-words}]]

    ["/word"
     {:swagger {:tags ["word"]}}
     [""
      {:post service-handlers/word-create}]
     ["/{id}"
      {:get service-handlers/word-get-by-id
       :patch service-handlers/word-update
       :delete service-handlers/word-delete}]]

    ["/collections"
     {:swagger {:tags ["collections"]}}

     [""
      {:get service-handlers/user-get-all-collections-by-logged-in}]]  ;; This method is a place holder - it is used elsewhere

    ["/refresh-courses"
     {:swagger {:tags ["refresh-courses"]}}

     [""
      {:post service-handlers/refresh-courses}]]  ;; This method is a place holder - it is used elsewhere

    ["/collection"
     {:swagger {:tags ["collection"]}}
     [""
      {:post service-handlers/collection-create}]
     ["/{id}"
      {:get service-handlers/collection-get-by-id
       :patch service-handlers/collection-update
       :delete service-handlers/collection-delete}]
     ["/{id}/add-user"
      {:post service-handlers/collection-add-user}]
     ["/{id}/remove-user"
      {:post service-handlers/collection-remove-user}]
     ["/{id}/add-course"
      {:post service-handlers/collection-add-course}]
     ["/{id}/remove-course"
      {:post service-handlers/collection-remove-course}]
     ["/{id}/contents"
      {:get service-handlers/collection-get-all-contents}]
     ["/{id}/courses"
      {:get service-handlers/collection-get-all-courses}]
     ["/{id}/users"
      {:get service-handlers/collection-get-all-users}]]

    [ "/course"
     {:swagger {:tags ["course"]}}
     [""
      {:post service-handlers/course-create}]
     ["/{id}"
      {:get service-handlers/course-get-by-id
       :patch service-handlers/course-update
       :delete service-handlers/course-delete}]
     ;["/{id}/add-collection"
     ; {:post service-handlers/course-add-collection}]
     ;["/{id}/remove-collection"
     ; {:post service-handlers/course-remove-collection}]
     ["/{id}/collections"
      {:get service-handlers/course-get-all-collections}]
     ["/{id}/add-user"
      {:post service-handlers/course-add-user}]
     ["/{id}/remove-user"
      {:post service-handlers/course-remove-user}]
     ["/{id}/users"
      {:get service-handlers/course-get-all-users}]]

    ["/resource"
     {:swagger {:tags ["resource"]}}
     [""
      {:post service-handlers/resource-create}]

     ["/{id}"
      {:get service-handlers/resource-get-by-id
       :patch service-handlers/resource-update
       :delete service-handlers/resource-delete}]
     ;["/{id}/connect-file"
     ; {:post service-handlers/resource-connect-file}]
     ["/{id}/files"
      {:get service-handlers/resource-get-all-files}]
     ;["/{id}/add-view"
     ; {:post service-handlers/resource-add-view}]
     ["/{id}/collections"
      {:get service-handlers/resource-get-all-collections}]
     ["/{id}/contents"
      {:get service-handlers/resource-get-all-contents}]
     ["/{id}/subtitles"
      {:get service-handlers/resource-get-all-subtitles}]]


    ["/content"
     {:swagger {:tags ["content"]}}
     [""
      {:post service-handlers/content-create}]
     ["/{id}"
      {:get service-handlers/content-get-by-id
       :patch service-handlers/content-update
       :delete service-handlers/content-delete}]
     ["/{id}/add-view"
      {:post service-handlers/content-add-view}]
     ;["/{id}/add-subtitle"
     ; {:post service-handlers/content-add-subtitle}]
     ;["/{id}/remove-subtitle"
     ; {:post service-handlers/content-remove-subtitle}]
     ["/{id}/subtitles"
      {:get service-handlers/content-subtitles}]
     ["/{id}/clone-subtitle"
      {:post service-handlers/content-clone-subtitle}]]

    ["/subtitle"
     {:swagger {:tags ["subtitle"]}}
     [""
      {:post service-handlers/subtitle-create}]
     ["/{id}"
      {:get service-handlers/subtitle-get-by-id
       :patch service-handlers/subtitle-update
       :delete service-handlers/subtitle-delete}]]

    ["/file"
     {:swagger {:tags ["file"]}}
     [""
      {:post service-handlers/file-create}]
     ["/{id}"
      {:get service-handlers/file-get-by-id
       :patch service-handlers/file-update
       :delete service-handlers/file-delete}]]

    ["/languages"
     {:swagger {:tags ["languages"]}}
     [""
      {:get service-handlers/language-get-all}]]
    ["/language"
     {:swagger {:tags ["language"]}}
     [""
      {:post service-handlers/language-create}]
     ["/{id}"
      {:get service-handlers/language-get-by-id
       :patch service-handlers/language-update
       :delete service-handlers/language-delete}]]

    ;["/connect-collection-and-course"
    ; {:swagger {:tags ["connect"]}}
    ; [""
    ;  {:post service-handlers/connect-collection-and-course}]]

    ["/search"
     {:swagger {:tags ["search"]}}
     [""]]
      ;{:get service-handlers/search-by-term}]] ; Searches all tables at once
    ["/admin"
     {:swagger {:tags ["admin"]}}
     ["/user/{term}"
      {:get service-handlers/search-by-user}]
     ["/collection/{term}"
      {:get service-handlers/search-by-collection}]
     ["/content/{term}"
      {:get service-handlers/search-by-content}]
     ["/resource/{term}"
      {:get service-handlers/search-by-resource}]]
     ;["/refresh-course-list/{password}"
     ; {:post service-handlers/refresh-course-list}]]


    ["/media"
     {:swagger {:tags ["media"]}}
     ;["/upload-file"
     ; {:post service-handlers/upload-file}}
     ["/get-file-key/{file-id}"
      {:get service-handlers/get-file-key}]
     ["/stream-media/{file-key}"
      {:get service-handlers/stream-media}]]])
