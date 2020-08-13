(ns y-video-back.routes.service-handlers.handlers.content-handlers
  (:require
   [y-video-back.db.contents :as contents]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.subtitles :as subtitles]
   [y-video-back.db.content-subtitles-assoc :as content-subtitles-assoc]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [y-video-back.routes.service-handlers.utils.utils :as ut]))

(def content-create ;; Non-functional
  {:summary "Creates new content"
   :permission-level 1
   :role-level "instructor"
   :path-to-id [:parameters :body :collection-id]
   :parameters {:header {:session-id uuid?}
                :body models/content-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (if-not (collections/EXISTS? (:collection-id body))
                {:status 500
                 :body {:message "collection not found"}}
                (if-not (resources/EXISTS? (:resource-id body))
                  {:status 500
                   :body {:message "resource not found"}}
                  ;(if (contents/EXISTS-COLL-CONT? (:collection-id body) (:resource-id body))
                  ;  {:status 500
                  ;   :body {:message "content connecting collection and resource already exists"}
                  (let [new-thumbnail (first (filter #(not (= "" %)) [(:thumbnail body) (utils/get-thumbnail (:url body)) "none"]))
                        res (contents/CREATE (assoc (dissoc body :thumbnail) :thumbnail new-thumbnail))]
                    {:status 200
                     :body {:message "1 content created"
                            :id (utils/get-id res)}}))))})

(def content-get-by-id
  {:summary "Retrieves specified content"
   :permission-level 1
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/content}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (contents/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body res})))})

(def content-update ;; Non-functional
  {:summary "Updates the specified content"
   :permission-level 1
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/content}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
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
                         :body {:message (str result " contents updated")}}))))))})

(def content-delete ;; Non-functional
  {:summary "Deletes the specified content"
   :permission-level 0
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (contents/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "content not found"}}
                  {:status 200
                   :body {:message (str result " contents deleted")}})))})

(def content-add-view
  {:summary "Adds view to content and resource"
   :permission-level 1
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [this-content (contents/READ id)]
                (if (nil? this-content)
                  {:status 404
                   :body {:message "content not found"}}
                  (let [res-cont (contents/INCR-VIEWS id)]
                    (if (= res-cont [0])
                      {:status 500
                       :body {:message "unable to increment content view, aborting resource increment"}}
                      (let [res-rsrc (resources/INCR-VIEWS (:resource-id this-content))]
                        (if (= res-rsrc [0])
                          {:status 500
                           :body {:message "content view incremented, but unable to increment resource view"}}
                          {:status 200
                           :body {:message "incremented views on content and resource"}})))))))})

(comment (def content-add-subtitle)
  {:summary "Adds subtitle to specified content"
   :permission-level 1
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:subtitle-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if (not (contents/EXISTS? id))
                {:status 404
                 :body {:message "content not found"}}
                (if (not (subtitles/EXISTS? (:subtitle-id body)))
                  {:status 500
                   :body {:message "subtitle not found"}}
                  ;(if (not (contents/ELIGIBLE-CONT-SUB? id (:subtitle-id body)))
                  ;  {:status 500
                  ;   :body {:message "content and subtitle not eligible for connection"}
                  ;(if (content-subtitles-assoc/EXISTS-CONT-SBTL? id (:subtitle-id body))
                  ;  {:status 500
                  ;   :body {:message "subtitle already connected to content"}
                  (let [result (utils/get-id (content-subtitles-assoc/CREATE (into body {:content-id id})))]
                    (if (= nil result)
                      {:status 500
                       :body {:message "unable to add subtitle"}}
                      {:status 200
                       :body {:message (str 1 " subtitles added to content")
                              :id result}})))))})

(comment (def content-remove-subtitle)
  {:summary "Removes subtitle from specified content"
   :permission-level 1
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:subtitle-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if (not (contents/EXISTS? id))
                {:status 404
                 :body {:message "content not found"}}
                (if (not (subtitles/EXISTS? (:subtitle-id body)))
                  {:status 500
                   :body {:message "subtitle not found"}}
                  (if-not (content-subtitles-assoc/EXISTS-CONT-SBTL? id (:subtitle-id body))
                    {:status 500
                     :body {:message "subtitle not connected to content"}}
                    (let [result (content-subtitles-assoc/DELETE-BY-IDS [id (:subtitle-id body)])]
                      (if (= 0 result)
                        {:status 500
                         :body {:message "unable to remove subtitle"}}
                        {:status 200
                         :body {:message (str result " subtitles removed from content")}}))))))})


(def content-subtitles
  {:summary "Retrieve all subtitles connected to content"
   :permission-level 1
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/subtitle]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (contents/EXISTS? id))
                {:status 404
                 :body {:message "content not found"}}
                (let [result (subtitles/READ-BY-CONTENT-ID id)]
                  {:status 200
                   :body (map #(-> %
                                   (ut/remove-db-only))
                              result)})))})
