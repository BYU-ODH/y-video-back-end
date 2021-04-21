(ns legacy.utils.route-proxy.routes.rp-public
  (:require
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))

; get collection by id
(defn collection-id-get
  "Retrieves public collection"
  [id]
  (app (-> (request :get (str "/public/collection/" id)))))

; get all collections
(defn collection-get-all
  []
  (app (-> (request :get "/public/collections"))))

; get content by id
(defn content-id-get
  "Retrieves public content"
  [id]
  (app (-> (request :get (str "/public/content/" id)))))

; get all contents
(defn content-get-all
  []
  (app (-> (request :get "/public/contents"))))

; get resource by id
(defn resource-id-get
  "Retrieves public resource"
  [id]
  (app (-> (request :get (str "/public/resource/" id)))))

; get all resources
(defn resource-get-all
  []
  (app (-> (request :get "/public/resources"))))
