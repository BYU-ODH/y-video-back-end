(ns y-video-back.routes.service-handlers.file-handlers
  (:require
   [y-video-back.db.files :as files]
   [y-video-back.db.content-files-assoc :as content-files-assoc]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))

(def file-create
  {:summary "Creates a new file"
   :parameters {:header {:session-id uuid?}
                :body models/file-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "file-create" 0)
                ru/forbidden-page
                {:status 200
                 :body {:message "1 file created"
                        :id (utils/get-id (files/CREATE body))}}))})



(def file-get-by-id
  {:summary "Retrieves specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/file}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "file-get-by-id" 0)
                ru/forbidden-page
                (let [file-result (files/READ id)]
                  (if (= "" (:id file-result))
                    {:status 404
                     :body {:message "requested file not found"}}
                    {:status 200
                     :body file-result}))))})

(def file-update
  {:summary "Updates specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/file}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "file-update" 0)
                ru/forbidden-page
                (let [result (files/UPDATE id body)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested file not found"}}
                    {:status 200
                     :body {:message (str result " files updated")}}))))})

(def file-delete
  {:summary "Deletes specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "file-delete" 0)
                ru/forbidden-page
                (let [result (files/DELETE id)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested file not found"}}
                    {:status 200
                     :body {:message (str result " files deleted")}}))))})


(def file-get-all-contents ;; Non-functional
  {:summary "Retrieves all contents for specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/content {:file-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "file-get-all-contents" 0)
                ru/forbidden-page
                (let [file-contents-result (content-files-assoc/READ-CONTENTS-BY-FILE id)]
                  (let [content-result (map #(utils/remove-db-only %) file-contents-result)]
                    (if (= 0 (count content-result))
                      {:status 404
                       :body {:message "no files found for given content"}}
                      {:status 200
                       :body content-result})))))})
