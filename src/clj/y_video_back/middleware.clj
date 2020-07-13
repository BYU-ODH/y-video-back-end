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
            [y-video-back.routes.service-handlers.utils :as sh-utils]
            [ring.middleware.cors :refer [wrap-cors]]
            [y-video-back.routes.service-handlers.role-utils :as ru])
  (:import [javax.servlet ServletContext]))

(def cors-headers
  "Generic CORS headers"
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Allow-Methods" "GET POST OPTIONS DELETE PUT"})

(defn wrap-cas [handler]
  (cas/wrap-cas handler (str (-> env :y-video-back :site-url) "/")))

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

(defn check-permission
  "Checks user has permission for route"
  [handler]
  ;handler)
  (fn [request]
    (if (ru/req-has-permission (:uri request) (:header (:parameters request)) (:body (:parameters request)))
      (handler request)
      (error-page {:status 401, :title "401 - Unauthorized",
                   :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))))

(defn add-session-id
  "Adds new session-id to response header"
  [handler]
  (fn [request]
    (let [new-id (ru/get-new-session-id (get-session-id request))
          response (handler request)]
      (assoc-in response [:headers "session-id"] new-id))))

(defn wrap-api [handler]
  (let [check-csrf  (if-not (:test env) wrap-csrf identity)]
      (-> ((:middleware defaults) handler)
          (wrap-cors :access-control-allow-origin #"http://localhost:3000" :access-control-allow-methods [:get :put :post :delete :patch]
                     :access-control-allow-credentials "true"))))
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
      add-session-id))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      wrap-cas
      wrap-csrf
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))
      wrap-context
      wrap-internal-error))
