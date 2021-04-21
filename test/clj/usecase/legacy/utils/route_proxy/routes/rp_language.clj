(ns legacy.utils.route-proxy.routes.rp-language
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

(defn language-post
  "Create a language via app's post request"
  ([session-id language]
   (ap2 (-> (request :post "/api/language")
            (json-body language)
            (header :session-id session-id))))
  ([language]
   (language-post (:session-id-bypass env) language)))

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
   (ap2 (-> (request :get (str "/api/language"))
            (header :session-id session-id))))
  ([]
   (language-get-all (:session-id-bypass env))))

