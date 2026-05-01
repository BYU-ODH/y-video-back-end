(ns y-video-back.middleware.exception
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [expound.alpha :as expound]
            [reitit.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]))

(defn coercion-error-handler [status]
  (let [printer (expound/custom-printer {:print-specs? false})]
    (fn [exception request]
      {:status status
       :headers {"Content-Type" "text/html"}
       :body (with-out-str (printer (-> exception ex-data :problems)))})))

(defn- coercion-error? [e]
  (contains? #{:reitit.coercion/request-coercion
               :reitit.coercion/response-coercion}
             (:type (ex-data e))))

(defn- summarize-problem [p]
  (str (pr-str (:path p)) " expected " (:pred p) ", got " (pr-str (:val p))))

(defn- log-coercion-warn [e request]
  (let [problems (-> e ex-data :problems :clojure.spec.alpha/problems)
        summary (str/join "; " (map summarize-problem problems))
        method (some-> request :request-method name str/upper-case)
        uri (:uri request)]
    (log/warn (str "Request coercion failed: " method " " uri " — " summary))))

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {;; log stack-traces for all exceptions except coercion failures, which get a concise one-liner
     ::exception/wrap (fn [handler e request]
                        (if (coercion-error? e)
                          (log-coercion-warn e request)
                          (log/error e (.getMessage e)))
                        (handler e request))
     ;; human-optimized validation messages
     ::coercion/request-coercion (coercion-error-handler 400)
     ::coercion/response-coercion (coercion-error-handler 500)})))
