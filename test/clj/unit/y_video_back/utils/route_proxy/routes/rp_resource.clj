(ns y-video-back.utils.route-proxy.routes.rp-resource
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))


(defn resource-post
  "Create a resource via app's post request"
  ([session-id resource-without-id]
   (app (-> (request :post "/api/resource")
            (json-body resource-without-id)
            (header :session-id session-id))))
  ([resource-without-id]
   (resource-post (:session-id-bypass env) resource-without-id)))

(defn resource-id-get
  "Retrieves resource via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id))
            (header :session-id session-id))))
  ([id]
   (resource-id-get (:session-id-bypass env) id)))

(defn resource-id-patch
  "Updates resource via app's patch (id) request"
  ([session-id id new-resource]
   (app (-> (request :patch (str "/api/resource/" id))
            (json-body new-resource)
            (header :session-id session-id))))
  ([id new-resource]
   (resource-id-patch (:session-id-bypass env) id new-resource)))

(defn resource-id-delete
  "Deletes resource via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/resource/" id))
            (header :session-id session-id))))
  ([id]
   (resource-id-delete (:session-id-bypass env) id)))

(defn resource-id-collections
  "Reads all collections connected to resource"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (resource-id-collections (:session-id-bypass env) id)))


(defn resource-id-files
  "Reads all files connected to resource"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id "/files"))
            (header :session-id session-id))))
  ([id]
   (resource-id-files (:session-id-bypass env) id)))

(defn resource-id-contents
  "Reads all contents connected to resource"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (resource-id-contents (:session-id-bypass env) id)))
