(ns y-video-back.middleware
  (:require [y-video-back.env :refer [defaults]]
            [clojure.tools.logging :as log]
            [y-video-back.layout :refer [*app-context* error-page]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [y-video-back.config :refer [env]]
            [ring.middleware.flash :refer [wrap-flash]]
            [immutant.web.middleware :refer [wrap-session]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [byu-cas.core :as cas]
            ;[cheshire.generate :as cheshire]
            ;[cognitect.transit :as transit]
            [y-video-back.middleware.formats :as formats]
            ;[muuntaja.middleware :refer [wrap-format wrap-params]]
            [ring-ttl-session.core :refer [ttl-memory-store]]
            [y-video-back.routes.service-handlers.utils.utils :as sh-utils]
            [ring.middleware.cors :refer [wrap-cors]]
            [y-video-back.routes.service-handlers.utils.role-utils :as ru]
            [clojure.data.json :as json]
            [y-video-back.utils.account-permissions :as ac])

  (:import [javax.servlet ServletContext]))

(def cors-headers
  "Generic CORS headers"
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Expose-Headers" "*"
   "Access-Control-Allow-Methods" "GET POST OPTIONS DELETE PUT"})

(defn wrap-cas [handler]
  (fn [request]
    ((cas/wrap-cas handler {:timeout 120 :host-override (:host env)})
     request)))
    ;((cas/wrap-cas handler (str (-> env :y-video-back :site-url) (str (:uri request))))
    ; request))

(defn wrap-pre-cas [handler]
  (fn [request]
    ;(println "request-in-pre-cas=" request)
    (handler request)))

(defn wrap-post-cas [handler]
  (fn [request]
    (handler request)))

(defn wrap-cas-to-request-url
  "redirects user to BYU cas login"
  [handler]
  (fn [request]
    (let [etaoin-test? (:etaoin-test env)
          request (if etaoin-test?
                    (assoc request :username "puppypar"
                      request))]
      (if-not (and (:username request)
                   (or (:test env) etaoin-test?))
        ((cas/wrap-cas handler (str (-> env
                                        :humhelp
                                        :site-url)
                                    (-> request :path-info))) request)
        (handler request)))))
(defn wrap-context [handler]
  (fn [request]
    (binding [*app-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath ^ServletContext context)
                     (catch IllegalArgumentException _ context))
                ;; if the context is not specified in the request
                ;; we check if one has been specified in the environment
                ;; instead
                (:app-context env))]
      (handler request))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t)
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn print-handler [handler]
  (fn [req]
    (println "\n\n-----Handler is:")
    (println (str handler))
    (println "\n\n-----Req is:")
    (println (str req))
    (handler req)))



(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "403 - Invalid anti-forgery token"
        :image "lack_of_faith.jpg"
        :caption "I find your lack of valid anti-forgery token disturbing."})}))

(defn wrap-formats
  "Ensure that json<>map conversion is in place"
  [handler]
  (let [wrapped (wrap-restful-format
                  handler
                  {:formats [:json-kw :transit-json :transit-msgpack]})]
    (fn [request]
      ((if (:websocket? request) handler wrapped) request))))


(defn get-session-id
  "Gets session-id out of request"
  [req]
  (:session-id (:header (:parameters req))))


(defn get-permission-level
  "Returns permission level from request, nil if not found."
  [request]
  (ac/to-int-type (get-in request [:reitit.core/match :data (:request-method request) :permission-level])))

(defn get-role-level
  "Returns role level from request, nil if not found."
  [request]
  (get-in request [:reitit.core/match :data (:request-method request) :role-level]))

(defn get-path-to-id
  "Returns path to id from request, nil if not found."
  [request]
  (get-in request [:reitit.core/match :data (:request-method request) :path-to-id]))

