(ns y-video-back.routes.service-handlers.handlers.media-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.file-keys :as file-keys]
   [y-video-back.db.files :as files]
   [y-video-back.db.users :as users]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [ring.util.response :refer [file-response]]
   [y-video-back.log :as log-ut]))

; TODO - check if user has permission to stream requested file

(def get-file-key
  {:summary "Gets volatile url for streaming specified media file. If accessing public file as public user, use '00000000-0000-0000-0000-000000000000' as session-id."
   :permission-level "lab-assistant"
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:file-id uuid?}}
   :responses {200 {:body {:file-key uuid?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [file-id]} :path} :parameters}]
              (let [user-id (ru/token-to-user-id session-id)
                    file-key (file-keys/CREATE {:file-id file-id
                                                :user-id user-id})
                    file-res (files/READ file-id)]
                {:status 200
                 :body {:file-key (:id file-key)}}))})

(def stream-media ; TODO - require session-id?
  {:summary "Stream media referenced by file-key"
   :parameters {:path {:file-key uuid?}}
   :handler (fn [{{{:keys [file-key]} :path} :parameters}]
              (let [file-key-res (file-keys/READ-UNEXPIRED file-key)]
                (if (nil? file-key-res)
                  {:status 404
                   :body {:message "file-key not found"}}
                  (let [user-res (users/READ (:user-id file-key-res))]
                    (if (or (:dev env) (:prod env))
                      (log-ut/log-media-access {:file-id (str (:file-id file-key-res))
                                                :username (:username user-res)}))
                    (file-response (utils/file-id-to-path (:file-id file-key-res)))))))})

(def stream-partial-media ; TODO - require session-id?
  {:summary "Stream partial media referenced by file-key"
   :parameters {:path {:file-key uuid?}}
   :handler (fn [{{{:keys [file-key]} :path} :parameters}]
              (let [file-key-res (file-keys/READ-UNEXPIRED file-key)]
                (if (nil? file-key-res)
                  {:status 404
                   :body {:message "file-key not found"}}
                  (let [user-res (users/READ (:user-id file-key-res))]
                    (if (or (:dev env) (:prod env))
                      (log-ut/log-media-access {:file-id (str (:file-id file-key-res))
                                                :username (:username user-res)}))
                    (file-response (utils/file-id-to-path (:file-id file-key-res)))))))})
