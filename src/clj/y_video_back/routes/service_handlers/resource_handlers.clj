(ns y-video-back.routes.service-handlers.resource-handlers
  (:require
   [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
   [y-video-back.db.resource-files-assoc :as resource-files-assoc]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.files :as files]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def resource-create ;; Non-functional
  {:summary "Creates new resource"
   :parameters {:header {:session-id uuid?}
                :body models/resource-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "resource-create" 0)
                ru/forbidden-page
                {:status 200
                 :body {:message "1 resource created"
                        :id (utils/get-id (resources/CREATE body))}}))})

(def resource-get-by-id
  {:summary "Retrieves specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/resource}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "resource-get-by-id" {:resource-id id})
                ru/forbidden-page
                (let [res (resources/READ id)]
                  (if (nil? res)
                    {:status 404
                     :body {:message "requested resource not found"}}
                    {:status 200
                     :body res}))))})

(def resource-update ;; Non-functional
  {:summary "Updates the specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/resource}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "resource-update" {:resource-id id})
                ru/forbidden-page
                (if-not (resources/EXISTS? id)
                  {:status 404
                   :body {:message "resource not found"}}
                  ;(let [current-resource (resources/READ id)
                  ;      proposed-resource (merge current-resource body)
                  ;      same-name-resource (first (resources/READ-ALL-BY-NAME [(:resource-name proposed-resource)]))
                    ; If there is a name collision and the collision is not with self
                  ;  (if (and (not (nil? same-name-resource))
                  ;           (not (= (:id current-resource)
                  ;                   (:id same-name-resource)}
                  ;    {:status 500
                  ;     :body {:message "unable to update resource, name-owner pair likely in use"}
                  (let [result (resources/UPDATE id body)]
                    (if (= 0 result)
                      {:status 404
                       :body {:message "requested resource not found"}}
                      {:status 200
                       :body {:message (str result " resources updated")}})))))})

(def resource-delete ;; Non-functional
  {:summary "Deletes the specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "resource-delete" {:resource-id id})
                ru/forbidden-page
                (let [result (resources/DELETE id)]
                  (if (nil? result)
                    {:status 404
                     :body {:message "requested resource not found"}}
                    {:status 200
                     :body {:message (str 1 " resources deleted")}}))))})

(def resource-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:resource-id uuid?})]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "resource-get-all-collections" {:resource-id id})
                ru/forbidden-page
                (if (not (resources/EXISTS? id))
                  {:status 404
                   :body {:message "resource not found"}}
                  (let [resource-collections-result (collection-contents-assoc/READ-COLLECTIONS-BY-CONTENT id)]
                    (let [collection-result (map #(utils/remove-db-only %) resource-collections-result)]
                      {:status 200
                       :body collection-result})))))})

(def resource-get-all-files ;; Non-functional
  {:summary "Retrieves all the files for the specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/file {:resource-id uuid?})]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "resource-get-all-files" {:resource-id id})
                ru/forbidden-page
                (if (not (resources/EXISTS? id))
                  {:status 404
                   :body {:message "resource not found"}}
                  (let [file-resources-result (resource-files-assoc/READ-FILES-BY-CONTENT id)]
                    (let [file-result (map #(utils/remove-db-only %) file-resources-result)]
                      {:status 200
                       :body file-result})))))})

(def resource-add-view ;; Non-functional
  {:summary "Adds 1 view to specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "resource-add-view" {:resource-id id})
                ru/forbidden-page
                ; We need the current view count, so we might as well pull the whole
                ; db object to check for existence as well
                (let [resource-res (resources/READ id)]
                  (if-not resource-res
                    {:status 404
                     :body {:message "requested resource not found"}}
                    (do
                      (resources/UPDATE id {:views (+ 1 (:views resource-res))})
                      {:status 200
                       :body {:message "view successfully added"}})))))})

(def resource-add-file
  {:summary "Adds file to specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:file-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "resource-add-file" {:resource-id id})
                ru/forbidden-page
                (if (not (resources/EXISTS? id))
                  {:status 404
                   :body {:message "resource not found"}}
                  (if (not (files/EXISTS? (:file-id body)))
                    {:status 500
                     :body {:message "file not found"}}
                    (if (resource-files-assoc/EXISTS-CONT-FILE? id (:file-id body))
                      {:status 500
                       :body {:message "file already connected to resource"}}
                      (let [result (utils/get-id (resource-files-assoc/CREATE (into body {:resource-id id})))]
                        (if (= nil result)
                          {:status 500
                           :body {:message "unable to add file"}}
                          {:status 200
                           :body {:message (str 1 " files added to resource")
                                  :id result}})))))))})

(def resource-remove-file
  {:summary "Removes file from specified resource"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:file-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "resource-remove-file" {:resource-id id})
                ru/forbidden-page
                (if (not (resources/EXISTS? id))
                  {:status 404
                   :body {:message "resource not found"}}
                  (if (not (files/EXISTS? (:file-id body)))
                    {:status 500
                     :body {:message "file not found"}}
                    (if-not (resource-files-assoc/EXISTS-CONT-FILE? id (:file-id body))
                      {:status 500
                       :body {:message "file not connected to resource"}}
                      (let [result (resource-files-assoc/DELETE-BY-IDS [id (:file-id body)])]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to remove file"}}
                          {:status 200
                           :body {:message (str result " files removed from resource")}})))))))})
