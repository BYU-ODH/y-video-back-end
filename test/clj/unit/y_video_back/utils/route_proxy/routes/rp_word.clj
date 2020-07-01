(ns y-video-back.utils.route-proxy.routes.rp-word
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))

(defn word-post
  "Create a word via app's post request"
  ([session-id word-without-id]
   (app (-> (request :post "/api/word")
            (header :session-id session-id)
            (json-body word-without-id))))
  ([word-without-id]
   (word-post (:session-id-bypass env) word-without-id)))

(defn word-id-get
  "Retrieves word via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-get (:session-id-bypass env) id)))

(defn word-id-patch
  "Updates word via app's patch (id) request"
  ([session-id id new-word]
   (app (-> (request :patch (str "/api/word/" id))
            (json-body new-word)
            (header :session-id session-id))))
  ([id new-word]
   (word-id-patch (:session-id-bypass env) id new-word)))

(defn word-id-delete
  "Deletes word via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-delete (:session-id-bypass env) id)))
