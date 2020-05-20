(ns y-video-back.db
  (:require [clojure.spec.alpha :as s]))

;;tables/current-page keys
(s/def ::template (s/nilable string?))
(s/def ::name (s/nilable keyword?))  ;;example :y-video-back.routes/devices
(s/def ::view (s/nilable fn?)) ;; example #'y-video-back.views.devices/devices-page
(s/def ::data  (s/keys :req-un [::name
                                ::view]))
(s/def ::result nil?)
(s/def ::path-params (s/nilable map?))
(s/def ::path (s/nilable string?))
(s/def ::query-params (s/nilable map?))
(s/def ::controllers (s/nilable vector?))

(s/def ::current-page
  (s/keys :req-un [::template
                   ::data
                   ::result
                   ::path-params
                   ::query-params
                   ::parameters
                   ::controllers]))

;;;;;;;;;;;;
;; APP DB ;;
;;;;;;;;;;;;
(s/def ::app-db
  (s/keys :req-un [::current-page]))

(def app-db
  {:current-page {:template nil
                  :data {:name :default
                         :view nil}
                  :result nil 
                  :path-params nil
                  :path nil
                  :query-params nil
                  :parameters {:path nil
                               :query nil}
                  :controllers nil}
   :api-result nil
   :api-error-result nil})
