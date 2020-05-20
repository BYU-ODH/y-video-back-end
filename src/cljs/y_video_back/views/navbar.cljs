(ns y-video-back.views.navbar
  (:require [re-frame.core :as rfc]
            [reitit.frontend.easy :as rfe]
            [y-video-back.shared :as shared]))

(defn current-page?
  "Is the given route-id the same as the current-page?"
  [route-id]
  (let [current-page-id (-> @(rfc/subscribe [:current-page])
                            (get-in [:data :name]))]
    (some #{current-page-id} [route-id
                              (shared/routify-keyword route-id)])))

(defn nav-item
  "A bulma navbar item"
  [text &[opts]]
  (let [valid-anchor-args [:href :class :on-click]
        href (or (:href opts) (-> (:route-id opts) shared/routify-keyword rfe/href))
        current-page-class (let [base-classes (:class opts)
                                 rid (:route-id opts)]
                             (cond-> base-classes
                               (current-page? rid) (conj "active")))]
    [:a.navbar-item (assoc
                     (select-keys opts valid-anchor-args)
                     :class current-page-class
                     :href href)
     text]))

(defn burger-menu
  [data-target-id]
  [:a.navbar-burger.burger {:role "button"
                            :aria-label "menu"
                            :aria-expanded false
                            :data-target data-target-id}
   [:span {:aria-hidden "true"}]
   [:span {:aria-hidden "true"}]
   [:span {:aria-hidden "true"}]])


(defn navbar
  "A simple navbar for the app"
  []
  (let [content-id "y-video-back-contents"]
    [:div.navbar
     [:div.navbar-brand
      [nav-item "y-video-back"]
      [burger-menu content-id]]
     [:div.navbar-menu {:id content-id}
      [:div.navbar-start
       [nav-item "Dashboard" {:route-id :front-dashboard}]]]]))
