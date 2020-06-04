(ns y-video-back.routes.service_handlers.collection_handlers
  (:require
   [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
   [y-video-back.db.users-by-collection :as users-by-collection]
   [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
   [y-video-back.db.user-collections-assoc :as user_collections_assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))


(def collection-create ;; Non-functional
  {:summary "Creates a new collection with the given (temp) user as an owner"
   :parameters {:body {:collection models/collection_without_id :user_id uuid?}}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 collection created"
                           :id (let [collection_id (utils/get-id (collections/CREATE (:collection body)))]
                                 (user_collections_assoc/CREATE {:user_id (:user_id body)
                                                                 :collection_id (utils/to-uuid collection_id)
                                                                 :account_role 0})
                                 collection_id)}}
                   (catch Exception e
                     {:status 409
                      :body {:message e}})))})

(def collection-get-by-id ;; Not tested
  {:summary "Retrieves specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/collection}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (collections/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body res})))})

(def collection-update ;; Non-functional
  {:summary "Updates the specified collection"
   :parameters {:path {:id uuid?} :body ::sp/collection}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (collections/UPDATE id body)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body {:message (str 1 " collections updated")}})))})

(def collection-delete ;; Non-functional
  {:summary "Deletes the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (collections/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body {:message (str 1 " collections deleted")}})))})

(def collection-add-user
  {:summary "Adds user to specified collection"
   :parameters {:path {:id uuid?} :body {:user-id uuid? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (utils/get-id (user_collections_assoc/CREATE (into body {:collection-id id})))]
                (if (= nil result)
                  {:status 404
                   :body {:message "unable to add user"}}
                  {:status 200
                   :body {:message (str 1 " users added to collection")
                          :id result}})))})

(def collection-remove-user
  {:summary "Removes user from specified collection"
   :parameters {:path {:id uuid?} :body {:user_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (user_collections_assoc/DELETE-BY-IDS [id (:user_id body)])]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove user"}}
                  {:status 200
                   :body {:message (str result " users removed from collection")}})))})

(def collection-add-content
  {:summary "Adds content to specified collection"
   :parameters {:path {:id uuid?} :body {:content-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (utils/get-id (collection_contents_assoc/CREATE (into body {:collection-id id})))]
                (if (= nil result)
                  {:status 404
                   :body {:message "unable to add content"}}
                  {:status 200
                   :body {:message (str 1 " contents added to collection")
                          :id result}})))})

(def collection-remove-content
  {:summary "Removes content from specified collection"
   :parameters {:path {:id uuid?} :body {:content-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (collection_contents_assoc/DELETE-BY-IDS [id (:content-id body)])]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove content"}}
                  {:status 200
                   :body {:message (str result " contents removed from collection")}})))})

(def collection-add-course
  {:summary "Adds course to specified collection"
   :parameters {:path {:id uuid?} :body {:course-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (utils/get-id (collection_courses_assoc/CREATE (into body {:collection-id id})))]
                (if (= nil result)
                  {:status 404
                   :body {:message "unable to add course"}}
                  {:status 200
                   :body {:message (str 1 " courses added to collection")
                          :id result}})))})

(def collection-remove-course
  {:summary "Removes course from specified collection"
   :parameters {:path {:id uuid?} :body {:course-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (collection_courses_assoc/DELETE-BY-IDS [id (:course-id body)])]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove course"}}
                  {:status 200
                   :body {:message (str result " courses removed from collection")}})))})


(def collection-get-all-contents ;; Non-functional
  {:summary "Retrieves all the contents for the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/content {:collection-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [content_collections_result (collection_contents_assoc/READ-CONTENTS-BY-COLLECTION id)]
                (let [content_result (map #(utils/remove-db-only %) content_collections_result)]
                  (if (= 0 (count content_result))
                    {:status 404
                     :body {:message "no contents found for given collection"}}
                    {:status 200
                     :body content_result}))))})

(def collection-get-all-courses ;; Non-functional
  {:summary "Retrieves all the courses for the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/course {:collection-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [course_collections_result (collection_courses_assoc/READ-CONTENTS-BY-COLLECTION id)]
                (let [course_result (map #(utils/remove-db-only %) course_collections_result)]
                  (if (= 0 (count course_result))
                    {:status 404
                     :body {:message "no courses found for given collection"}}
                    {:status 200
                     :body course_result}))))})

(def collection-get-all-users
  {:summary "Retrieves all users for the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/user {:account-role int? :collection-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [user_collections_result (user_collections_assoc/READ-USERS-BY-COLLECTION id)]
                (let [user_result (map #(utils/remove-db-only %) user_collections_result)]
                  (if (= 0 (count user_result))
                    {:status 404
                     :body {:message "no users found for given collection"}}
                    {:status 200
                     :body user_result}))))})
