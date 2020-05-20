(ns y-video-back.core
  (:require [reagent.core :as r]
            [y-video-back.ajax :refer [load-interceptors!]]
            [y-video-back.routes :as routes]
            [re-frame.core :as rfc]
            [y-video-back.views.core :as views]
            [y-video-back.views.navbar :refer [navbar]])
  (:import goog.History))

;; -------------------------
;; Initialize app

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "nav"))
  (r/render [#'views/main-panel] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (routes/init-routes!)
  (rfc/dispatch-sync [:initialize-db])
  (mount-components))
