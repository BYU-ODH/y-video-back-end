(ns y-video-back.routes.service-handlers.handlers.file-handlers-test
  (:require
   [y-video-back.routes.service-handlers.handlers.file-handlers :as subj] 
   [clojure.test :refer [use-fixtures deftest is testing]]
   [mount.core :as mount]
   [y-video-back.config :refer [env]]
   [y-video-back.db.core :refer [*db*] :as db]
   [y-video-back.db.files :as files]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.languages :as languages]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.handler :as handle]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [reitit.ring.middleware.multipart :as multipart]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [kawa.core :as ffm]
   [kawa.manager :as ffmanager]
   [legacy.db.test-util :as tcore]
   [legacy.utils.utils :as ut]
   [taoensso.timbre :as log])
  )

(tcore/basic-transaction-fixtures
 (mount/start #'y-video-back.config/env)
 (mount/start #'y-video-back.handler/app)
 (mount/start #'y-video-back.db.core/*db*) 
 (ut/renew-db))

(deftest ffprobe
  (testing "Change aspect ratio"
    (let [file-path (-> env :FILES :media-url (str "small_test_video.mp4"))
          test-name :ffprobe
          mp4 (ffmanager/register :mp4 (ffm/ffmpeg! :format "lavfi" :input-url "testsrc" :duration 10
                                                    :pixel-format "yuv420p" "testsrc.mp4"))
          ffp (ffmanager/register test-name
                                  (ffm/ffprobe! :input-url file-path :output-format "json"))]
      #_(shell/sh "ffprobe" "-v" "error" "-select_streams" "v:0" "-show_entries" "stream=width,height,display_aspect_ratio" "-of" "json=c=1" (-> (:tempfile file) .getAbsolutePath))
      (-> (ffmanager/ls) test-name :cmd)
      ;(is false)
      )
    ;; run let
    ))


#_(deftest _file-create_test
  "File Creation, including dimension clipping with ffprobe"
  []
  (let [{{{:keys [file resource-id file-version metadata]} :multipart} :parameters} {:test-request "dummy"}
        file-name (utils/get-filename (:filename file))]
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
