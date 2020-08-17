(ns y-video-back.routes.home
    (:require
     [y-video-back.layout :as layout]
     [ring.util.http-response :as response]
     [y-video-back.middleware :as middleware]
     [y-video-back.user-creator :as uc]
     [byu-cas.core :as cas]))

(defn home-page [request-map]
  (layout/hiccup-render-cljs-base {:username request-map}))

(defn hello-page [request]
  (layout/render request "hello.html"))

(defn index-page [request]
  (println (get-in request [:query-params]))
  (println (get-in request [:query-params]))
  (if (nil? (:username request))
    (response/ok {:message "CAS failed to provide username"})
    (let [session-id (uc/get-session-id (:username request) true)] ; temporary fix
      (println (str "user from CAS: " (:username request)))
      (println (str "serving session-id from home.clj: " session-id))
      (layout/render (into request {:session-id session-id}) "index.html"))))

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

         ; dev routes
         ["/ping" {:get (constantly (response/ok {:message "pong"}))}]
         ["/hello" {:get hello-page}]
         ["/who-am-i" {:get (fn [request] {:status 200 :body {:username (:username request)}})}]
         ["/show-request" {:get (fn [request] {:status 200 :body {:request (str request)
                                                                  :cas-info (:cas-info request)}})}]

         ;["/logout" {:get {:handler (fn [req] (cas/logout-resp "https://cheneycreations.com"))}}] ; placeholder url until we get a login page going
         ["/logout" {:get {:handler (cas/logout-resp "https://cheneycreations.com")}}] ; placeholder url until we get a login page going
         ; serving videos routes

         ; React BrowserRouter support
         ["/index" {:get index-page}]
         ["/admin" {:get index-page}]
         ["/collections" {:get index-page}]
         ["/lab-assistant" {:get index-page}]
         ["/lab-assistant-manager/:professorId" {:get index-page}]
         ["/lab-assistant-manager/:professorId/:collectionId" {:get index-page}]
         ["/manager" {:get index-page}]
         ["/manager/:id" {:get index-page}]
         ["/player/:id" {:get index-page}]
         ["/trackeditor/:id" {:get index-page}])))
