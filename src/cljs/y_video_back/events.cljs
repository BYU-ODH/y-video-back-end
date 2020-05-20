(ns y-video-back.events
  (:require 
   [day8.re-frame.http-fx]
   [re-frame.core :refer [reg-event-db after reg-event-fx dispatch] :as rfc]
   [y-video-back.views.dashboard :as dash]
   [y-video-back.db :as db :refer [app-db]]
   [clojure.spec.alpha :as s]))


;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw
       (ex-info
        (str "Spec check after " event " failed: " explain-data)
        explain-data)))))

(defn validate-spec
  [spec]
  (if goog.DEBUG
    (after (partial check-and-throw spec))
    []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INITIALIZE THE RE-FRAME DB ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn ^{:private true} init-view
  "Initialize the default view `home-page`"
  [db & rest]
  (let [home-page-view #'dash/home-page]
    (-> db
        (assoc-in [:current-page :data]
                  {:name :init
                   :view home-page-view}))))

(rfc/reg-event-db
 :initialize-db
 (fn [_ _]
   (init-view app-db)))

(rfc/reg-event-db
 :set-current-page
 (validate-spec ::db/app-db)
 (fn [db [_ m]]
   (assoc db :current-page m)))
