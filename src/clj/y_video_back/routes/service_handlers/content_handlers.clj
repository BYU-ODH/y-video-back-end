(ns y-video-back.routes.service_handlers.content_handlers
  (:require
   [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
   [y-video-back.db.content-files-assoc :as content_files_assoc]
   [y-video-back.db.contents :as contents]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))


(def content-create ;; Non-functional
  {:summary "Creates new content"
   :parameters {:body models/content_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 content created"
                           :id (utils/get-id (contents/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message "unable to create content, likely bad collection id"
                             :error e}})))})

(def content-get-by-id
  {:summary "Retrieves specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/content}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [content_result (contents/READ id)]
                (if (= "" (:id content_result))
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body content_result})))})

(def content-update ;; Non-functional
  {:summary "Updates the specified content"
   :parameters {:path {:id uuid?} :body ::sp/content}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (contents/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body {:message (str result " contents updated")}})))})

(def content-delete ;; Non-functional
  {:summary "Deletes the specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (contents/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body {:message (str result " contents deleted")}})))})

(def content-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:content-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [content_collections_result (collection_contents_assoc/READ-COLLECTIONS-BY-CONTENT id)]
                (let [collection_result (map #(utils/remove-db-only %) content_collections_result)]
                  (if (= 0 (count collection_result))
                    {:status 404
                     :body {:message "no contents found for given collection"}}
                    {:status 200
                     :body collection_result}))))})

(def content-get-all-files ;; Non-functional
  {:summary "Retrieves all the files for the specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/file {:content-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [file_contents_result (content_files_assoc/READ-CONTENTS-BY-COLLECTION id)]
                (let [file_result (map #(utils/remove-db-only %) file_contents_result)]
                  (if (= 0 (count file_result))
                    {:status 404
                     :body {:message "no files found for given content"}}
                    {:status 200
                     :body file_result}))))})

(def content-add-view ;; Non-functional
  {:summary "Adds 1 view to specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result "placeholder"]
                (if result
                  {:status 200
                   :body {:message "view successfully added"}}
                  {:status 404
                   :body {:message "requested content not found"}})))})

(def content-add-file
  {:summary "Adds file to specified content"
   :parameters {:path {:id uuid?} :body {:file-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (utils/get-id (content_files_assoc/CREATE (into body {:content-id id})))]
                (if (= nil result)
                  {:status 404
                   :body {:message "unable to add file"}}
                  {:status 200
                   :body {:message (str 1 " files added to content")
                          :id result}})))})

(def content-remove-file
  {:summary "Removes file from specified content"
   :parameters {:path {:id uuid?} :body {:file-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (content_files_assoc/DELETE-BY-IDS [id (:file-id body)])]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove file"}}
                  {:status 200
                   :body {:message (str result " files removed from content")}})))})
