(ns y-video-back.routes.service-handlers.handlers.subtitle-handlers
  (:require
   [y-video-back.db.subtitles :as subtitles]
   [y-video-back.db.contents :as contents]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]))

(def subtitle-create
  {:summary "Creates a new subtitle"
   :permission-level "lab-assistant"
   :role-level "ta"
   :path-to-id [:parameters :body :resource-id]
   :parameters {:header {:session-id uuid?}
                :body models/subtitle-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (if-not (contents/EXISTS? (:content-id body))
                {:status 500
                 :body {:message "content not found"}}
                {:status 200
                 :body {:message "1 subtitle created"
                        :id (utils/get-id (subtitles/CREATE body))}}))})



(def subtitle-get-by-id
  {:summary "Retrieves specified subtitle"
   :permission-level "lab-assistant"
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/subtitle}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (subtitles/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested subtitle not found"}}
                  {:status 200
                   :body res})))})

(def subtitle-update
  {:summary "Updates specified subtitle"
   :permission-level "lab-assistant"
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/subtitle}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if-not (subtitles/EXISTS? id)
                {:status 404
                 :body {:message "subtitle not found"}}
                (let [current-subtitle (subtitles/READ id)
                      proposed-subtitle (merge current-subtitle body)]
                  (if-not (contents/EXISTS? (:content-id proposed-subtitle))
                    {:status 500
                     :body {:message "content not found"}}
                    (let [result (subtitles/UPDATE id body)]
                      (if (= 0 result)
                        {:status 500
                         :body {:message "unable to update subtitle"}}
                        {:status 200
                         :body {:message (str result " subtitles updated")}}))))))})

(def subtitle-delete
  {:summary "Deletes specified subtitle"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (subtitles/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested subtitle not found"}}
                  {:status 200
                   :body {:message (str result " subtitles deleted")}})))})

