(ns y-video-postgres-swagger.test.utils
  (:require
    [muuntaja.core :as m]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-postgres-swagger.handler :refer :all]))


(defn insert_user
  [user]
  (let [res ((app) (-> (request :post "/api/user")
                       (json-body user)))]
    (:id (m/decode-response-body res))))

(defn insert_collection
  [collection, user_id]
  (let [res ((app) (-> (request :post "/api/collection")
                       (json-body {:name (:collection_name collection) :user_id user_id})))]
    (:id (m/decode-response-body res))))

(defn insert_content
  [content collection_id]
  (let [res ((app) (-> (request :post "/api/content")
                       (json-body (into content {:collection_id (str collection_id)}))))]
    (:id (m/decode-response-body res))))
