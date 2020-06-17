(ns y-video-back.routes.service-handlers.admin-handlers
  (:require
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]
   [clojure.spec.alpha :as s]
   [y-video-back.db.core :as db]))

(def search-by-user ;; Non-functional
  {:summary "Searches users, collections, contents, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/user]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (if-not (ru/has-permission session-id "search-by-term" 0)
                ru/forbidden-page
                (let [user-result (map utils/remove-db-only
                                       (db/read-all-pattern :users
                                                            [:email :account-name :username]
                                                            (str "%" term "%")))]
                  {:status 200
                   :body user-result})))})

(def search-by-collection ;; Non-functional
  {:summary "Searches users, collections, contents, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/collection]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (if-not (ru/has-permission session-id "search-by-term" 0)
                ru/forbidden-page
                (let [collection-result (map utils/remove-db-only
                                             (db/read-all-pattern :collections
                                                                  [:collection-name]
                                                                  (str "%" term "%")))]
                  (println collection-result)
                  {:status 200
                   :body collection-result})))})

(def search-by-content
  {:summary "Searches users, collections, contents, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/content]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (if-not (ru/has-permission session-id "search-by-term" 0)
                ru/forbidden-page
                (let [content-result (map utils/remove-db-only
                                          (db/read-all-pattern :contents
                                                               [:content-name :content-type :requester-email
                                                                :thumbnail]
                                                               (str "%" term "%")))]
                  {:status 200
                   :body content-result})))})
