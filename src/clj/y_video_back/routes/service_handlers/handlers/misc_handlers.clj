(ns y-video-back.routes.service-handlers.handlers.misc-handlers
  (:require
   [clojure.spec.alpha :as s]))


(s/def :echo/first string?)
(s/def :echo/second string?)
(s/def ::echo (s/keys :req-un [:echo/first]
                      :opt-un [:echo/second]))

(def echo-patch
  {:summary "echo parameter post"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :body ::echo}
   :responses {200 {:body {:message string?}}}
   :handler {:status 200
             :body {:message "this route does nothing!"}}})
