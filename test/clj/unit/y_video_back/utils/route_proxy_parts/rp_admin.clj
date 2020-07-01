(ns y-video-back.utils.route-proxy-parts.rp-admin
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))


(defn search
  "Searches by query-term"
  ([session-id query-term]
   (app (-> (request :get (str "/api/search?query-term=" (java.net.URLEncoder/encode query-term)))
            (header :session-id session-id))))
  ([query-term]
   (search (:session-id-bypass env) query-term)))


(defn search-by-user
  "Search user table by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/user/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-user (:session-id-bypass env) term)))

(defn search-by-collection
  "Search collection table by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/collection/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-collection (:session-id-bypass env) term)))

(defn search-by-resource
  "Search resource table by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/resource/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-resource (:session-id-bypass env) term)))
