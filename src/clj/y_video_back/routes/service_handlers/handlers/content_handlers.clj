(ns y-video-back.routes.service-handlers.handlers.content-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.subtitles :as subtitles]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as ut]))

(def content-create
  {:summary "Creates new content"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :path-to-id [:parameters :body :collection-id]
   :parameters {:header {:session-id uuid?}
                :body models/content-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters
                  p-vals :permission-values}]
                (if-not (collections/EXISTS? (:collection-id body))
                  {:status 500
                   :body {:message "collection not found"}}
                  (if-not (resources/EXISTS? (:resource-id body))
                    {:status 500
                     :body {:message "resource not found"}}
                    (if (or (= (:session-id-bypass env) (str session-id))
                            (= (ut/to-uuid "00000000-0000-0000-0000-000000000000") (:resource-id body))
                            (ut/has-resource-permission (:resource-id body) (:collection-id body)))
                      (let [new-thumbnail (first (filter #(not (= "" %)) [(:thumbnail body) (ut/get-thumbnail (:url body)) "none"]))
                            res (contents/CREATE (assoc (dissoc body :thumbnail) :thumbnail new-thumbnail))]
                        {:status 200
                         :body {:message "1 content created"
                                :id (ut/get-id res)}})
                      {:status 403
                       :body {:message "collection owner does not have permission to use resource"}}))))})

(def content-get-by-id
  {:summary "Retrieves specified content"
   :permission-level "lab-assistant"
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
   ; TODO - do not return contents with expired resource-access

(def content-update
  {:summary "Updates the specified content"
   :permission-level "lab-assistant"
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/content}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path {:keys [session-id]} :header :keys [body]} :parameters}]
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
                      (if (or (= (:session-id-bypass env) (str session-id))
                              (= (ut/to-uuid "00000000-0000-0000-0000-000000000000") (:resource-id body))
                              (ut/has-resource-permission (:resource-id body) (:collection-id body)))
                          (let [result (contents/UPDATE id body)]
                            {:status 200
                             :body {:message (str result " contents updated")}})
                          {:status 403
                           :body {:message "collection owner does not have permission to use resource"}}))))))})

(def content-delete
  {:summary "Deletes the specified content"
   :permission-level "admin"
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
   :permission-level "lab-assistant"
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

(def content-subtitles
  {:summary "Retrieve all subtitles connected to content"
   :permission-level "lab-assistant"
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

(def content-clone-subtitle
  {:summary "Clone subtitle to content"
   :permission-level "lab-assistant"
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}
                :body {:subtitle-id uuid?}}
   :responses {200 {:body {:message string?
                           :id string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path {:keys [subtitle-id]} :body} :parameters}]
              (if (not (contents/EXISTS? id))
                {:status 404
                 :body {:message "content not found"}}
                (if (not (subtitles/EXISTS? subtitle-id))
                  {:status 404
                   :body {:message "subtitle not found"}}
                  (let [sbtl-one (subtitles/READ subtitle-id)
                        add-res (subtitles/CREATE (-> sbtl-one
                                                      (ut/remove-db-only)
                                                      (dissoc :id)
                                                      (dissoc :content-id)
                                                      (assoc :content-id id)))]
                    {:status 200
                     :body {:message "1 subtitle cloned"
                            :id (ut/get-id add-res)}}))))})
