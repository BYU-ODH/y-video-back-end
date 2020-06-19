(ns y-video-back.routes.service-handlers.content-handlers
  (:require
   [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
   [y-video-back.db.content-files-assoc :as content-files-assoc]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.files :as files]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def content-create ;; Non-functional
  {:summary "Creates new content"
   :parameters {:header {:session-id uuid?}
                :body models/content-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-create" 0)
                ru/forbidden-page
                {:status 200
                 :body {:message "1 content created"
                        :id (utils/get-id (contents/CREATE body))}}))})

(def content-get-by-id
  {:summary "Retrieves specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/content}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-get-by-id" {:content-id id})
                ru/forbidden-page
                (let [res (contents/READ id)]
                  (if (nil? res)
                    {:status 404
                     :body {:message "requested content not found"}}
                    {:status 200
                     :body res}))))})

(def content-update ;; Non-functional
  {:summary "Updates the specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/content}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-update" {:content-id id})
                ru/forbidden-page
                (if-not (contents/EXISTS? id)
                  {:status 404
                   :body {:message "content not found"}}
                  ;(let [current-content (contents/READ id)
                  ;      proposed-content (merge current-content body)
                  ;      same-name-content (first (contents/READ-ALL-BY-NAME [(:content-name proposed-content)]))
                    ; If there is a name collision and the collision is not with self
                  ;  (if (and (not (nil? same-name-content))
                  ;           (not (= (:id current-content)
                  ;                   (:id same-name-content)}
                  ;    {:status 500
                  ;     :body {:message "unable to update content, name-owner pair likely in use"}
                  (let [result (contents/UPDATE id body)]
                    (if (= 0 result)
                      {:status 404
                       :body {:message "requested content not found"}}
                      {:status 200
                       :body {:message (str result " contents updated")}})))))})

(def content-delete ;; Non-functional
  {:summary "Deletes the specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-delete" {:content-id id})
                ru/forbidden-page
                (let [result (contents/DELETE id)]
                  (if (nil? result)
                    {:status 404
                     :body {:message "requested content not found"}}
                    {:status 200
                     :body {:message (str 1 " contents deleted")}}))))})

(def content-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:content-id uuid?})]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-get-all-collections" {:content-id id})
                ru/forbidden-page
                (if (not (contents/EXISTS? id))
                  {:status 404
                   :body {:message "content not found"}}
                  (let [content-collections-result (collection-contents-assoc/READ-COLLECTIONS-BY-CONTENT id)]
                    (let [collection-result (map #(utils/remove-db-only %) content-collections-result)]
                      {:status 200
                       :body collection-result})))))})

(def content-get-all-files ;; Non-functional
  {:summary "Retrieves all the files for the specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/file {:content-id uuid?})]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-get-all-files" {:content-id id})
                ru/forbidden-page
                (if (not (contents/EXISTS? id))
                  {:status 404
                   :body {:message "content not found"}}
                  (let [file-contents-result (content-files-assoc/READ-FILES-BY-CONTENT id)]
                    (let [file-result (map #(utils/remove-db-only %) file-contents-result)]
                      {:status 200
                       :body file-result})))))})

(def content-add-view ;; Non-functional
  {:summary "Adds 1 view to specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "content-add-view" {:content-id id})
                ru/forbidden-page
                ; We need the current view count, so we might as well pull the whole
                ; db object to check for existence as well
                (let [content-res (contents/READ id)]
                  (if-not content-res
                    {:status 404
                     :body {:message "requested content not found"}}
                    (do
                      (contents/UPDATE id {:views (+ 1 (:views content-res))})
                      {:status 200
                       :body {:message "view successfully added"}})))))})

(def content-add-file
  {:summary "Adds file to specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:file-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-add-file" {:content-id id})
                ru/forbidden-page
                (if (not (contents/EXISTS? id))
                  {:status 404
                   :body {:message "content not found"}}
                  (if (not (files/EXISTS? (:file-id body)))
                    {:status 500
                     :body {:message "file not found"}}
                    (if (content-files-assoc/EXISTS-CONT-FILE? id (:file-id body))
                      {:status 500
                       :body {:message "file already connected to content"}}
                      (let [result (utils/get-id (content-files-assoc/CREATE (into body {:content-id id})))]
                        (if (= nil result)
                          {:status 500
                           :body {:message "unable to add file"}}
                          {:status 200
                           :body {:message (str 1 " files added to content")
                                  :id result}})))))))})

(def content-remove-file
  {:summary "Removes file from specified content"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:file-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "content-remove-file" {:content-id id})
                ru/forbidden-page
                (if (not (contents/EXISTS? id))
                  {:status 404
                   :body {:message "content not found"}}
                  (if (not (files/EXISTS? (:file-id body)))
                    {:status 500
                     :body {:message "file not found"}}
                    (if-not (content-files-assoc/EXISTS-CONT-FILE? id (:file-id body))
                      {:status 500
                       :body {:message "file not connected to content"}}
                      (let [result (content-files-assoc/DELETE-BY-IDS [id (:file-id body)])]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to remove file"}}
                          {:status 200
                           :body {:message (str result " files removed from content")}})))))))})
