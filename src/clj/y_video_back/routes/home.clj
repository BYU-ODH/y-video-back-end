(ns y-video-back.routes.home
    (:require
     [y-video-back.config :refer [env]]
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
      (layout/render request "index.html" {:logged-in false})
    (let [session-id (uc/get-session-id (:username request))]
      (println "checking user courses")
      (check-courses-with-api (:username request))
      (println (str "user from CAS: " (:username request)))
      (println (str "serving session-id from home.clj: " session-id))
      (layout/render (into request {:session-id session-id}) "index.html" {:logged-in true}))))

(defn get-routes-r
  "Recursive helper for get-routes"
  [arg]
  (let [key (get arg 0) ; string uri piece
        val (get arg 1)] ; map - potentially endpoint info
    (if (contains? val :swagger)
      (do
        (reduce concat
          (map (fn [a] (let [res (get-routes-r a)]
                         (vec (map (fn [b] (list (str key (get b 0))
                                                 (get b 1)
                                                 (get b 2)))
                                   res))))
               (subvec arg 2))))
      (map (fn [c] (vec (list key c (get val c))))
           (keys val)))))

(defn get-routes
  "Gets only route-specific information from arg"
  [arg]
  (let [all-routes (map #(vec (get-routes-r %)) (subvec arg 2))]
    (sort #(compare (first %1) (first %2))
          (reduce concat all-routes))))

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

         ; Direct-to-back-end routes
         ["/permission-docs" {:get permission-docs-page}]
         ["/login" {:get (constantly (redirect "/"))}]

         ["/logout" {:get {:handler (fn [req] (cas/logout-resp (:host env)))}}] ; placeholder url until we get a login page going

         ; React BrowserRouter support
         ; These should match yvideo-client/src/components/c/Root/index.jsx
         ["/index" {:get index-page}]
         ["/search-public-collections" (:get index-page)]
         ["/admin" {:get index-page}]
         ["/collections" {:get index-page}]
         ["/lab-assistant" {:get index-page}]
         ["/manage-resource" {:get index-page}]
         ["/lab-assistant-manager/:professorId" {:get index-page}]
         ["/lab-assistant-manager/:professorId/:collectionId" {:get index-page}]
         ["/manager" {:get index-page}]
         ["/manager/:id" {:get index-page}]
         ["/public-manager/:id?" (:get index-page)]
         ["/player/:id" {:get index-page}]
         ["/videoeditor/:id" {:get index-page}]
         ["/subtitleeditor/:id" {:get index-page}]
         ["/clipeditor/:id" {:get index-page}]
         ["/feedback" {:get index-page}])))
