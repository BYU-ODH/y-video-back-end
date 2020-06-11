(ns y-video-back.routes.service_handlers.course_handlers
  (:require
   [y-video-back.db.courses :as courses]
   [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
   [y-video-back.db.user-courses-assoc :as user_courses_assoc]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]
   [y-video-back.routes.service_handlers.role_utils :as ru]))

(def course-create ;; Non-functional
  {:summary "Creates a new course"
   :parameters {:header {:session-id uuid?}
                :body models/course_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-create" 0)
                ru/forbidden-page
                (try {:status 200
                      :body {:message "1 course created"
                             :id (utils/get-id (courses/CREATE body))}}
                     (catch Exception e
                       {:status 409
                        :body {:message (e)}}))))})

(def course-get-by-id ;; Non-functional
  {:summary "Retrieves specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/course}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-get-by-id" 0)
                ru/forbidden-page
                (let [result (courses/READ id)]
                  (if (= "" (:id result))
                    {:status 404
                     :body {:message "requested course not found"}}
                    {:status 200
                     :body result}))))})

(def course-update ;; Non-functional
  {:summary "Updates the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/course}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-update" 0)
                ru/forbidden-page
                (let [result (courses/UPDATE id body)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested course not found"}}
                    {:status 200
                     :body {:message (str result " courses updated")}}))))})

(def course-delete ;; Non-functional
  {:summary "Deletes the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-delete" 0)
                ru/forbidden-page
                (let [result (courses/DELETE id)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested course not found"}}
                    {:status 200
                     :body {:message (str result " courses deleted")}}))))})

(def course-add-collection
  {:summary "Adds collection to specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:collection_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-add-collection" 0)
                ru/forbidden-page
                (let [result "placeholder"]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "unable to add collection"}}
                    {:status 200
                     :body {:message (str result " collections added to course")}}))))})


(def course-remove-collection
  {:summary "Removes collection from specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:collection_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-remove-collection" 0)
                ru/forbidden-page
                (let [result "placeholder"] ;; needs adjustment
                  (if (= 0 result)
                    {:status 404
                     :body {:message "unable to remove collection"}}
                    {:status 200
                     :body {:message (str result " collections removed from course")}}))))})

(def course-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:course-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-get-all-collections" 0)
                ru/forbidden-page
                (let [course_collections_result (collection_courses_assoc/READ-COLLECTIONS-BY-CONTENT id)]
                  (let [collection_result (map #(utils/remove-db-only %) course_collections_result)]
                    (if (= 0 (count collection_result))
                      {:status 404
                       :body {:message "no courses found for given collection"}}
                      {:status 200
                       :body collection_result})))))})

(def course-add-user
  {:summary "Adds user to specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-add-user" {:course-id id})
                ru/forbidden-page
                (let [result (utils/get-id (user_courses_assoc/CREATE (into body {:course-id id})))]
                  (if (= nil result)
                    {:status 404
                     :body {:message "unable to add user"}}
                    {:status 200
                     :body {:message (str 1 " users added to course")
                            :id result}}))))})

(def course-remove-user
  {:summary "Removes user from specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-remove-user" 0)
                ru/forbidden-page
                (let [result (user_courses_assoc/DELETE-BY-IDS [id (:user_id body)])]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "unable to remove user"}}
                    {:status 200
                     :body {:message (str result " users removed from course")}}))))})

(def course-get-all-users
  {:summary "Retrieves all users for the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/user {:account-role int? :course-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-get-all-users" 0)
                ru/forbidden-page
                (let [user_courses_result (user_courses_assoc/READ-USERS-BY-COURSE id)]
                  (let [user_result (map #(utils/remove-db-only %) user_courses_result)]
                    (if (= 0 (count user_result))
                      {:status 404
                       :body {:message "no users found for given course"}}
                      {:status 200
                       :body user_result})))))})
