(ns y-video-back.routes.service-handlers.user-handlers
  (:require
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.collections :as collections]
   [y-video-back.models :as models]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def user-create
  {:summary "Creates a new user - FOR DEVELOPMENT ONLY"
   :parameters {:header {:session-id uuid?}
                :body models/user-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (= '() (users/READ-BY-EMAIL [(:email body)]))
                {:status 500
                 :body {:message "email already taken"}}
                {:status 200
                 :body {:message "1 user created"
                        :id (utils/get-id (users/CREATE body))}}))})

(def user-get-by-id
  {:summary "Retrieves specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/user}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [user-result (users/READ id)]
                (if (nil? user-result)
                  {:status 404
                   :body {:message "user not found"}}
                  {:status 200
                   :body user-result})))})


(def user-update
  {:summary "Updates specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/user}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (let [result (users/UPDATE id body)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str 1 " users updated")}})))})  ; I know, hard coded. Will change later.

(def user-delete
  {:summary "Deletes specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [result (users/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str result " users deleted")}})))})


(def user-get-logged-in ;; Non-functional
  {:summary "Retrieves the current logged-in user"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body models/user}
                     ;:header {:Access-Control-Allow-Origin "http://localhost:3000"}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              (let [user-id (ru/token-to-user-id session-id)]
                (if-not (users/EXISTS? user-id) ; this should only be true if using session-id-bypass
                  {:status 404
                   :body {:message "user not found"}}
                  (let [user-result (users/READ user-id)]
                    (if (nil? user-result)
                      {:status 500
                       :body {:message "user not found, not sure why"}}
                      (do
                        (users/UPDATE user-id {:last-login (str (utils/now))})
                        {:status 200
                         :body user-result}))))))})


(def user-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections the specified user owns"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/collection]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (users/EXISTS? id)
                {:status 404
                 :body {:message "user not found"}}
                (let [owner-result (collections/READ-ALL-BY-OWNER [id])]
                  (let [collection-result (map #(-> %
                                                    (utils/remove-db-only)
                                                    (dissoc :user-id)
                                                    (dissoc :account-role))
                                               owner-result)]
                      {:status 200
                       :body collection-result}))))})

(def user-get-all-collections-by-logged-in
  {:summary "Retrieves all collections for session user"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body [(assoc models/collection :content [models/content])]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              (let [user-id (ru/token-to-user-id session-id)]
                (if-not (users/EXISTS? user-id)
                  {:status 404
                   :body {:message "user not found"}}
                  (let [user-owner-result (collections/READ-ALL-BY-OWNER [user-id])
                        user-collections-result (user-collections-assoc/READ-COLLECTIONS-BY-USER user-id)
                        user-courses-result (users/READ-COLLECTIONS-BY-USER-VIA-COURSES user-id)]
                    (let [courses-result (map #(-> %
                                                   (utils/remove-db-only)
                                                   (dissoc :user-id))
                                              user-courses-result)
                          collections-result (map #(-> %
                                                       (utils/remove-db-only)
                                                       (dissoc :user-id)
                                                       (dissoc :account-role))
                                                  user-collections-result)
                          owner-result (map #(-> %
                                                 (utils/remove-db-only)
                                                 (dissoc :user-id))
                                             user-owner-result)
                          total-result (map #(-> %
                                                 (assoc :content (map utils/remove-db-only (contents/READ-BY-COLLECTION (:id %)))))
                                            (distinct (concat courses-result collections-result owner-result)))]
                      {:status 200
                       :body total-result})))))})

(def user-get-all-courses ;; Non-functional
  {:summary "Retrieves all courses for specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/course]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (users/EXISTS? id)
                {:status 404
                 :body {:message "user not found"}}
                (let [user-courses-result (user-courses-assoc/READ-COURSES-BY-USER id)]
                  (let [course-result (map #(-> %
                                                (utils/remove-db-only)
                                                (dissoc :user-id)
                                                (dissoc :account-role))
                                           user-courses-result)]
                      {:status 200
                       :body course-result}))))})


(def user-get-all-words
  {:summary "Retrieves all words under specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/word]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (users/EXISTS? id)
                {:status 404
                 :body {:message "user not found"}}
                (let [user-words-result (users/READ-WORDS id)]
                  (let [word-result (map #(utils/remove-db-only %) user-words-result)]
                      {:status 200
                       :body word-result}))))})
