(ns y-video-back.routes.home
    (:require
     [y-video-back.layout :as layout]
     [ring.util.http-response :as response]
     [ring.util.response :refer [redirect]]
     [y-video-back.middleware :as middleware]
     [y-video-back.user-creator :as uc]
     [y-video-back.course-creator :refer [check-courses-with-api]]
     [byu-cas.core :as cas]
     [y-video-back.routes.services :refer [service-routes]]))

(defn home-page [request-map]
  (layout/hiccup-render-cljs-base {:username request-map}))

(defn hello-page [request]
  (layout/render request "hello.html"))

(defn index-page [request]
  (println (get-in request [:query-params]))
  (println (get-in request [:query-params]))
  (if (nil? (:username request))
    (response/ok {:message "CAS failed to provide username"})
    (let [session-id (uc/get-session-id (:username request))] ; temporary fix
      (println "checking user courses")
      (check-courses-with-api (:username request))
      (println (str "user from CAS: " (:username request)))
      (println (str "serving session-id from home.clj: " session-id))
      (layout/render (into request {:session-id session-id}) "index.html"))))

(defn get-routes-r
  "Recursive helper for get-routes"
  [arg]
  ;(println "arg=" arg)
  (let [key (get arg 0) ; string uri piece
        val (get arg 1)] ; map - potentially endpoint info
    ;(println "key=" key)
    ;(println "val=" val)
    (if (contains? val :swagger)
      (do
        ;(println "swagger")
        (reduce concat
          (map (fn [a] (let [res (get-routes-r a)]
                         (vec (map (fn [b] (list (str key (get b 0))
                                                 (get b 1)
                                                 (get b 2)))
                                   res))))
               (subvec arg 2))))
      (map (fn [c] (vec (list key c (get val c))))
           (keys val)))))


        ; (map #(let [res (get-routes-r %)]
        ;         (list (str key (get res 0))
        ;               (get res 1)))
        ;      (subvec arg 2))]])))


(defn get-routes
  "Gets only route-specific information from arg"
  [arg]
  (let [all-routes (map #(vec (get-routes-r %)) (subvec arg 2))]
    (reduce concat all-routes)))
  ; (map #(list (str (get arg 0) (get % 0))
  ;             (get-routes-r %))
  ;       (subvec arg 2)))

(defn permission-docs-page [request]
  (layout/render request "permissions.html" {:routes (get-routes (service-routes))
                                             :message "test message!"}))

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
         ;["/show-request" {:get (fn [request] {:status 200 :body {:request (str request)}})}]
         ["/permission-docs" {:get permission-docs-page}]

         ;["/logout" {:get {:handler (fn [req] (cas/logout-resp "https://cheneycreations.com"))}}] ; placeholder url until we get a login page going
         ;["/logout" {:get {:handler (redirect (str "/?logout=true"))}}]
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
