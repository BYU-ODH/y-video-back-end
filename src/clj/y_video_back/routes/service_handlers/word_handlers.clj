(ns y-video-back.routes.service_handlers.word_handlers
  (:require
   [y-video-back.db.words :as words]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))

(def word-create
  {:summary "Creates a new word"
   :parameters {:body models/word_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{{:keys [user_id]} :path :keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 word created"
                           :id (utils/get-id (words/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message e}})))})

(def word-get-by-id
  {:summary "Retrieves specified word"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/word}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [word_result (words/READ id)]
                (if (= "" (:id word_result))
                  {:status 404
                   :body {:message "requested word not found"}}
                  {:status 200
                   :body word_result})))})

(def word-update
  {:summary "Updates specified word"
   :parameters {:path {:id uuid?} :body ::sp/word}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (words/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested word not found"}}
                  {:status 200
                   :body {:message (str result " words updated")}})))})

(def word-delete
  {:summary "Deletes specified word"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (words/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested word not found"}}
                  {:status 200
                   :body {:message (str result " words deleted")}})))})
