(ns legacy.utils.route-proxy.routes.rp-resource
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

(defn resource-post
  "Create a resource via app's post request"
  ([session-id resource-without-id]
   (ap2 (-> (request :post "/api/resource")
            (json-body resource-without-id)
            (header :session-id session-id))))
  ([resource-without-id]
   (resource-post (:session-id-bypass env) resource-without-id)))

(defn resource-id-get
  "Retrieves resource via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/resource/" id))
            (header :session-id session-id))))
  ([id]
   (resource-id-get (:session-id-bypass env) id)))

(defn resource-id-patch
  "Updates resource via app's patch (id) request"
  ([session-id id new-resource]
   (ap2 (-> (request :patch (str "/api/resource/" id))
            (json-body new-resource)
            (header :session-id session-id))))
  ([id new-resource]
   (resource-id-patch (:session-id-bypass env) id new-resource)))

(defn resource-id-delete
  "Deletes resource via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/resource/" id))
            (header :session-id session-id))))
  ([id]
   (resource-id-delete (:session-id-bypass env) id)))

(defn resource-id-collections
  "Reads all collections connected to resource"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/resource/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (resource-id-collections (:session-id-bypass env) id)))


(defn resource-id-files
  "Reads all files connected to resource"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/resource/" id "/files"))
            (header :session-id session-id))))
  ([id]
   (resource-id-files (:session-id-bypass env) id)))

(defn resource-id-contents
  "Reads all contents connected to resource"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/resource/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (resource-id-contents (:session-id-bypass env) id)))

(defn resource-id-subtitles
  "Reads all subtitles connected to resource"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/resource/" id "/subtitles"))
            (header :session-id session-id))))
  ([id]
   (resource-id-subtitles (:session-id-bypass env) id)))

(defn resource-add-access
  "Give username permission to create contents with resource"
  ([session-id username resource-id]
   (ap2 (-> (request :post (str "/api/resource/" resource-id "/add-access"))
            (json-body {:username username})
            (header :session-id session-id))))
  ([username resource-id]
   (resource-add-access (:session-id-bypass env) username resource-id)))

(defn resource-remove-access
  "Remove username permission to create contents with resource"
  ([session-id username resource-id]
   (ap2 (-> (request :delete (str "/api/resource/" resource-id "/remove-access"))
            (json-body {:username username})
            (header :session-id session-id))))
  ([username resource-id]
   (resource-remove-access (:session-id-bypass env) username resource-id)))

(defn resource-read-all-access
  "Remove username permission to create contents with resource"
  ([session-id resource-id]
   (ap2 (-> (request :get (str "/api/resource/" resource-id "/read-all-access"))
            (header :session-id session-id))))
  ([resource-id]
   (resource-read-all-access (:session-id-bypass env) resource-id)))

