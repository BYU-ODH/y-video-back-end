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
  {:summary "Searches users, collections, resources, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/user]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :users-undeleted
                                                  [:email :account-name :username]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})

(def search-by-collection ;; Non-functional
  {:summary "Searches users, collections, resources, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [(into models/collection {:username string?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    coll-res (map utils/remove-db-only
                                  (db/read-all-pattern :collections-undeleted
                                                       [:collection-name]
                                                       (str "%" term "%")))
                    res (map #(into % {:username (:username (users/READ (:owner %)))})
                             coll-res)]
                {:status 200
                 :body res}))})

(def search-by-content
  {:summary "Searches users, collections, contents, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/content]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :contents-undeleted
                                                  [:title :content-type :url :description :tags :file-version]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})


(def search-by-resource
  {:summary "Searches users, collections, resources, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/resource]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :resources-undeleted
                                                  [:resource-name :resource-type :requester-email]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})
