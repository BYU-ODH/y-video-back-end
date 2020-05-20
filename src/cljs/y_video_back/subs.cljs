(ns y-video-back.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :current-page
 (fn [db _]
   (:current-page db)))

(reg-sub
 :current-view
 (fn [db _]
   (get-in db [:current-page :data :view]
           (constantly [:h1 "Not Found"]))))

(reg-sub
 :get-db
 (fn [db path]
   (get-in db path)))
