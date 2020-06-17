(ns y-video-back.routes.service-handlers.annotation-handlers
  (:require
   [y-video-back.db.annotations :as annotations]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def annotation-create ;; Non-functional
  {:summary "Creates new annotation"
   :parameters {:header {:session-id uuid?}
                :body models/annotation-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "annotation-create" 0)
                ru/forbidden-page
                (try {:status 200
                      :body {:message "1 annotation created"
                             :id (utils/get-id (annotations/CREATE body))}}
                     (catch Exception e
                       {:status 409
                        :body {:message "unable to create annotation, likely bad collection id"
                               :error e}}))))})

(def annotation-get-by-id
  {:summary "Retrieves specified annotation"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/annotation}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "annotation-get-by-id" 0)
                ru/forbidden-page
                (let [annotation-result (annotations/READ id)]
                  {:status 200
                   :body annotation-result})))})

(def annotation-update ;; Non-functional
  {:summary "Updates the specified annotation"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/annotation}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "annotation-update" 0)
                ru/forbidden-page
                (let [result (annotations/UPDATE id body)]
                  {:status 200
                   :body {:message (str result " annotations updated")}})))})

(def annotation-delete ;; Non-functional
  {:summary "Deletes the specified annotation"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "annotation-delete" 0)
                ru/forbidden-page
                (let [result (annotations/DELETE id)]
                  {:status 200
                   :body {:message (str result " annotations deleted")}})))})

(def annotation-get-by-collection-and-content
  {:summary "Gets annotations by collection and content ids"
   :parameters {:header {:session-id uuid?}
                :body {:collection-id uuid?
                       :content-id uuid?}}
   :responses {200 {:body [models/annotation]}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "annotation-create" 0)
                ru/forbidden-page
                (let [res (annotations/READ-BY-IDS [(:collection-id body)
                                                    (:content-id body)])]
                  {:status 200
                   :body res})))})
