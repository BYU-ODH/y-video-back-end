(ns y-video-back.routes.home
    (:require
     [y-video-back.layout :as layout]
     [clojure.java.io :as io]
     [ring.util.http-response :as response]
     [y-video-back.middleware :as middleware]
     [y-video-back.user-creator :as uc]))



;(defn home-page [request]
;  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

;(defn about-page [request]
;  (layout/render request "about.html"))

;(defn home-routes []
;  [""
;   {:middleware [middleware/wrap-csrf
;                 middleware/wrap-formats
;   ["/" {:get home-page}]
;   ["/about" {:get about-page}]))



(defn home-page [request-map]
  (layout/hiccup-render-cljs-base {:username request-map}))


(defn hello-page [request]
  (layout/render request "hello.html"))

(defn index-page [request]
  (if (nil? (:username request))
    "failed to provide username"
    (let [session-id (uc/get-session-id (:username request))]
      (println (str "user from CAS: " (:username request)))
      (println (str "serving session-id from home.clj: " session-id))
      (layout/render (into request {:session-id session-id}) "index.html"))))

(defn factor-home [request]
  (layout/render request "fear-no-factor.html"))

(defn factor-about [request]
  (layout/render request "about.html"))

(defn factor-contact [request]
  (layout/render request "contact.html"))

(defn echo-page [request]
  (layout/render request "echo.html"))

(def ^{:private true} home-paths
  ["/"])


(defn home-routes
  "The basic routes to be handled by the SPA (as rendered by fn `home-page`)"
  []
  (into [""
         {:middleware [middleware/wrap-base
                       middleware/wrap-formats]}]
        (conj
         (for [path home-paths]
           [path {:get index-page}])
         ["/ping" {:get (constantly (response/ok {:message "pong"}))}]
         ["/ping-post" {:post (constantly (response/ok {:message "pong"}))}]
         ["/who-am-i" {:get (fn [request] {:status 200 :body {:username (:username request)}})}]
         ["/hello" {:get hello-page}]
         ["/factoring" {:get factor-home}]
         ["/about" {:get factor-about}]
         ["/contact" {:get factor-contact}]
         ["/echo" {:get echo-page}]
         ["/index" {:get index-page}])))
