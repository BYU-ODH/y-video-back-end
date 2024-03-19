(ns y-video-back.middleware
  (:require
   [byu-cas.core :as cas]
   [clojure.data.json :as json]
   [clojure.tools.logging :as log]
   [immutant.web.middleware :refer [wrap-session]]
   [ring-ttl-session.core :refer [ttl-memory-store]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.flash :refer [wrap-flash]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [ring.middleware.webjars :refer [wrap-webjars]]
   [ring.middleware.content-type :as ring-content]
   [ring.util.response :refer [redirect]]
   [y-video-back.config :refer [env]]
   [y-video-back.db.users :as users]
   [y-video-back.layout :as layout]
   [y-video-back.layout :refer [*app-context* error-page]]
   [y-video-back.log :as ylog]
   [y-video-back.middleware.formats :as formats]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [y-video-back.routes.service-handlers.utils.utils :as sh-utils]
   [y-video-back.utils.account-permissions :as ac]
   [y-video-back.env :refer [defaults]])

  (:import [javax.servlet ServletContext]))

(def cors-headers
  "Generic CORS headers"
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Expose-Headers" "*"
   "Access-Control-Allow-Methods" "GET POST OPTIONS DELETE PUT"})

(defn wrap-cas [handler]
    "Validates CAS login. If invalid, only prompts login if given /login path-info"
  (fn [request]
    ((cas/wrap-cas handler {:timeout 120 :host-override (:host env) :no-redirect? (constantly (not (= "/login" (str (:path-info request)))))})
     request)))

(defn wrap-pre-cas [handler]
  (fn [request]
    (let [res (handler request)]
      (if (= 403 (:status res))
          (layout/render request "index.html" {:session-id ""})
          res))))

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
                     :message "We have ecountered an unexpected error. We are working to fix it. Please try again later."})))))

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
        :caption "Sorry, you are not authorized to access this page. Check your credentials and try again."})}))

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

(def forbidden-page
  (error-page {:status 403, :title "403 - Forbidden",
              :caption "Sorry, you are not authorized to access this page. Check your credentials and try again."}))

(def unauthorized-page
  (error-page {:status 401, :title "401 - Unauthorized",
              :caption "Your session id has expired. Please sign in again."}))

(defn add-id-and-username
  "Adds user id and username to request. Adds nil if values do not exist."
  [handler]
  (fn [request]
    (let [session-id (get-in request [:parameters :header :session-id])
          user-id (ru/token-to-user-id session-id)
          username (users/id-to-username user-id)]
      (handler (merge request {:user-id user-id :username username})))))

(defn check-permission
  "Checks user has permission for route"
  [handler]
  (fn [request]
    (if (ru/bypass-uri (:uri request))
      (handler request)
      (let [session-id (get-in request [:parameters :header :session-id])]
        (if (or (nil? session-id)
                (and (not (and (or (:dev env) (:test env))
                               (= (:session-id-bypass env) (str session-id))))
                     (nil? (ru/token-to-user-id session-id)))) ;; TODO #145 maybe error here
          unauthorized-page  ; no user for session-id and session-id is not bypass from config
          (if (and (or (:dev env) (:test env)) ;; TODO: rewrite this logic without nested control statements
                   (= (:session-id-bypass env) (str session-id)))
            (handler request)  ; session-id is bypass from config
            (let [valid-type (and (not (nil? (get-permission-level request)))
                                  (<= (ru/get-user-type (ru/token-to-user-id session-id))
                                      (get-permission-level request)))
                  valid-role (and (not (nil? (get-role-level request)))
                                  (ru/check-user-role (ru/token-to-user-id session-id)
                                                      (get-obj-id request)
                                                      (get-role-level request)))
                  bypass-permission (get-bypass-permission request)]
              (if (or valid-type valid-role bypass-permission)
                (handler (assoc request :permission-values {:valid-type valid-type
                                                            :valid-role valid-role}))
                forbidden-page))))))))

(defn add-session-id
  "Adds new session-id to response header. Invalidates old session-id."
  [handler]
  (fn [request]
    (if (or (clojure.string/starts-with? (:uri request) "/api/docs")
            (clojure.string/starts-with? (:uri request) "/api/swagger.json")
            (clojure.string/starts-with? (:uri request) "/api/get-session-id")
            (clojure.string/starts-with? (:uri request) "/api/media/stream-media")
            (clojure.string/starts-with? (:uri request) "/api/partial-media/stream-media")
            (= (:uri request) "/api/ping"))
      (handler request)
      (if (= (get-session-id request) (sh-utils/to-uuid (:session-id-bypass env)))
        (assoc-in (handler request) [:headers "session-id"] (get-session-id request))
        (let [response (handler request)]
          (if (and (= 200 (:status response))
                   (not (nil? (get-session-id request)))
                   (not (and (= :post (:request-method request))
                             (clojure.string/starts-with? (:uri request) "/api/file"))))
              (assoc-in response [:headers "session-id"] (ru/get-new-session-id (get-session-id request)))
              (assoc-in response [:headers "session-id"] (get-session-id request))))))))

(defn log-endpoint-access
  "Logs access to endpoint."
  [handler]
  (fn [request]
    (if (or (:dev env) (:prod env))
      (ylog/log-endpoint-access {:method (:request-method request)
                                 :path (str (:path-info request))
                                 :username (:username request)
                                 :user-id (str (:user-id request))}))
    (handler request)))

(defn wrap-api [handler]
  (let [check-csrf  (if-not (:test env) wrap-csrf identity)]
      (-> ((:middleware defaults) handler)
          (wrap-cors :access-control-allow-origin #"http://localhost:3000" :access-control-allow-methods [:get :put :post :delete :patch]
                     :access-control-allow-credentials "true" :access-control-expose-headers "session-id"))))

(defn wrap-api-post [handler]
  (-> handler
      log-endpoint-access
      add-id-and-username
      check-permission
      add-session-id))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)      
      wrap-flash
      wrap-post-cas
      wrap-cas
      wrap-pre-cas
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (dissoc :session)))
      wrap-context
      wrap-internal-error))
