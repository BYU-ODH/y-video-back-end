(ns y-video-back.routes.service-handlers.handlers.file-handlers-test
)
;;   (:require
;;    [clojure.data.json :as json]
;;    [clojure.java.io :as io]
;;    [clojure.java.shell :as shell]
;;    [clojure.test :refer [deftest is testing]]
;;    [legacy.db.test-util :as tcore]
;;    [legacy.utils.utils :as ut]
;;    [mount.core :as mount]
;;    [y-video-back.config :refer [env]]
;;    [y-video-back.db.core :as db :refer [*db*]]
;;    [y-video-back.db.files :as files]
;;    [y-video-back.db.languages :as languages]
;;    [y-video-back.db.resources :as resources]
;;    [y-video-back.handler :as handle]
;;    [legacy.utils.route-proxy.routes.rp-file :as test-file]
;;    [y-video-back.routes.service-handlers.handlers.file-handlers :as subj]
;;    [y-video-back.routes.service-handlers.utils.utils :as utils]))

;; (tcore/basic-transaction-fixtures
;;   (mount/start #'y-video-back.config/env)
;;   (mount/start #'y-video-back.handler/app)
;;   (mount/start #'y-video-back.db.core/*db*) 
;;   (ut/renew-db))

;; (deftest compute-aspect-ratio
;;   (let [width "1600"
;;         height "900"
;;         iw 1600
;;         ih 900]
;;     (testing "pretty-print the aspect ratio"
;;       (is (= "16:9" (and
;;                      (subj/compute-aspect-ratio iw ih :string)
;;                      (subj/compute-aspect-ratio width height :string)))
;;           "Works with both ints and strings"))
;;     (testing "return value vec of the ratio"
;;       (is (= [16 9] (and
;;                      (subj/compute-aspect-ratio iw ih)
;;                      (subj/compute-aspect-ratio width height)))
;;               "Works with both ints and strings"))))


;; (deftest probe-aspect-ratio
;;   (testing "check aspect ratio"
;;     (let [file-path (-> env :FILES :media-url (str "small_test_video.mp4"))
;;           aspect-ratio (subj/probe-aspect-ratio file-path)
;;           computed-ratio (subj/probe-aspect-ratio file-path :compute) 
;;           target "16:9"]
;;       (is (= target aspect-ratio) "Got the ffprobe aspect-ratio")
;;       (is (= target computed-ratio) "Computed the aspect ratio"))))

;; ;; TODO the test below is needed for issue https://github.com/BYU-ODH/y-video-back-end/issues/161
;; #_ (deftest _file-create_test
;;   []
;;   (testing "File Creation"
;;       (let [{{{:keys [file resource-id file-version metadata]} :multipart} :parameters} {:test-request "dummy"}
;;             file-name (utils/get-filename (:filename file))
;;             session-id "What to put here?"
;;             file-db "What to put here?"
;;             filecontent (java.file test-video)
;;             file-absent-response (test-file/file-post "-1" )]
;;         (testing "Resource doesn't exist"
;;           (is 500 (:status file-absent-response)))
;; (if-not (resources/EXISTS? resource-id)
;;           {:status 500
;;            :body {:message "resource not found"}}
;;           (do
;;             (if-not (languages/EXISTS? file-version)
;;               (languages/CREATE {:id file-version}))
;;             (let
;;                 [output (:out
;;                          (shell/sh "ffprobe" "-v" "error" "-select_streams" "v:0" "-show_entries" "stream=width,height,display_aspect_ratio" "-of" "json=c=1" (-> (:tempfile file) .getAbsolutePath)))
;;                  stream (get (get (json/read-str output) "streams") 0)
;;                  ;; use display_aspect_ratio if present, else use width:height
;;                  aspect-ratio (clojure.string/replace (get stream "display_aspect_ratio" (apply str [(get stream "width") ":" (get stream "height")])) ":" ",")
;;                  copy-result (io/copy (:tempfile file)
;;                                       (io/file (str (-> env :FILES :media-url) file-name)))
;;                  id (if (nil? copy-result)
;;                       (utils/get-id (files/CREATE {:filepath file-name
;;                                                    :file-version file-version
;;                                                    :metadata metadata
;;                                                    :resource-id resource-id
;;                                                    :aspect-ratio aspect-ratio}))
;;                       (print "Failed to create file in media directory"))]
;;               ;; ; :FILES :media-url + file-name = file path for ffmpeg
;;               ;; ; TODO: check first the cp command if it is successfull then add to the database
;;               ;; (try (io/copy (:tempfile file)
;;               ;;               (io/file (str (-> env :FILES :media-url) file-name)))
;;               ;;      (catch Exception e (str "caught exception: " (.getMessage e))))
            
;;               (if (:test env)
;;                 (print "Testing environment")
;;                 (io/delete-file (:tempfile file)))
;;               {:status 200
;;                :body {:message "1 file created"
;;                       :id id}})))))
;;     )
