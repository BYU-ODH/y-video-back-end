(ns y-video-back.routes.service_handlers.annotation_handlers
  (:require
   [y-video-back.db.annotations :as annotations]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))


(def annotation-create ;; Non-functional
  {:summary "Creates new annotation"
   :parameters {:body models/annotation_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 annotation created"
                           :id (utils/get-id (annotations/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message "unable to create annotation, likely bad collection id"
                             :error e}})))})

(def annotation-get-by-id
  {:summary "Retrieves specified annotation"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/annotation}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [annotation_result (annotations/READ id)]
                (if (= "" (:id annotation_result))
                  {:status 404
                   :body {:message "requested annotation not found"}}
                  {:status 200
                   :body annotation_result})))})

(def annotation-update ;; Non-functional
  {:summary "Updates the specified annotation"
   :parameters {:path {:id uuid?} :body ::sp/annotation}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (annotations/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested annotation not found"}}
                  {:status 200
                   :body {:message (str result " annotations updated")}})))})

(def annotation-delete ;; Non-functional
  {:summary "Deletes the specified annotation"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (annotations/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested annotation not found"}}
                  {:status 200
                   :body {:message (str result " annotations deleted")}})))})
