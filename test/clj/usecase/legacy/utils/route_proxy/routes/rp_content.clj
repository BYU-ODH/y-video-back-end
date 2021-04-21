(ns legacy.utils.route-proxy.routes.rp-content
  (:require
    [y-video-back.config :refer [env]]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.utils.utils :as ut]))

(defn ap2
  [arg]
  (let [res (app arg)]
    (ut/check-header res)
    res))

(defn content-id-add-view
  "Adds a view to content"
  ([session-id id]
   (ap2 (-> (request :post (str "/api/content/" id "/add-view"))
            (header :session-id session-id))))
  ([id]
   (content-id-add-view (:session-id-bypass env) id)))

(defn content-post
  "Create a content via app's post request"
  ([session-id content-without-id]
   (ap2 (-> (request :post "/api/content")
            (json-body content-without-id)
            (header :session-id session-id))))
  ([content-without-id]
   (content-post (:session-id-bypass env) content-without-id)))

(defn content-id-get
  "Retrieves content via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-get (:session-id-bypass env) id)))

(defn content-id-patch
  "Updates content via app's patch (id) request"
  ([session-id id new-content]
   (ap2 (-> (request :patch (str "/api/content/" id))
            (json-body new-content)
            (header :session-id session-id))))
  ([id new-content]
   (content-id-patch (:session-id-bypass env) id new-content)))

(defn content-id-delete
  "Deletes content via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-delete (:session-id-bypass env) id)))

(defn content-id-add-subtitle
  "Connects subtitle and content"
  ([session-id content-id subtitle-id]
   (ap2 (-> (request :post (str "/api/content/" content-id "/add-subtitle"))
            (header :session-id session-id)
            (json-body {:subtitle-id subtitle-id}))))
  ([content-id subtitle-id]
   (content-id-add-subtitle (:session-id-bypass env) content-id subtitle-id)))

(defn content-id-remove-subtitle
  "Connects subtitle and content"
  ([session-id content-id subtitle-id]
   (ap2 (-> (request :post (str "/api/content/" content-id "/remove-subtitle"))
            (json-body {:subtitle-id subtitle-id})
            (header :session-id session-id))))
  ([content-id subtitle-id]
   (content-id-remove-subtitle (:session-id-bypass env) content-id subtitle-id)))

(defn content-id-subtitles
  "Retrieves all subtitles for content via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/content/" id "/subtitles"))
            (header :session-id session-id))))
  ([id]
   (content-id-subtitles (:session-id-bypass env) id)))

(defn content-id-clone-subtitle
  "Copies subtitle with id as child of content"
  ([session-id content-id subtitle-id]
   (ap2 (-> (request :post (str "/api/content/" content-id "/clone-subtitle"))
            (json-body {:subtitle-id subtitle-id})
            (header :session-id session-id))))
  ([content-id subtitle-id]
   (content-id-clone-subtitle (:session-id-bypass env) content-id subtitle-id)))

