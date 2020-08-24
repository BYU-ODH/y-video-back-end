(ns y-video-back.utils.route-proxy.routes.rp-language
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

(defn language-post
  "Create a language via app's post request"
  ([session-id language-without-id]
   (ap2 (-> (request :post "/api/language")
            (json-body language-without-id)
            (header :session-id session-id))))
  ([language-without-id]
   (language-post (:session-id-bypass env) language-without-id)))

(defn language-id-get
  "Retrieves language via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/language/" id))
            (header :session-id session-id))))
  ([id]
   (language-id-get (:session-id-bypass env) id)))

(defn language-id-patch
  "Updates language via app's patch (id) request"
  ([session-id id new-language]
   (ap2 (-> (request :patch (str "/api/language/" id))
            (json-body new-language)
            (header :session-id session-id))))
  ([id new-language]
   (language-id-patch (:session-id-bypass env) id new-language)))

(defn language-id-delete
  "Deletes language via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/language/" id))
            (header :session-id session-id))))
  ([id]
   (language-id-delete (:session-id-bypass env) id)))

(defn language-get-all
  "Retrieves all languages in db"
  ([session-id]
   (ap2 (-> (request :get (str "/api/languages"))
            (header :session-id session-id))))
  ([]
   (language-get-all (:session-id-bypass env))))
