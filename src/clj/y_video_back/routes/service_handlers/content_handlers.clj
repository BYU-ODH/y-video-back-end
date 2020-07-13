(ns y-video-back.routes.service-handlers.content-handlers
  (:require
   [y-video-back.db.contents :as contents]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.subtitles :as subtitles]
   [y-video-back.db.content-subtitles-assoc :as content-subtitles-assoc]
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
                   :body {:message "collection not found"}
                   :headers {"session-id" session-id}}
                  (if-not (resources/EXISTS? (:resource-id body))
                    {:status 500
                     :body {:message "resource not found"}
                     :headers {"session-id" session-id}}
                    (if (contents/EXISTS-COLL-CONT? (:collection-id body) (:resource-id body))
                      {:status 500
                       :body {:message "content connecting collection and resource already exists"}
                       :headers {"session-id" session-id}}
                      (let [res (contents/CREATE body)]
                        {:status 200
                         :body {:message "1 content created"
                                :id (utils/get-id res)}
                         :headers {"session-id" session-id}}))))))})

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
                     :body {:message "requested content not found"}
                     :headers {"session-id" session-id}}
                    {:status 200
                     :body res
                     :headers {"session-id" session-id}}))))})

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
                   :body {:message "content not found"}
                   :headers {"session-id" session-id}}
                  (let [current-content (contents/READ id)
                        proposed-content (merge current-content body)]
                    (if-not (collections/EXISTS? (:collection-id proposed-content))
                      {:status 500
                       :body {:message "collection not found"}
                       :headers {"session-id" session-id}}
                      (if-not (resources/EXISTS? (:resource-id proposed-content))
                        {:status 500
                         :body {:message "resource not found"}
                         :headers {"session-id" session-id}}
                        (let [result (contents/UPDATE id body)]
                          {:status 200
                           :body {:message (str result " contents updated")}
                           :headers {"session-id" session-id}})))))))})

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
                     :body {:message "content not found"}
                     :headers {"session-id" session-id}}
                    {:status 200
                     :body {:message (str result " contents deleted")}
                     :headers {"session-id" session-id}}))))})

(def content-add-view
  {:summary "Adds view to content and resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-get-by-id" 0)
                ru/forbidden-page
                (let [this-content (contents/READ id)]
                  (if (nil? this-content)
                    {:status 404
                     :body {:message "content not found"}
                     :headers {"session-id" session-id}}
                    (let [res-cont (contents/INCR-VIEWS id)]
                      (if (= res-cont [0])
                        {:status 500
                         :body {:message "unable to increment content view, aborting resource increment"}
                         :headers {"session-id" session-id}}
                        (let [res-rsrc (resources/INCR-VIEWS (:resource-id this-content))]
                          (if (= res-rsrc [0])
                            {:status 500
                             :body {:message "content view incremented, but unable to increment resource view"}
                             :headers {"session-id" session-id}}
                            {:status 200
                             :body {:message "incremented views on content and resource"}
                             :headers {"session-id" session-id}}))))))))})

(def content-add-subtitle
  {:summary "Adds subtitle to specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:subtitle-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-add-subtitle" {:content-id id})
                ru/forbidden-page
                (if (not (contents/EXISTS? id))
                  {:status 404
                   :body {:message "content not found"}
                   :headers {"session-id" session-id}}
                  (if (not (subtitles/EXISTS? (:subtitle-id body)))
                    {:status 500
                     :body {:message "subtitle not found"}
                     :headers {"session-id" session-id}}
                    (if (not (contents/ELIGIBLE-CONT-SUB? id (:subtitle-id body)))
                      {:status 500
                       :body {:message "content and subtitle not eligible for connection"}
                       :headers {"session-id" session-id}}
                      (if (content-subtitles-assoc/EXISTS-CONT-SBTL? id (:subtitle-id body))
                        {:status 500
                         :body {:message "subtitle already connected to content"}
                         :headers {"session-id" session-id}}
                        (let [result (utils/get-id (content-subtitles-assoc/CREATE (into body {:content-id id})))]
                          (if (= nil result)
                            {:status 500
                             :body {:message "unable to add subtitle"}
                             :headers {"session-id" session-id}}
                            {:status 200
                             :body {:message (str 1 " subtitles added to content")
                                    :id result}
                             :headers {"session-id" session-id}}))))))))})

(def content-remove-subtitle
  {:summary "Removes subtitle from specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:subtitle-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-remove-subtitle" {:content-id id})
                ru/forbidden-page
                (if (not (contents/EXISTS? id))
                  {:status 404
                   :body {:message "content not found"}
                   :headers {"session-id" session-id}}
                  (if (not (subtitles/EXISTS? (:subtitle-id body)))
                    {:status 500
                     :body {:message "subtitle not found"}
                     :headers {"session-id" session-id}}
                    (if-not (content-subtitles-assoc/EXISTS-CONT-SBTL? id (:subtitle-id body))
                      {:status 500
                       :body {:message "subtitle not connected to content"}
                       :headers {"session-id" session-id}}
                      (let [result (content-subtitles-assoc/DELETE-BY-IDS [id (:subtitle-id body)])]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to remove subtitle"}
                           :headers {"session-id" session-id}}
                          {:status 200
                           :body {:message (str result " subtitles removed from content")}
                           :headers {"session-id" session-id}})))))))})
