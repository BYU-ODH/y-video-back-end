(ns y-video-back.routes.service-handlers.handlers.media-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.file-keys :as file-keys]
   [y-video-back.db.files :as files]
   [y-video-back.db.users :as users]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [ring.util.response :as rs :refer [file-response]]
   [ring.mock.request :as mr]
   [y-video-back.log :as log-ut]
   [taoensso.timbre :as log]))

                                        ; TODO - check if user has permission to stream requested file

(defn extension [s]
  (second (re-find #"\.([a-zA-Z0-9]+)$" s)))

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

(defn _stream-media
  [{{{:keys [file-key]} :path} :parameters}]
  (let [file-key-res (file-keys/READ-UNEXPIRED file-key)
                  _ (log/info "file-key-res is:" file-key-res)]
              (if (nil? file-key-res)
                {:status 404
                 :body {:message "file-key not found"}}
                (let [user-res (users/READ (:user-id file-key-res))]
                  (if (or (:dev env) (:prod env))
                    (log-ut/log-media-access {:file-id (str (:file-id file-key-res))
                                              :username (:username user-res)}))
                  (-> (file-response (utils/file-id-to-path (:file-id file-key-res)))
                      (rs/content-type "video/mp4") ;; not always mp4?
                      ;; TODO need to apply headers here, but based on the filename
                      )))))

(def stream-media ; TODO - require session-id?
  {:summary "Stream media referenced by file-key"
   :parameters {:path {:file-key uuid?}}
   :handler _stream-media})

;; (comment (mr/header "Content-Type"
;;                                   (case (extension (:filename file-key-res))
;;                                     :mp4 "video/mp4"
;;                                     :mp3 "audio/mp3")))

(defn get-file-key-m-v
  "Get the filekey either from a nested map or recognize if it is just given as a value"
  [m-v]
  (cond
    (map? m-v) (get-in m-v [:parameters :path :file-key])
    (uuid? m-v) m-v
    :else (throw (ex-info "Not recognizing the type for extracting the file-key" {:given-m-v m-v}))))

(defn _stream-partial-media
  "backend fn for streaming partial-media"
  [m-v]
  (let [file-key (get-file-key-m-v m-v)
        file-key-res (when file-key (file-keys/READ-UNEXPIRED file-key))]
    (if-not file-key-res
      {:status 404
       :body {:message "file-key not found"}}
      (let [user-res (users/READ (:user-id file-key-res))]
        (if (or (:dev env) (:prod env))
          (log-ut/log-media-access {:file-id (str (:file-id file-key-res))
                                    :username (:username user-res)}))
        (-> (file-response (utils/file-id-to-path (:file-id file-key-res)))
            (mr/header "Content-Type"
                       (case (extension (:filename m-v))
                         :mp4 "video/mp4"
                         :mp3 "audio/mp3")))))))

(def stream-partial-media ; TODO - require session-id?
  {:summary "Stream partial media referenced by file-key"
   :parameters {:path {:file-key uuid?}}
   :handler _stream-partial-media})
