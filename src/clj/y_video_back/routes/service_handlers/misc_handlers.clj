(ns y-video-back.routes.service_handlers.misc_handlers
  (:require
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [clojure.spec.alpha :as s]
   [y-video-back.routes.service_handlers.utils :as utils]))


(s/def :echo/first string?)
(s/def :echo/second string?)
(s/def ::echo (s/keys :req-un [:echo/first]
                      :opt-un [:echo/second]))

(def echo-patch
  {:summary "echo parameter post"
   :parameters {:body ::echo}
   :responses {200 {:body {:message string?}}}
   :handler (fn [ignore-me] {:status 200 :body {:message "this route does nothing!"}})})


(def connect-collection-and-course ;; Non-functional
  {:summary "Connects specified collection and course (bidirectional)"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def search-by-term ;; Non-functional
  {:summary "Searches users, collections, and content by search term"
   :parameters {:query {:query_term string?}}
   :responses {200 {:body {:users [models/user]
                           :collections [models/collection]
                           :courses [models/course]
                           :contents [models/content]}}}
   :handler (fn [{{{:keys [query_term]} :query} :parameters}]
              (let [result "placeholder"]
                {:status 200
                 :body result}))})
