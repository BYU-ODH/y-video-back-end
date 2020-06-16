(ns y-video-back.routes.service_handlers.user_handlers
  (:require
   [y-video-back.db.user-collections-assoc :as user_collections_assoc]
   [y-video-back.db.user-courses-assoc :as user_courses_assoc]
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]
   [y-video-back.routes.service_handlers.role_utils :as ru]))


(def user-create
  {:summary "Creates a new user - FOR DEVELOPMENT ONLY"
   :parameters {:header {:session-id uuid?}
                :body models/user_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?
                           :error string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "user-create" 0)
                ru/forbidden-page
                (try {:status 200
                      :body {:message "1 user created"
                             :id (utils/get-id (users/CREATE body))}}
                     (catch Exception e
                       {:status 409
                        :body {:message "unable to create user, email likely taken"
                               :error (.toString e)}}))))})

(def user-get-by-id
  {:summary "Retrieves specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/user}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-get-by-id" 0)
                ru/forbidden-page
                (let [user_result (users/READ id)]
                  (if (nil? user_result)
                    {:status 404
                     :body {:message "user not found"}}
                    {:status 200
                     :body user_result}))))})


(def user-update
  {:summary "Updates specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/user}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "user-update" 0)
                ru/forbidden-page
                (let [result (users/UPDATE id body)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested user not found"}}
                    {:status 200
                     :body {:message (str 1 " users updated")}}))))})  ; I know, hard coded. Will change later.

(def user-delete
  {:summary "Deletes specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-delete" 0)
                ru/forbidden-page
                (let [result (users/DELETE id)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested user not found"}}
                    {:status 200
                     :body {:message (str result " users deleted")}}))))})


(def user-get-logged-in ;; Non-functional
  {:summary "Retrieves the current logged-in user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/user}
                     ;:header {:Access-Control-Allow-Origin "http://localhost:3000"}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-get-logged-in" 0)
                ru/forbidden-page
                (let [user_result (users/READ id)]
                  (if (nil? user_result)
                    {:status 404
                     :body {:message "user not found"}}
                    {:status 200
                     :body (utils/user-db-to-front user_result)}))))})
                     ;:header {"Access-Control-Allow-Origin" "*"}}))))})


(def user-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:account-role int? :user-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-get-all-collections" 0)
                ru/forbidden-page
                (let [user_collections_result (user_collections_assoc/READ-COLLECTIONS-BY-USER id)]
                  (let [collection_result (map #(utils/remove-db-only %) user_collections_result)]
                    (if (= 0 (count collection_result))
                      {:status 404
                       :body {:message "no users found for given collection"}}
                      {:status 200
                       :body collection_result})))))})

(def user-get-all-collections-by-logged-in
  {:summary "Retrieves all collections for session user"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body [(into models/collection {:account-role int? :user-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              (if-not (ru/has-permission session-id "user-get-all-collections" 0)
                ru/forbidden-page
                (let [user_collections_result (user_collections_assoc/READ-COLLECTIONS-BY-USER (ru/token-to-user-id session-id))]
                  (let [collection_result (map #(utils/remove-db-only %) user_collections_result)]
                    (if (= 0 (count collection_result))
                      {:status 404
                       :body []}
                      {:status 200
                       :body collection_result})))))})

(def user-get-all-courses ;; Non-functional
  {:summary "Retrieves all courses for specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/course {:account-role int? :user-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-get-all-courses" 0)
                ru/forbidden-page
                (let [user_courses_result (user_courses_assoc/READ-COURSES-BY-USER id)]
                  (let [course_result (map #(utils/remove-db-only %) user_courses_result)]
                    (if (= 0 (count course_result))
                      {:status 404
                       :body {:message "no users found for given course"}}
                      {:status 200
                       :body course_result})))))})


(def user-get-all-words
  {:summary "Retrieves all words under specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/word]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-get-all-words" 0)
                ru/forbidden-page
                (let [res (users/READ-WORDS id)]
                  {:status 200
                   :body res})))})
