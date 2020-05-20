(ns y-video-back.views.shared-components
  "Shared components for multiple views, with the exception of the navbar which has its own namespace. "
  (:require [re-frame.core :as rfc]
            [tick.core :as tick]
            [tick.alpha.api :as t]
            [tick.locale-en-us] ;; needed for formatting side-effects
            [y-video-back.shared :as shared]))

(defn date-line
  "A right-aligned current date, newspaper style"
  []
  (let [formatter (t/formatter "MMMM dd, yyyy")
        time (t/format formatter (-> (tick/now) tick/date))]
    [:h2.date-line time])) 

(defn basic-template [{:keys [page-id page-class contents page-title]}]
    [shared/err-boundary
     [:div {:id page-id :class page-class}
      [:div.hero
       [:div.hero-title
        [:div.container 
         [:h1.title (or page-title "y-video-back")]]]]
      contents]])
