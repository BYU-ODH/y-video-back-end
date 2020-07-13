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
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if (courses/EXISTS-DEP-CAT-SEC? (:department body) (:catalog-number body) (:section-number body))
                {:status 500
                 :body {:message "department / catalog number / section number combination already in use, unable to create course"}
                 :headers {"session-id" session-id}}
                {:status 200
                 :body {:message "1 course created"
                        :id (utils/get-id (courses/CREATE body))}
                 :headers {"session-id" session-id}}))})

(def course-get-by-id ;; Non-functional
  {:summary "Retrieves specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/course}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [res (courses/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested course not found"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body res
                   :headers {"session-id" session-id}})))})

(def course-update ;; Non-functional
  {:summary "Updates the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/course}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (courses/EXISTS? id)
                {:status 404
                 :body {:message "course not found"}
                 :headers {"session-id" session-id}}
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
                     :body {:message "unable to update course, department / catalog / section combination likely in use"}
                     :headers {"session-id" session-id}}
                    (let [result (courses/UPDATE id body)]
                      (if (= 0 result)
                        {:status 500
                         :body {:message "unable to update course"}
                         :headers {"session-id" session-id}}
                        {:status 200
                         :body {:message (str result " courses updated")}
                         :headers {"session-id" session-id}}))))))})

(def course-delete ;; Non-functional
  {:summary "Deletes the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [result (courses/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested course not found"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body {:message (str result " courses deleted")}
                   :headers {"session-id" session-id}})))})

(def course-add-user
  {:summary "Adds user to specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if (not (courses/EXISTS? id))
                {:status 404
                 :body {:message "course not found"}
                 :headers {"session-id" session-id}}
                (if (not (users/EXISTS? (:user-id body)))
                  {:status 500
                   :body {:message "user not found"}
                   :headers {"session-id" session-id}}
                  (if (user-courses-assoc/EXISTS-CRSE-USER? id (:user-id body))
                    {:status 500
                     :body {:message "user already connected to course"}
                     :headers {"session-id" session-id}}
                    (let [result (utils/get-id (user-courses-assoc/CREATE (into body {:course-id id})))]
                      (if (= nil result)
                        {:status 500
                         :body {:message "unable to add user"}
                         :headers {"session-id" session-id}}
                        {:status 200
                         :body {:message (str 1 " users added to course")
                                :id result}
                         :headers {"session-id" session-id}}))))))})

(def course-remove-user
  {:summary "Removes user from specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if (not (courses/EXISTS? id))
                {:status 404
                 :body {:message "course not found"}
                 :headers {"session-id" session-id}}
                (if (not (users/EXISTS? (:user-id body)))
                  {:status 500
                   :body {:message "user not found"}
                   :headers {"session-id" session-id}}
                  (if-not (user-courses-assoc/EXISTS-CRSE-USER? id (:user-id body))
                    {:status 500
                     :body {:message "user not connected to course"}
                     :headers {"session-id" session-id}}
                    (let [result (user-courses-assoc/DELETE-BY-IDS [id (:user-id body)])]
                      (if (= 0 result)
                        {:status 500
                         :body {:message "unable to remove user"}
                         :headers {"session-id" session-id}}
                        {:status 200
                         :body {:message (str result " users removed from course")}
                         :headers {"session-id" session-id}}))))))})

(def course-get-all-users
  {:summary "Retrieves all users for the specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/user]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (courses/EXISTS? id)
                {:status 404
                 :body {:message "course not found"}
                 :headers {"session-id" session-id}}
                (let [user-courses-result (user-courses-assoc/READ-USERS-BY-COURSE id)]
                  (let [user-result (map #(-> %
                                              (utils/remove-db-only)
                                              (dissoc :course-id)
                                              (dissoc :account-role))
                                         user-courses-result)]
                    {:status 200
                     :body user-result
                     :headers {"session-id" session-id}}))))})

(def course-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified course"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/collection]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (courses/EXISTS? id)
                {:status 404
                 :body {:message "course not found"}
                 :headers {"session-id" session-id}}
                (let [course-collections-result (collection-courses-assoc/READ-COLLECTIONS-BY-COURSE id)]
                  (let [collection-result (map #(utils/remove-db-only %) course-collections-result)
                        remove-extra (map #(dissoc % :course-id) collection-result)]
                    {:status 200
                     :body remove-extra
                     :headers {"session-id" session-id}}))))})
