(ns y-video-back.routes.service-handlers.annotation-handlers
  (:require
   [y-video-back.db.annotations :as annotations]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.resources :as resources]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def annotation-create ;; Non-functional
  {:summary "Creates new annotation"
   :parameters {:header {:session-id uuid?}
                :body models/annotation-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "annotation-create" 0)
                ru/forbidden-page
                (if-not (collections/EXISTS? (:collection-id body))
                  {:status 500
                   :body {:message "collection not found"}}
                  (if-not (resources/EXISTS? (:resource-id body))
                    {:status 500
                     :body {:message "resource not found"}}
                    (if (annotations/EXISTS-COLL-CONT? (:collection-id body) (:resource-id body))
                      {:status 500
                       :body {:message "annotation connecting collection and resource already exists"}}
                      (let [res (annotations/CREATE body)]
                        {:status 200
                         :body {:message "1 annotation created"
                                :id (utils/get-id res)}}))))))})

(def annotation-get-by-id
  {:summary "Retrieves specified annotation"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/annotation}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "annotation-get-by-id" 0)
                ru/forbidden-page
                (let [res (annotations/READ id)]
                  (if (nil? res)
                    {:status 404
                     :body {:message "requested annotation not found"}}
                    {:status 200
                     :body res}))))})

(def annotation-update ;; Non-functional
  {:summary "Updates the specified annotation"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/annotation}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "annotation-update" 0)
                ru/forbidden-page
                (if-not (annotations/EXISTS? id)
                  {:status 404
                   :body {:message "annotation not found"}}
                  (let [current-annotation (annotations/READ id)
                        proposed-annotation (merge current-annotation body)
                        same-name-annotation (first (annotations/READ-BY-IDS [(:collection-id proposed-annotation)
                                                                              (:resource-id proposed-annotation)]))]
                    ; If there is a collision and the collision is not with self (i.e. annotation being changed)
                    (if (and (not (nil? same-name-annotation))
                             (not (= (:id current-annotation)
                                     (:id same-name-annotation))))
                      {:status 500
                       :body {:message "unable to update annotation, annotation between resource and collection likely already exists"}}
                      (if-not (collections/EXISTS? (:collection-id proposed-annotation))
                        {:status 500
                         :body {:message "collection not found"}}
                        (if-not (resources/EXISTS? (:resource-id proposed-annotation))
                          {:status 500
                           :body {:message "resource not found"}}
                          (let [result (annotations/UPDATE id body)]
                            {:status 200
                             :body {:message (str result " annotations updated")}}))))))))})

(def annotation-delete ;; Non-functional
  {:summary "Deletes the specified annotation"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "annotation-delete" 0)
                ru/forbidden-page
                (let [result (annotations/DELETE id)]
                  (if (nil? result)
                    {:status 404
                     :body {:message "annotation not found"}}
                    {:status 200
                     :body {:message (str result " annotations deleted")}}))))})

(def annotation-get-by-collection-and-resource
  {:summary "Gets annotations by collection and resource ids"
   :parameters {:header {:session-id uuid?}
                :body {:collection-id uuid?
                       :resource-id uuid?}}
   :responses {200 {:body [models/annotation]}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "annotation-create" 0)
                ru/forbidden-page
                (if-not (collections/EXISTS? (:collection-id body))
                  {:status 500
                   :body {:message "collection not found"}}
                  (if-not (resources/EXISTS? (:resource-id body))
                    {:status 500
                     :body {:message "resource not found"}}
                    (let [res (annotations/READ-BY-IDS [(:collection-id body)
                                                        (:resource-id body)])]
                      (if (= 0 (count res))
                        {:status 404
                         :body {:message "annotation not found"}}
                        {:status 200
                         :body res}))))))})
