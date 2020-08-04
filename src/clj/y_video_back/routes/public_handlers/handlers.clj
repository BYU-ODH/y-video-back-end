(ns y-video-back.routes.public-handlers.handlers
  (:require
   [y-video-back.db.users-by-collection :as users-by-collection]
   [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.users :as users]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.courses :as courses]
   [y-video-back.db.contents :as contents]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]))


(def public-collection-get-by-id ;; Not tested
  {:summary "Retrieves specified public collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/collection}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (collections/READ-PUBLIC id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body res})))})

(def public-collection-get-all ;; Non-functional
  {:summary "Retrieves all the resources for the specified collection"
   :responses {200 {:body [models/collection]}
               404 (:body {:message string?})}
   :handler (fn [req]
              (let [raw-res (collections/READ-ALL-PUBLIC)
                    res (map #(utils/remove-db-only %) raw-res)]
                {:status 200
                 :body res}))})

(def public-content-get-by-id ;; Not tested
  {:summary "Retrieves specified public content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/content}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (contents/READ-PUBLIC id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body res})))})

(def public-content-get-all ;; Non-functional
  {:summary "Retrieves all the resources for the specified content"
   :responses {200 {:body [models/content]}
               404 (:body {:message string?})}
   :handler (fn [req]
              (let [raw-res (contents/READ-ALL-PUBLIC)
                    res (map #(utils/remove-db-only %) raw-res)]
                {:status 200
                 :body res}))})

(def public-resource-get-by-id ;; Not tested
  {:summary "Retrieves specified public resource"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/resource}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (resources/READ-PUBLIC id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested resource not found"}}
                  {:status 200
                   :body res})))})

(def public-resource-get-all ;; Non-functional
  {:summary "Retrieves all the resources for the specified resource"
   :responses {200 {:body [models/resource]}
               404 (:body {:message string?})}
   :handler (fn [req]
              (let [raw-res (resources/READ-ALL-PUBLIC)
                    res (map #(utils/remove-db-only %) raw-res)]
                {:status 200
                 :body res}))})
