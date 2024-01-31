(ns y-video-back.routes.service-handlers.handlers.file-handlers-test
  (:require
   [y-video-back.routes.service-handlers.handlers.file-handlers :as subj] 
   [clojure.test :refer [use-fixtures deftest is testing]]
   [mount.core :as mount]
   [y-video-back.config :refer [env]]
   [y-video-back.db.core :refer [*db*] :as db]
   [y-video-back.handler :as handle]
   [legacy.db.test-util :as tcore]
   [legacy.utils.utils :as ut]))

(tcore/basic-transaction-fixtures
  (mount/start #'y-video-back.config/env)
  (mount/start #'y-video-back.handler/app)
  (mount/start #'y-video-back.db.core/*db*) 
  (ut/renew-db))

(deftest compute-aspect-ratio
  (let [width "1600"
        height "900"
        iw 1600
        ih 900]
    (testing "pretty-print the aspect ratio"
      (is (= "16:9" (and
                     (subj/compute-aspect-ratio iw ih :string)
                     (subj/compute-aspect-ratio width height :string)))
          "Works with both ints and strings"))
    (testing "return value vec of the ratio"
      (is (= [16 9] (and
                     (subj/compute-aspect-ratio iw ih)
                     (subj/compute-aspect-ratio width height)))
              "Works with both ints and strings"))))


(deftest probe-aspect-ratio
  (testing "check aspect ratio"
    (let [file-path (-> env :FILES :media-url (str "small_test_video.mp4"))
          aspect-ratio (subj/probe-aspect-ratio file-path)
          computed-ratio (subj/probe-aspect-ratio file-path :compute) 
          target "16:9"]
      (is (= target aspect-ratio) "Got the ffprobe aspect-ratio")
      (is (= target computed-ratio) "Computed the aspect ratio"))))


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
