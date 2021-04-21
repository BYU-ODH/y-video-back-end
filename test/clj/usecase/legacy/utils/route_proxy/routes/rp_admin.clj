(ns legacy.utils.route-proxy.routes.rp-admin
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

(defn search
  "Searches by query-term"
  ([session-id query-term]
   (ap2 (-> (request :get (str "/api/search?query-term=" (java.net.URLEncoder/encode query-term)))
            (header :session-id session-id))))
  ([query-term]
   (search (:session-id-bypass env) query-term)))


(defn search-by-user
  "Search user table by term"
  ([session-id term]
   (ap2 (-> (request :get (str "/api/admin/user/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-user (:session-id-bypass env) term)))

(defn search-by-collection
  "Search collection table by term"
  ([session-id term]
   (ap2 (-> (request :get (str "/api/admin/collection/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-collection (:session-id-bypass env) term)))

(defn search-public-collections
  "Search public collection view by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/public-collection/" (java.net.URLEncoder/encode term))))))
  ([term]
   (search-public-collections (:session-id-bypass env) term)))


(defn search-by-content
  "Search content table by term"
  ([session-id term]
   (ap2 (-> (request :get (str "/api/admin/content/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-content (:session-id-bypass env) term)))

(defn search-by-resource
  "Search resource table by term"
  ([session-id term]
   (ap2 (-> (request :get (str "/api/admin/resource/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-resource (:session-id-bypass env) term)))

