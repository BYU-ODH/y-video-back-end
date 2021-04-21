(ns y-video-back.routes.service-handlers.handlers.language-handlers
  (:require
   [y-video-back.db.languages :as languages]
   [y-video-back.models :as models]
   [y-video-back.routes.service-handlers.utils.utils :as utils]))

(def language-create
  {:summary "Creates a new language"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :body models/language}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              {:status 200
               :body {:message "1 language created"
                      :id (utils/get-id (languages/CREATE body))}})})

(def language-delete
  {:summary "Deletes specified language"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id string?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (languages/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested language not found"}}
                  {:status 200
                   :body {:message (str result " languages deleted")}})))})

(def language-get-all
  {:summary "Retrieves all languages"
   :permission-level "student"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body [string?]}}
   :handler (fn [req]
              {:status 200
               :body (map #(:id %) (languages/GET-ALL))})})
