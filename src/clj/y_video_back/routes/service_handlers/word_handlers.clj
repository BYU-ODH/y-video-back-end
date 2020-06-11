(ns y-video-back.routes.service_handlers.word_handlers
  (:require
   [y-video-back.db.words :as words]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]
   [y-video-back.routes.service_handlers.role_utils :as ru]))

(def word-create
  {:summary "Creates a new word"
   :parameters {:header {:session-id uuid?}
                :body models/word_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [user_id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "word-create" 0)
                ru/forbidden-page
                (try {:status 200
                      :body {:message "1 word created"
                             :id (utils/get-id (words/CREATE body))}}
                     (catch Exception e
                       {:status 409
                        :body {:message e}}))))})

(def word-get-by-id
  {:summary "Retrieves specified word"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/word}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "word-get-by-id" 0)
                ru/forbidden-page
                (let [word_result (words/READ id)]
                  (if (= "" (:id word_result))
                    {:status 404
                     :body {:message "requested word not found"}}
                    {:status 200
                     :body word_result}))))})

(def word-update
  {:summary "Updates specified word"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/word}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "word-update" 0)
                ru/forbidden-page
                (let [result (words/UPDATE id body)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested word not found"}}
                    {:status 200
                     :body {:message (str result " words updated")}}))))})

(def word-delete
  {:summary "Deletes specified word"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "word-delete" 0)
                ru/forbidden-page
                (let [result (words/DELETE id)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested word not found"}}
                    {:status 200
                     :body {:message (str result " words deleted")}}))))})