(defn get-obj-id
  "Returns obj id for checking permission. Relies on :path-to-id field in request,
  or defaults to [:parameters :path :id]. Returns nil if no id found."
  [request]
  (get-in request (first (remove nil? [
                                       (get-path-to-id request)
                                       [:parameters :path :id]]))))

(defn get-bypass-permission
  "Returns true if :bypass-permission set to true, else false."
  [request]
  (first (remove nil? [
                       (get-in request [:reitit.core/match :data (:request-method request) :bypass-permission])
                       false])))

(defn check-permission
  "Checks user has permission for route"
  [handler]
  ;handler)
  (fn [request]
    ;(println "permission-level=" (get-permission-level request))
    ;(println "role-level=" (get-role-level request))
    ;(println "path-to-id=" (get-path-to-id request))
    (if (ru/bypass-uri (:uri request))
      (handler request)
      (let [session-id (get-in request [:parameters :header :session-id])]
        (if (or (nil? session-id)
                (and (not (= (:session-id-bypass env) (str session-id)))
                     (nil? (ru/token-to-user-id session-id))))
          {:status 401 :body {:message "forbidden"}}
          (if (= (:session-id-bypass env) (str session-id))
            (handler request)
            (let [valid-type (and (not (nil? (get-permission-level request)))
                                  (<= (ru/get-user-type (ru/token-to-user-id session-id))
                                      (get-permission-level request)))
                  valid-role (and (not (nil? (get-role-level request)))
                                  (ru/check-user-role (ru/token-to-user-id session-id)
                                                      (get-obj-id request)
                                                      (get-role-level request)))
                  bypass-permission (get-bypass-permission request)]
              ;(println "valid-type, valid-role, bypass-permission: " valid-type valid-role bypass-permission)
              (if (or valid-type valid-role bypass-permission)
                (handler (assoc request :permission-values {:valid-type valid-type
                                                            :valid-role valid-role}))
                {:status 401 :body {:message "forbidden"}}))))))))

(defn add-session-id
  "Adds new session-id to response header. Invalidates old session-id."
  [handler]
  (fn [request]
    ;(println "add session-id middleware")
    (if (or (clojure.string/starts-with? (:uri request) "/api/docs")
            (clojure.string/starts-with? (:uri request) "/api/swagger.json")
            (clojure.string/starts-with? (:uri request) "/api/get-session-id")
            (clojure.string/starts-with? (:uri request) "/api/media/stream-media")
            (= (:uri request) "/api/ping"))
      (handler request)
      (if (= (get-session-id request) (sh-utils/to-uuid (:session-id-bypass env)))
        (assoc-in (handler request) [:headers "session-id"] (get-session-id request))
        (let [response (handler request)]
          (if (= 200 (:status response))
            (assoc-in response [:headers "session-id"] (ru/get-new-session-id (get-session-id request)))
            (assoc-in response [:headers "session-id"] (get-session-id request))))))))

(defn wrap-api [handler]
  (let [check-csrf  (if-not (:test env) wrap-csrf identity)]
      (-> ((:middleware defaults) handler)
          ;(print-handler)
          (wrap-cors :access-control-allow-origin #"http://localhost:3000" :access-control-allow-methods [:get :put :post :delete :patch]
                     :access-control-allow-credentials "true" :access-control-expose-headers "session-id"))))
          ;check-csrf)))
          ;check-permission)))
          ;wrap-flash
          ;;wrap-cas
          ;wrap-csrf)))
          ;(wrap-session {:cookie-attrs {:http-only true}})
          ;(wrap-defaults
          ;  (-> site-defaults
          ;      (assoc-in [:security :anti-forgery] false)
          ;      (dissoc :session)
          ;wrap-context
          ;wrap-internal-error)))

(defn wrap-api-post [handler]
  (-> handler
      check-permission
      add-session-id)) ; Why are these evaluated backwards?

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      ;wrap-post-cas
      wrap-cas
      ;wrap-pre-cas
      ;wrap-csrf
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))
      wrap-context
      wrap-internal-error))
