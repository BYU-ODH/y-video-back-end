(ns legacy.utils.route-proxy.routes.rp-home
  (:require
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.utils.utils :as ut]))

(defn ap2
  [arg]
  (let [res (app arg)]
    (ut/check-header res)
    res))

(defn home-page
  "Get request to home page"
  []
  (app (-> (request :get "/"))))


