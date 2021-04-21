(ns legacy.utils.route-proxy.routes.rp-media
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

(defn get-file-key
  ([session-id file-id]
   (ap2 (-> (request :get (str "/api/media/get-file-key/" file-id))
            (header :session-id session-id))))
  ([file-id]
   (get-file-key (:session-id-bypass env) file-id)))

(defn stream-media
  [file-key]
  (app (-> (request :get (str "/api/media/stream-media/" file-key)))))

(comment (defn file-post)
  "Create a file via app's post request"
  ([session-id file-without-id]
   (ap2 (-> (request :post "/api/file")
            (json-body file-without-id)
            (header :session-id session-id))))
  ([file-without-id]
   (file-post (:session-id-bypass env) file-without-id)))

