(ns y-video-back.routes.service-handlers.handlers.misc-handlers
  (:require
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [clojure.spec.alpha :as s]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.db.core :as db]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]))


(s/def :echo/first string?)
(s/def :echo/second string?)
(s/def ::echo (s/keys :req-un [:echo/first]
                      :opt-un [:echo/second]))

(def echo-patch
  {:summary "echo parameter post"
   :parameters {:header {:session-id uuid?}
                :body ::echo}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              {:status 200
               :body {:message "this route does nothing!"}})})


(def connect-collection-and-course ;; Non-functional
  {:summary "Connects specified collection and course (bidirectional)"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              {:status 200
               :body {:message "placeholder"}})})

;; Searches across users, collections, resources, and courses
(def search-by-term ;; Non-functional
  {:summary "Searches users, collections, resources, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :query {:query-term string?}}
   :responses {200 {:body {:users [models/user]
                           :collections [models/collection]
                           :resources [models/resource]
                           :courses [models/course]}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [query-term]} :query} :parameters}]
              (let [user-result (map utils/remove-db-only
                                     (db/read-all-pattern :users
                                                          [:email :account-name :username]
                                                          (str "%" query-term "%")))
                    collection-result (map utils/remove-db-only
                                              (db/read-all-pattern :collections
                                                                   [:collection-name]
                                                                   (str "%" query-term "%")))
                    resource-result (map utils/remove-db-only
                                        (db/read-all-pattern :resources
                                                             [:resource-name :resource-type :requester-email
                                                              :thumbnail]
                                                             (str "%" query-term "%")))
                    course-result (map utils/remove-db-only
                                       (db/read-all-pattern :courses
                                                            [:department :catalog-number :section-number]
                                                            (str "%" query-term "%")))]
                {:status 200
                 :body {:users user-result
                        :collections collection-result
                        :resources resource-result
                        :courses course-result}}))})
