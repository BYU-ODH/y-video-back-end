(ns y-video-back.routes.service-handlers.handlers.media-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.files :as files]
   [y-video-back.db.file-keys :as file-keys]
   [y-video-back.models :as models]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [clojure.spec.alpha :as s]
   ;[y-video-back.db.core :as db]
   [ring.swagger.upload :as swagger-upload]))

(def upload-file
  {:summary "Uploads file"
   :permission-level 0
   :responses {200 {:body {:message string?}}}
   :handler (fn [p]
              (let [file-params (get-in p [:params "file"])
                    body (:body p)]
                (println "DEBUG: file-params=" file-params)
                (println "DEBUG: body=" body)
                (clojure.java.io/copy (:tempfile file-params)
                                      (clojure.java.io/file (str (-> env :FILES :media-url) (:filename file-params)))))
              {:status 200
               :schema swagger-upload/TempFileUpload})})


;; TODO - check if user has permission to stream requested file

(def get-file-key ;; Non-functional
  {:summary "Gets volatile url for streaming specified media file. If accessing public file as public user, use '00000000-0000-0000-0000-000000000000' as session-id."
   :permission-level 1
   :parameters {:header {:session-id uuid?}
                :path {:file-id uuid?}}
   :responses {200 {:body {:file-key uuid?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [file-id]} :path} :parameters}]
              (let [user-id (ru/token-to-user-id session-id)
                    file-key (file-keys/CREATE {:file-id file-id
                                                :user-id user-id})]
                {:status 200
                 :body {:file-key (:id file-key)}}))})

(def stream-media ;; TODO - require session-id?
  {:summary "Stream media referenced by file-key"
   :parameters {
                ;:header {:session-id uuid?}
                :path {:file-key uuid?}}
   ;:responses {200 {:body "file response body"}
   ;            404 (:body {:message string?})
   ;:handler (fn [{{{:keys [session-id]} :header {:keys [file-id]} :path} :parameters}])
   :handler (fn [{{{:keys [file-key]} :path} :parameters}]
              (let [file-key-res (file-keys/READ-UNEXPIRED file-key)]
                (if (nil? file-key-res)
                  {:status 404
                   :body {:message "file-key not found"}}
                  (ring.util.response/file-response (utils/file-id-to-path (:file-id file-key-res))))))})
