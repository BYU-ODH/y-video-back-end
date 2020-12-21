(ns y-video-back.routes.service-handlers.handlers.email-handlers
  (:require
   [y-video-back.config :refer [env]]))
;
; (def search-by-user ;; Non-functional
;   {:summary "Searches users, collections, resources, and courses by search term"
;    :permission-level "instructor"
;    :parameters {:header {:session-id uuid?}
;                 :path {:term string?}}
;    :responses {200 {:body [models/user]}}
;    :handler (fn [{{{:keys [term]} :path} :parameters}]
;               (let [term (java.net.URLDecoder/decode term)
;                     res (map utils/remove-db-only
;                              (db/read-all-pattern :users-undeleted
;                                                   [:email :account-name :username]
;                                                   (str "%" term "%")))]
;                 {:status 200
;                  :body res}))})
