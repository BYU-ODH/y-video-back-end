(ns y-video-back.routes.service-handlers.handlers.resource-handlers
  (:require
   [y-video-back.db.resources :as resources]
   [y-video-back.db.subtitles :as subtitles]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.utils.account-permissions :as ac]))


(def resource-create ;; Non-functional
  {:summary "Creates new resource"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :body models/resource-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              {:status 200
               :body {:message "1 resource created"
                      :id (utils/get-id (resources/CREATE body))}})})

(def resource-get-by-id
  {:summary "Retrieves specified resource"
   :permission-level "instructor"
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/resource}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (resources/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested resource not found"}}
                  {:status 200
                   :body res})))})

(def resource-update ;; Non-functional
  {:summary "Updates the specified resource"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/resource}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if-not (resources/EXISTS? id)
                {:status 404
                 :body {:message "resource not found"}}
                (let [result (resources/UPDATE id body)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested resource not found"}}
                    {:status 200
                     :body {:message (str result " resources updated")}}))))})

(def resource-delete ;; Non-functional
  {:summary "Deletes the specified resource"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (resources/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested resource not found"}}
                  {:status 200
                   :body {:message (str 1 " resources deleted")}})))})

(def resource-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified resource"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/collection]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (resources/EXISTS? id))
                {:status 404
                 :body {:message "resource not found"}}
                (let [res (resources/COLLECTIONS-BY-RESOURCE id)]
                  {:status 200
                   :body (map #(-> %
                                   (utils/remove-db-only)
                                   (dissoc :resource-id))
                              res)})))})

(def resource-get-all-contents ;; Non-functional
  {:summary "Retrieves all contents for specified resource"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/content {:resource-id uuid?})]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (resources/EXISTS? id))
                {:status 404
                 :body {:message "resource not found"}}
                (let [content-resources-result (resources/CONTENTS-BY-RESOURCE id)
                      content-result (map #(utils/remove-db-only %) content-resources-result)]
                  {:status 200 ; Not implemented yet
                   :body content-result})))})

(def resource-get-all-subtitles ;; Non-functional
  {:summary "Retrieves all subtitles connected to resource"
   :permission-level "instructor"
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/subtitle]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (resources/EXISTS? id))
                {:status 404
                 :body {:message "resource not found"}}
                (let [res (resources/READ-SBTL-BY-RSRC id)]
                  {:status 200 ; Not implemented yet
                   :body (map #(-> %
                                   (dissoc :resource-id)
                                   (utils/remove-db-only))
                              res)})))})


(def resource-get-all-files ;; Non-functional
  {:summary "Retrieves all the files for the specified resource"
   :permission-level "instructor"
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/file {:resource-id uuid?})]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (resources/EXISTS? id))
                {:status 404
                 :body {:message "resource not found"}}
                (let [file-resources-result (resources/FILES-BY-RESOURCE id)
                      file-result (map #(utils/remove-db-only %) file-resources-result)]
                  {:status 200 ; Not implemented yet
                   :body file-result})))})
