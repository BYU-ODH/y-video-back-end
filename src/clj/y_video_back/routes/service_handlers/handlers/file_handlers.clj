(ns y-video-back.routes.service-handlers.handlers.file-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.files :as files]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.languages :as languages]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [reitit.ring.middleware.multipart :as multipart]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [kawa.core :as ffmpeg]))

(defn _file-create
  "File Creation, including dimension clipping with ffprobe"
  [{{{:keys [file resource-id file-version metadata]} :multipart} :parameters}]
  (let [file-name (utils/get-filename (:filename file))]
                (if-not (resources/EXISTS? resource-id)
                  {:status 500
                   :body {:message "resource not found"}}
                  (do
                    (if-not (languages/EXISTS? file-version)
                      (languages/CREATE {:id file-version}))
                    (let
                     [output (:out
                              (shell/sh "ffprobe" "-v" "error" "-select_streams" "v:0" "-show_entries" "stream=width,height,display_aspect_ratio" "-of" "json=c=1" (-> (:tempfile file) .getAbsolutePath)))
                      stream (get (get (json/read-str output) "streams") 0)
                      ;; use display_aspect_ratio if present, else use width:height
                      aspect-ratio (clojure.string/replace (get stream "display_aspect_ratio" (apply str [(get stream "width") ":" (get stream "height")])) ":" ",")
                      copy-result (io/copy (:tempfile file)
                                           (io/file (str (-> env :FILES :media-url) file-name)))
                      id (if (nil? copy-result)
                           (utils/get-id (files/CREATE {:filepath file-name
                                                        :file-version file-version
                                                        :metadata metadata
                                                        :resource-id resource-id
                                                        :aspect-ratio aspect-ratio}))
                           (print "Failed to create file in media directory"))]
                      ;; ; :FILES :media-url + file-name = file path for ffmpeg
                      ;; ; TODO: check first the cp command if it is successfull then add to the database
                      ;; (try (io/copy (:tempfile file)
                      ;;               (io/file (str (-> env :FILES :media-url) file-name)))
                      ;;      (catch Exception e (str "caught exception: " (.getMessage e))))
        
                      (if (:test env)
                        (print "Testing environment")
                        (io/delete-file (:tempfile file)))
                      {:status 200
                       :body {:message "1 file created"
                              :id id}}))))
  )
(def file-create
  {:summary "Creates a new file. MUST INCLUDE FILE AS UPLOAD."
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :multipart {:file multipart/temp-file-part
                            :resource-id uuid?
                            :file-version string?
                            :metadata string?}}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler _file-create})

(def file-get-by-id
  {:summary "Retrieves specified file"
   :permission-level "instructor"
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/file}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (files/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body res})))})

; Should this be allowed to update the filepath?
(def file-update
  {:summary "Updates specified file"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/file}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if-not (files/EXISTS? id)
                {:status 404
                 :body {:message "file not found"}}
                (let [current-file (files/READ id)
                      proposed-file (merge current-file body)
                      same-name-file (first (files/READ-ALL-BY-FILEPATH [(:filepath proposed-file)]))]
                  ; If there is a name-owner collision and the collision is not with self (i.e. file being changed)
                  (if-not (resources/EXISTS? (:resource-id proposed-file))
                    {:status 500
                     :body {:message "resource not found"}}
                    (if-not (languages/EXISTS? (:file-version proposed-file))
                      {:status 500
                       :body {:message "file-version not found in languages table"}}
                      (if (and (not (nil? same-name-file))
                               (not (= (:id current-file)
                                       (:id same-name-file))))
                        {:status 500
                         :body {:message "unable to update file, filepath likely in use"}}
                        (let [result (files/UPDATE id body)]
                          (if (= 0 result)
                            {:status 500
                             :body {:message "unable to update file"}}
                            {:status 200
                             :body {:message (str result " files updated")}}))))))))})

(def file-delete
  {:summary "Deletes specified file"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [file-one (files/READ id)]
                (if (nil? file-one)
                  {:status 404 :body {:message "requested file not found"}}
                  (do
                    (if (.exists (io/file (str (-> env :FILES :media-url) (:filepath file-one))))
                      (do (io/copy (io/file (str (-> env :FILES :media-url) (:filepath file-one)))
                                   (io/file (str (-> env :FILES :media-trash-url) (:filepath file-one))))
                          (io/delete-file (str (-> env :FILES :media-url) (:filepath file-one)))))
                    (files/DELETE id)
                    {:status 200
                     :body {:message "1 file deleted"}}))))})
