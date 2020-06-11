(ns y-video-back.routes.service_handlers.misc_handlers
  (:require
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [clojure.spec.alpha :as s]
   [y-video-back.routes.service_handlers.utils :as utils]
   [y-video-back.db.core :as db]
   [y-video-back.routes.service_handlers.role_utils :as ru]))


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
              (if-not (ru/has-permission session-id "echo-patch" 0)
                ru/forbidden-page
                {:status 200 :body {:message "this route does nothing!"}}))})


(def connect-collection-and-course ;; Non-functional
  {:summary "Connects specified collection and course (bidirectional)"
   :parameters {:header {:session-id uuid?}}

   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              (if-not (ru/has-permission session-id "connect-collection-and-course" 0)
                ru/forbidden-page
                {:status 200
                 :body {:message "placeholder"}}))})

;; Searches across users, collections, contents, and courses
(def search-by-term ;; Non-functional
  {:summary "Searches users, collections, contents, and courses by search term"
   :parameters {:header {:session-id uuid?}
                :query {:query_term string?}}
   :responses {200 {:body {:users [models/user]
                           :collections [models/collection]
                           :contents [models/content]
                           :courses [models/course]}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [query_term]} :query} :parameters}]
              (if-not (ru/has-permission session-id "search-by-term" 0)
                ru/forbidden-page
                (let [user-result (map utils/remove-db-only
                                       (db/read-all-pattern :users
                                                            [:email :account-name :username]
                                                            (str "%" query_term "%")))
                      collection-result (map utils/remove-db-only
                                                (db/read-all-pattern :collections
                                                                     [:collection-name]
                                                                     (str "%" query_term "%")))
                      content-result (map utils/remove-db-only
                                          (db/read-all-pattern :contents
                                                               [:content-name :content-type :requester-email
                                                                :thumbnail]
                                                               (str "%" query_term "%")))
                      course-result (map utils/remove-db-only
                                         (db/read-all-pattern :courses
                                                              [:department :catalog-number :section-number]
                                                              (str "%" query_term "%")))]
                  {:status 200
                   :body {:users user-result
                          :collections collection-result
                          :contents content-result
                          :courses course-result}})))})
