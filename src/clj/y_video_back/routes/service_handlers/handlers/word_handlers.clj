(ns y-video-back.routes.service-handlers.handlers.word-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.words :as words]
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]))

(def word-create
  {:summary "Creates a new word"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may create a word with own user-id as word's user-id."
   :parameters {:header {:session-id uuid?}
                :body models/word-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters
                  p-vals :permission-values}]
              (if-not (or (:valid-type p-vals)
                          (= (:session-id-bypass env) (str session-id))
                          (= (ru/token-to-user-id session-id) (:user-id body)))
                {:status 403 :body {:message "forbidden"}}
                (if-not (users/EXISTS? (:user-id body))
                  {:status 500
                   :body {:message "user not found"}}
                  (if (words/EXISTS-BY-FIELDS? (:user-id body) (:word body) (:src-lang body) (:dest-lang body))
                    {:status 500
                     :body {:message "word already exists"}}
                    {:status 200
                     :body {:message "1 word created"
                            :id (utils/get-id (words/CREATE body))}}))))})

(def word-get-by-id
  {:summary "Retrieves specified word"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may retrieve a word with own user-id as word's user-id."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/word}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters
                  p-vals :permission-values}]
              (let [word-result (words/READ id)]
                (if-not (or (:valid-type p-vals)
                            (= (:session-id-bypass env) (str session-id))
                            (= (ru/token-to-user-id session-id) (:user-id word-result)))
                  {:status 403 :body {:message "forbidden"}}
                  (if (nil? word-result)
                    {:status 404
                     :body {:message "requested word not found"}}
                    {:status 200
                     :body word-result}))))})

(def word-update
  {:summary "Updates specified word"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may update a word with own user-id as word's user-id."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/word}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters
                  p-vals :permission-values}]
              (if-not (words/EXISTS? id)
                {:status 404
                 :body {:message "word not found"}}
                (let [current-word (words/READ id)
                      proposed-word (merge current-word body)
                      same-name-word (first (words/READ-ALL-BY-FIELDS [(:user-id proposed-word)
                                                                       (:word proposed-word)
                                                                       (:src-lang proposed-word)
                                                                       (:dest-lang proposed-word)]))]
                  (if-not (or (:valid-type p-vals)
                              (= (:session-id-bypass env) (str session-id))
                              (= (ru/token-to-user-id session-id) (:user-id current-word)))
                    {:status 403 :body {:message "forbidden"}}
                    ; If there is a collision and the collision is not with self (i.e. word being changed)
                    (if (and (not (nil? same-name-word))
                             (not (= (:id current-word)
                                     (:id same-name-word))))
                      {:status 500
                       :body {:message "unable to update word, identical word likely exists"}}
                      (if-not (users/EXISTS? (:user-id proposed-word))
                        {:status 500
                         :body {:message "user not found"}}
                        (let [result (words/UPDATE id body)]
                          (if (= 0 result)
                            {:status 500
                             :body {:message "unable to update word"}}
                            {:status 200
                             :body {:message (str result " words updated")}}))))))))})

(def word-delete
  {:summary "Deletes specified word"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may delete a word with own user-id as word's user-id."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters
                  p-vals :permission-values}]
              (let [word-result (words/READ id)]
                (if-not (or (:valid-type p-vals)
                            (= (:session-id-bypass env) (str session-id))
                            (= (ru/token-to-user-id session-id) (:user-id word-result)))
                  {:status 403 :body {:message "forbidden"}}
                  (let [result (words/DELETE id)]
                    (if (nil? result)
                      {:status 404
                       :body {:message "requested word not found"}}
                      {:status 200
                       :body {:message (str result " words deleted")}})))))})
