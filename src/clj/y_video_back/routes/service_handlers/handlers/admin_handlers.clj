(ns y-video-back.routes.service-handlers.handlers.admin-handlers
  (:require
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.db.core :as db]
   [y-video-back.apis.persons :as persons]))

; TODO - sort results by more than just alphabetical

(def search-by-user
  {:summary "Searches users by search term"
   :permission-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/user]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :users-undeleted
                                                  [:email :account-name :username]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})

(def search-by-collection
  {:summary "Searches collections by search term"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [(into models/collection {:username string?})]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    coll-res (map utils/remove-db-only
                                  (db/read-all-pattern :collections-undeleted
                                                       [:collection-name]
                                                       (str "%" term "%")))
                    res (map #(into % {:username (if (nil? (:username (users/READ (:owner %))))
                                                   ""
                                                   (:username (users/READ (:owner %))))})
                             coll-res)]
                {:status 200
                 :body res}))})

(def search-public-collections
  {:summary "Searches public collections by search term"
   :permission-level "student"
   :bypass-permission true
   :parameters {:path {:term string?}}
   :responses {200 {:body [(into models/collection {:username string?})]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    coll-res (map utils/remove-db-only
                                  (db/read-all-pattern :public-collections-undeleted
                                                       [:collection-name]
                                                       (str "%" term "%")))
                    res (map #(into % {
                                       :username (let [username (:username (users/READ (:owner %)))] 
                                                   (if (nil? username)
                                                           "invalid"
                                                           username)) 
                                       :content (map utils/remove-db-only
                                                     (db/read-all-where :contents-undeleted
                                                                        :collection-id (:id %)))
                                       })
                             coll-res)]
                {:status 200
                 :body res}))})

(def search-by-content
  {:summary "Searches contents by search term"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/content]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :contents-undeleted
                                                  [:title :content-type :url :description :tags :file-version]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})


(def search-by-resource
  {:summary "Searches resources by search term"
   :permission-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/resource]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :resources-undeleted
                                                  [:resource-name :resource-type :requester-email]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})
