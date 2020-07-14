(ns y-video-back.utils.route-proxy.routes.rp-file
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [y-video-back.utils.utils :as ut]))

(defn ap2
  [arg]
  (let [res (app arg)]
    (ut/check-header res)
    res))

(defn file-post
  "Create a file via app's post request"
  ([session-id file-without-id]
   (ap2 (-> (request :post "/api/file")
            (json-body file-without-id)
            (header :session-id session-id))))
  ([file-without-id]
   (file-post (:session-id-bypass env) file-without-id)))

(defn file-id-get
  "Retrieves file via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/file/" id))
            (header :session-id session-id))))
  ([id]
   (file-id-get (:session-id-bypass env) id)))

(defn file-id-patch
  "Updates file via app's patch (id) request"
  ([session-id id new-file]
   (ap2 (-> (request :patch (str "/api/file/" id))
            (json-body new-file)
            (header :session-id session-id))))
  ([id new-file]
   (file-id-patch (:session-id-bypass env) id new-file)))

(defn file-id-delete
  "Deletes file via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/file/" id))
            (header :session-id session-id))))
  ([id]
   (file-id-delete (:session-id-bypass env) id)))
