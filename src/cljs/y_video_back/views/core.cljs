 (ns y-video-back.views.core
   (:require [re-frame.core :as re-frame]
             [y-video-back.shared :as shared]
             [y-video-back.events]	
             [y-video-back.subs]))

(defn main-panel []
  (let [view (re-frame/subscribe [:current-view])]
    [@view])) 

(defn render-view [view]
  (shared/err-boundary
   [:div.app-main
    view]))
                                        



