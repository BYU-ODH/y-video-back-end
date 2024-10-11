(ns y-video-back.cas
  (:require [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]
            [clojure.pprint :as pprint]
            [clojure.string :refer [join] :as s]
            [tick.alpha.api :as t]
            [clojure.data.json :as json])
  (:import (org.jasig.cas.client.validation Cas20ProxyTicketValidator
                                            TicketValidationException)))

;Cas10TicketValidator: https://github.com/apereo/java-cas-client/tree/master/cas-client-core/src/main/java/org/jasig/cas/client/validation
;Cas10TicketValidator < AbstractCasProtocolUrlBasedTicketValidator  < AbstractUrlBasedTicketValidator implements TicketValidator


(defn pprints-mw [handler]
  (fn [req]
    (println "req:")
    (pprint/pprint req)
    (let [resp (handler req)]
      (println "resp:")
      (pprint/pprint resp)
      resp)))


(def BYU-CAS-server "https://cas.byu.edu/cas")

(defn BYU-CAS-service
  "Returns a function that returns s because functions are required"
  [s]
  #(str s))

(def artifact-parameter-name "ticket")
(def const-cas-assertion "_const_cas_assertion_")

(defprotocol Validator
  (validate [v ticket service]))

(extend-type Cas20ProxyTicketValidator
  Validator
  (validate [v ticket service] (.validate v ticket service)))

;; (defn validator-maker [cas-server-fn]
;;   (Cas10TicketValidator. (cas-server-fn)))
(defn validator-maker
  ([] (validator-maker BYU-CAS-server))
  ([server] (Cas20ProxyTicketValidator. server)))

(defn- valid? [request]
  (or (get-in request [:session const-cas-assertion])
      (get-in request [:session (keyword const-cas-assertion)])
      (get-in request [:query-params artifact-parameter-name])
      (get-in request [:query-params (keyword artifact-parameter-name)])))

(defn add-query-params [url qparams]
    (str url
       (when-not (empty? qparams)
         (->> qparams
              (map (fn [[k v]] (str (name k) "=" (str v))))
              (join \&)
              (str \?)))))


(defn set-timeout
  "Takes a number of minutes and a response.  Sets a time in :session, after which user will be logged out."
  [duration resp]
  (if (= :none duration)
    resp
    (assoc-in resp [:session :logout-time]
              (t/+ (t/now)
                   (t/new-duration duration :minutes)))))


(defn create-redirect-url [req service remove-ticket? host-override]
  (let [host (if (nil? host-override)
               (str "http://" (get-in req [:headers "host"]))
               host-override)
        {:keys [uri query-params]} req
        query-params (cond-> query-params remove-ticket? (dissoc "ticket"))
        without-params (cond-> (str host uri)
                         (string? service) ((constantly service))
                         (fn? service) (service))]
    (add-query-params without-params query-params)))


(defn authentication-filter
  "Checks that the request is carrying CAS credentials (but does not validate them)"
  [handler options]
  (fn [request]
    (if (valid? request)
      (handler request)
      (if ((options :no-redirect?) request)
        {:status 403}
        (redirect (str (options :cas-server BYU-CAS-server) "/login?service="
                       (create-redirect-url request (:service options) false (:host-override options))))))))

(defn adds-assertion-to-response [resp assertion]
  (assoc-in resp [:session const-cas-assertion] assertion))

(defn ticket [r] (or (get-in r [:query-params artifact-parameter-name])
                     (get-in r [:query-params (keyword artifact-parameter-name)])))


(defn ticket-validation-filter
  [handler options]
  (let [ticket-validator (validator-maker)]
    (fn [request #_{:keys [query-params uri] :as request}]
      (if-let [t (ticket request)]
        (try
          (let [{:keys [query-params uri]} request
                redirect-url (create-redirect-url request (options :service) true (options :host-override))
                assertion (validate ticket-validator t redirect-url)]
            (-> (redirect redirect-url)
                (adds-assertion-to-response assertion)
                ((partial set-timeout (options :timeout 120)))))
          (catch TicketValidationException e
            (println "failed validation")
            (log/error "Ticket validation exception " e)
            {:status 403}))
        (handler request)))))

(defn user-principal-filter
  "Takes username and cas-info from request, and moves them into :username and :cas-info keys in the top level of the request map (rather than being buried in :query-params or :session)."
  [handler]
  (fn [request]
    (if-let [assertion (or
                        (get-in request [:query-params const-cas-assertion])
                        (get-in request [:query-params (keyword const-cas-assertion)])
                        (get-in request [:session const-cas-assertion])
                        (get-in request [:session (keyword const-cas-assertion)]))]
      (do
        (handler (-> request
                            (assoc :username (.getName (.getPrincipal assertion)))
                            (assoc :cas-info (.getAttributes (.getPrincipal assertion)))
                            (let [json (json/read-str (.getAttributes (.getPrincipal assertion)) :key-fn keyword)]
                            {:cas-json json})
                            
                 )))
      (handler request))))


(defn is-logged-in?
  "Takes a request and determines whether the requesting user is logged in or not."
  [req]
  (get-in req [:session const-cas-assertion]))

(defn- logs-out
  "Modifies a response map so as to end the user session.  Note that this does NOT end the CAS session, so users visiting your application will be redirected to CAS (per authentication-filter), and back to your application, only now authorized.  use with gateway parameter only
see ring.middleware.session/bare-session-response if curious how ring sessions work.   https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/middleware/session.clj"
  [resp]
  (assoc resp :session nil))

(defn logout-resp
  "Produces a response map that logs user out of the application (by ending the session) and CAS (by redirecting to the CAS logout endpoint).  Optionally takes a redirect URL which CAS uses to redirect the user (again!) after logout.  Redirect URL should be the *full* URL, including \"https:\""
  ([]
   (logout-resp nil))
  ([redirect-url]
   (let [added-str (if redirect-url
                     (str "?service=" redirect-url)
                     "")]
     (logs-out
      (redirect (str "https://cas.byu.edu/cas/logout" added-str))))))


(defn logout-filter [handler]
  (fn [req]
    (if (and (:session req)
             (or
              (when-let [logout-time (-> req :session :logout-time)]
                (t/> (t/now) logout-time))
              (= "true" (get-in req [:query-params "logout"]))))
      (logout-resp)
      (handler req))))



(defn dependency-string [wrapped]
  (str "wrap-cas requires wrap-" wrapped " to work.  Please make sure to insert Ring's (wrap-" wrapped ") into your middleware stack, after (wrap-cas), like so:
(-> handler
    (wrap-cas)
    (wrap-" wrapped "))
  See https://github.com/ring-clojure/ring/tree/master/ring-core/src/ring/middleware"))

(defn dependency-filter [handler]
  (fn [req]
    (cond (not (contains? req :session))
          (throw (new RuntimeException (dependency-string "session")))

          (not ((every-pred :params :query-params :form-params) req))
          (throw (new RuntimeException (dependency-string "params")))

          :else
          (handler req))))

(defn cas
  ([handler]
   (cas handler {}))
  ([handler options]
   (let [options (merge {:enabled true
                         :no-redirect? (constantly false)
                         :server BYU-CAS-server}
                        options)]
     (if-not (:enabled options)
       handler
       (-> handler
           user-principal-filter
           (authentication-filter options)
           (ticket-validation-filter options)
           (logout-filter)
           (dependency-filter)
           #_(pprints-mw))))))


(defn wrap-cas
  "Middleware that requires the user to authenticate with a CAS server.
  Is dependent on wrap-params and wrap-session; the general call will look something like
  (-> handler
      (wrap-cas :timeout 120)
      (wrap-session)
      (wrap-params))
  The users's username is added to the request map under the :username key.
  Accepts the following options:
    :enabled      - when false, the middleware does nothing
    :no-redirect? - if this predicate function returns true for a request, a
                    403 Forbidden response will be returned instead of a 302
                    Found redirect
    :server 	  - the target cas server
    :timeout      - takes a number representing the  length (in minutes) of the timeout period.  BYU recommends 120 (two hours), see README
    :host-override - host to redirect to after CAS authentication (if nil, host is obtained from request header)
  "
  ([& args]
   (apply cas args)))

