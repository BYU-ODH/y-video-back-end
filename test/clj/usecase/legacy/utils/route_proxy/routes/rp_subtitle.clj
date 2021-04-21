(ns legacy.utils.route-proxy.routes.rp-subtitle
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

(defn subtitle-post
  "Create a subtitle via app's post request"
  ([session-id subtitle-without-id]
   (ap2 (-> (request :post "/api/subtitle")
            (json-body subtitle-without-id)
            (header :session-id session-id))))
  ([subtitle-without-id]
   (subtitle-post (:session-id-bypass env) subtitle-without-id)))

(defn subtitle-id-get
  "Retrieves subtitle via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/subtitle/" id))
            (header :session-id session-id))))
  ([id]
   (subtitle-id-get (:session-id-bypass env) id)))

(defn subtitle-id-patch
  "Updates subtitle via app's patch (id) request"
  ([session-id id new-subtitle]
   (ap2 (-> (request :patch (str "/api/subtitle/" id))
            (json-body new-subtitle)
            (header :session-id session-id))))
  ([id new-subtitle]
   (subtitle-id-patch (:session-id-bypass env) id new-subtitle)))

(defn subtitle-id-delete
  "Deletes subtitle via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/subtitle/" id))
            (header :session-id session-id))))
  ([id]
   (subtitle-id-delete (:session-id-bypass env) id)))

