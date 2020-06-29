(ns y-video-back.routes.service-handlers.content-handlers
  (:require
   [y-video-back.db.contents :as contents]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.resources :as resources]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def content-create ;; Non-functional
  {:summary "Creates new content"
   :parameters {:header {:session-id uuid?}
                :body models/content-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-create" 0)
                ru/forbidden-page
                (if-not (collections/EXISTS? (:collection-id body))
                  {:status 500
                   :body {:message "collection not found"}}
                  (if-not (resources/EXISTS? (:resource-id body))
                    {:status 500
                     :body {:message "resource not found"}}
                    (if (contents/EXISTS-COLL-CONT? (:collection-id body) (:resource-id body))
                      {:status 500
                       :body {:message "content connecting collection and resource already exists"}}
                      (let [res (contents/CREATE body)]
                        {:status 200
                         :body {:message "1 content created"
                                :id (utils/get-id res)}}))))))})

(def content-get-by-id
  {:summary "Retrieves specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/content}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-get-by-id" 0)
                ru/forbidden-page
                (let [res (contents/READ id)]
                  (if (nil? res)
                    {:status 404
                     :body {:message "requested content not found"}}
                    {:status 200
                     :body res}))))})

(def content-update ;; Non-functional
  {:summary "Updates the specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/content}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-update" 0)
                ru/forbidden-page
                (if-not (contents/EXISTS? id)
                  {:status 404
                   :body {:message "content not found"}}
                  (let [current-content (contents/READ id)
                        proposed-content (merge current-content body)]
                    (if-not (collections/EXISTS? (:collection-id proposed-content))
                      {:status 500
                       :body {:message "collection not found"}}
                      (if-not (resources/EXISTS? (:resource-id proposed-content))
                        {:status 500
                         :body {:message "resource not found"}}
                        (let [result (contents/UPDATE id body)]
                          {:status 200
                           :body {:message (str result " contents updated")}})))))))})

(def content-delete ;; Non-functional
  {:summary "Deletes the specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-delete" 0)
                ru/forbidden-page
                (let [result (contents/DELETE id)]
                  (if (nil? result)
                    {:status 404
                     :body {:message "content not found"}}
                    {:status 200
                     :body {:message (str result " contents deleted")}}))))})

(def content-add-view
  {:summary "Adds view to content and resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-get-by-id" 0)
                ru/forbidden-page
                {:status 394
                 :body {:message "not implemented yet"}}))})
