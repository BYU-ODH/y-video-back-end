(ns y-video-back.utils.route-proxy.routes.rp-home
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

(defn home-page
  "Get request to home page"
  []
  (app (-> (request :get "/"))))
