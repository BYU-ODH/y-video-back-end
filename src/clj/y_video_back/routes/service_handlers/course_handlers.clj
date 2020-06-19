(ns y-video-back.routes.service-handlers.course-handlers
  (:require
   [y-video-back.db.courses :as courses]
   [y-video-back.db.users :as users]
   [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))

(def course-create ;; Non-functional
  {:summary "Creates a new course"
   :parameters {:header {:session-id uuid?}
                :body models/course-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-create" 0)
                ru/forbidden-page
                  (if (courses/EXISTS-DEP-CAT-SEC? (:department body) (:catalog-number body) (:section-number body))
                    {:status 500
                     :body {:message "department / catalog number / section number combination already in use, unable to create course"}}
                    {:status 200
                     :body {:message "1 course created"
                            :id (utils/get-id (courses/CREATE body))}})))})

(def course-get-by-id ;; Non-functional
  {:summary "Retrieves specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/course}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-get-by-id" {:course-id id})
                ru/forbidden-page
                (let [res (courses/READ id)]
                  (if (nil? res)
                    {:status 404
                     :body {:message "requested course not found"}}
                    {:status 200
                     :body res}))))})

(def course-update ;; Non-functional
  {:summary "Updates the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/course}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-update" {:course-id id})
                ru/forbidden-page
                (if-not (courses/EXISTS? id)
                  {:status 404
                   :body {:message "course not found"}}
                  (let [current-course (courses/READ id)
                        proposed-course (merge current-course body)
                        same-name-course (first (courses/READ-ALL-BY-DEP-CAT-SEC [(:department proposed-course)
                                                                                  (:catalog-number proposed-course)
                                                                                  (:section-number proposed-course)]))]
                    ; If there is a collision and the collision is not with self (i.e. course being changed)
                    (if (and (not (nil? same-name-course))
                             (not (= (:id current-course)
                                     (:id same-name-course))))
                      {:status 500
                       :body {:message "unable to update course, department / catalog / section combination likely in use"}}
                      (let [result (courses/UPDATE id body)]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to update course"}}
                          {:status 200
                           :body {:message (str result " courses updated")}})))))))})

(def course-delete ;; Non-functional
  {:summary "Deletes the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-delete" {:course-id id})
                ru/forbidden-page
                (let [result (courses/DELETE id)]
                  (if (nil? result)
                    {:status 404
                     :body {:message "requested course not found"}}
                    {:status 200
                     :body {:message (str result " courses deleted")}}))))})

(def course-add-user
  {:summary "Adds user to specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-add-user" {:course-id id})
                ru/forbidden-page
                (if (not (courses/EXISTS? id))
                  {:status 404
                   :body {:message "course not found"}}
                  (if (not (users/EXISTS? (:user-id body)))
                    {:status 500
                     :body {:message "user not found"}}
                    (if (user-courses-assoc/EXISTS-CRSE-USER? id (:user-id body))
                      {:status 500
                       :body {:message "user already connected to course"}}
                      (let [result (utils/get-id (user-courses-assoc/CREATE (into body {:course-id id})))]
                        (if (= nil result)
                          {:status 500
                           :body {:message "unable to add user"}}
                          {:status 200
                           :body {:message (str 1 " users added to course")
                                  :id result}})))))))})

(def course-remove-user
  {:summary "Removes user from specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "course-remove-user" {:course-id id})
                ru/forbidden-page
                (if (not (courses/EXISTS? id))
                  {:status 404
                   :body {:message "course not found"}}
                  (if (not (users/EXISTS? (:user-id body)))
                    {:status 500
                     :body {:message "user not found"}}
                    (if-not (user-courses-assoc/EXISTS-CRSE-USER? id (:user-id body))
                      {:status 500
                       :body {:message "user not connected to course"}}
                      (let [result (user-courses-assoc/DELETE-BY-IDS [id (:user-id body)])]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to remove user"}}
                          {:status 200
                           :body {:message (str result " users removed from course")}})))))))})

(def course-get-all-users
  {:summary "Retrieves all users for the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/user {:account-role int? :course-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-get-all-users" {:course-id id})
                ru/forbidden-page
                (if-not (courses/EXISTS? id)
                  {:status 404
                   :body {:message "course not found"}}
                  (let [user-courses-result (user-courses-assoc/READ-USERS-BY-COURSE id)]
                    (let [user-result (map #(utils/remove-db-only %) user-courses-result)]
                      {:status 200
                       :body user-result})))))})

(def course-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:course-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "course-get-all-collections" {:course-id id})
                ru/forbidden-page
                (if-not (courses/EXISTS? id)
                  {:status 404
                   :body {:message "course not found"}}
                  (let [course-collections-result (collection-courses-assoc/READ-COLLECTIONS-BY-COURSE id)]
                    (let [collection-result (map #(utils/remove-db-only %) course-collections-result)]
                      {:status 200
                       :body collection-result})))))})
