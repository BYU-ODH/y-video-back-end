(ns legacy.utils.route-proxy.routes.rp-word
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

(defn word-post
  "Create a word via app's post request"
  ([session-id word-without-id]
   (ap2 (-> (request :post "/api/word")
            (header :session-id session-id)
            (json-body word-without-id))))
  ([word-without-id]
   (word-post (:session-id-bypass env) word-without-id)))

(defn word-id-get
  "Retrieves word via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-get (:session-id-bypass env) id)))

(defn word-id-patch
  "Updates word via app's patch (id) request"
  ([session-id id new-word]
   (ap2 (-> (request :patch (str "/api/word/" id))
            (json-body new-word)
            (header :session-id session-id))))
  ([id new-word]
   (word-id-patch (:session-id-bypass env) id new-word)))

(defn word-id-delete
  "Deletes word via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-delete (:session-id-bypass env) id)))

