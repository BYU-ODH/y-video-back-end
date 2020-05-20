(ns y-video-back.routes
  (:require [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfcontrol]
            [re-frame.core :as re-fc]
            [y-video-back.views.dashboard :as dash]))

(def routes
  (rf/router
   ["/"
    [""
     {:name ::front-dashboard
      :view #'dash/home-page}]]))


(defn init-routes!
  "Start the routing"
  []
  (rfe/start! routes
              (fn [m]
                (re-fc/dispatch [:set-current-page
                                 (assoc m :controllers (rfcontrol/apply-controllers (:controllers m) m))]))
              {:use-fragment false}))

