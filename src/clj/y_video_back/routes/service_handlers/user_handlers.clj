(ns y-video-back.routes.service_handlers.user_handlers
  (:require
   [y-video-back.db.user-collections-assoc :as user_collections_assoc]
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))


(def user-create
  {:summary "Creates a new user - FOR DEVELOPMENT ONLY"
   :parameters {:body models/user_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?
                           :error string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 user created"
                           :id (utils/get-id (users/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message "unable to create user, email likely taken"
                             :error (.toString e)}})))})

(def user-get-by-id
  {:summary "Retrieves specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/user}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [user_result (users/READ id)]
                (if (nil? user_result)
                  {:status 404
                   :body {:message "user not found"}}
                  {:status 200
                   :body user_result})))})


(def user-update
  {:summary "Updates specified user"
   :parameters {:path {:id uuid?} :body ::sp/user}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (users/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str 1 " users updated")}})))})  ; I know, hard coded. Will change later.

(def user-delete
  {:summary "Deletes specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (users/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str result " users deleted")}})))})


(def user-get-logged-in ;; Non-functional
  {:summary "Retrieves the current logged-in user"
   :parameters {:query {:user_id string?}}
   :responses {200 {:body {:user_id string? :email string? :lastlogin string? :name string? :role int? :username string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :query} :parameters}]
              (let [user_result (users/READ id)]
                (if (nil? user_result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message user_result}})))})


(def user-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:account-role int? :user-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [user_collections_result (user_collections_assoc/READ-COLLECTIONS-BY-USER id)]
                (let [collection_result (map #(utils/remove-db-only %) user_collections_result)]
                  (if (= 0 (count collection_result))
                    {:status 404
                     :body {:message "no users found for given collection"}}
                    {:status 200
                     :body collection_result}))))})

(def user-get-all-words
  {:summary "Retrieves all words under specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [models/word]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (users/READ-WORDS id)]
                {:status 200
                 :body res}))})
