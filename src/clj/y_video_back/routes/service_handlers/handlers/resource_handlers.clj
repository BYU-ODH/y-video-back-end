(ns y-video-back.routes.service-handlers.handlers.resource-handlers
  (:require
   [y-video-back.db.resources :as resources]
   [y-video-back.db.resource-access :as resource-access]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]))

(def resource-create
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

(def resource-update
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

(def resource-delete
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

(def resource-get-all-collections
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

(def resource-get-all-contents
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
                  {:status 200
                   :body content-result})))})

(def resource-get-all-subtitles
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
                  {:status 200
                   :body (map #(-> %
                                   (dissoc :resource-id)
                                   (utils/remove-db-only))
                              res)})))})


(def resource-get-all-files
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
                  {:status 200
                   :body file-result})))})

(def resource-add-access
  {:summary "Gives user with username access to add this resource to contents"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :body {:username string?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               500 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body] {:keys [id]} :path} :parameters}]
              (if (resource-access/EXISTS-USERNAME-RESOURCE? (:username body) id)
                (resource-access/UPDATE-LAST-VERIFIED (:id (resource-access/READ-BY-USERNAME-RESOURCE (:username body) id)))
                (resource-access/CREATE {:username (:username body) :resource-id id}))
              {:status 200
               :body {:message "resource access added"}})})
              ; TODO add check for resource existence
              ; TODO add check for update or create success

(def resource-remove-access
  {:summary "Removes user with username access to add this resource to contents"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :body {:username string?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               500 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body] {:keys [id]} :path} :parameters}]
              (let [rsrc-acc (resource-access/READ-BY-USERNAME-RESOURCE (:username body) id)]
                (if (nil? rsrc-acc)
                  {:status 500
                   :body {:message "no user resource connection to delete"}}
                  (do
                    (resource-access/DELETE (:id rsrc-acc))
                    {:status 200
                     :body {:message "resource access added"}}))))})

(def resource-read-all-access
  {:summary "Returns usernames of all users with access to add this resource to contents"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [{:username string?
                            :valid boolean?}]}
               500 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (resources/EXISTS? id))
                {:status 404
                 :body {:message "resource not found"}}
                {:status 200
                 :body (map (fn [arg] {:username (:username arg)
                                       :valid (utils/is-valid-access-time (:last-verified arg))})
                            (resource-access/READ-USERNAMES-BY-RESOURCE id))}))})
