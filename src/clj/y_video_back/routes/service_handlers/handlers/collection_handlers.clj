(ns y-video-back.routes.service-handlers.handlers.collection-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.users :as users]
   [y-video-back.db.courses :as courses]
   [y-video-back.db.contents :as contents]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [y-video-back.utils.account-permissions :as ac]))

(def collection-create ;; Non-functional
  {:summary "Creates a new collection with the given (temp) user as an owner"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Instructors may create a collection with their own user-id as the collection's owner."
   :parameters {:header {:session-id uuid?}
                :body models/collection-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters
                  p-vals :permission-values}]
              (if-not (or (= (:session-id-bypass env) (str session-id))
                          (:valid-type p-vals)
                          (and (= (ru/get-user-type (ru/token-to-user-id session-id)) (ac/to-int-type "instructor"))
                               (= (ru/token-to-user-id session-id) (:owner body))))
                {:status 401 :body {:message "unauthorized"}}
                (if (not (users/EXISTS? (:owner body)))
                  {:status 500
                   :body {:message "user (owner) not found, unable to create collection"}}
                  (if (collections/EXISTS-NAME-OWNER? (:collection-name body)
                                                      (:owner body))
                    {:status 500
                     :body {:message "collection name / owner combination already in use, unable to create collection"}}
                    {:status 200
                     :body {:message "1 collection created"
                            :id (utils/get-id (collections/CREATE body))}}))))})

(def collection-get-by-id ;; Not tested
  {:summary "Retrieves specified collection"
   :permission-level "lab-assistant"
   :role-level "auditing"
   :path-to-id [:parameters :path :id]
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
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
   :permission-level "lab-assistant"
   :role-level "ta"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/collection}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if-not (collections/EXISTS? id)
                {:status 404
                 :body {:message "collection not found"}}
                (let [current-collection (collections/READ id)
                      proposed-collection (merge current-collection body)
                      same-name-collection (first (collections/READ-ALL-BY-NAME-OWNER [(:collection-name proposed-collection)
                                                                                       (:owner proposed-collection)]))]
                  ; If there is a name-owner collision and the collision is not with self (i.e. collection being changed)
                  (if (and (not (nil? same-name-collection))
                           (not (= (:id current-collection)
                                   (:id same-name-collection))))
                    {:status 500
                     :body {:message "unable to update collection, name-owner pair likely in use"}}
                    (if-not (users/EXISTS? (:owner proposed-collection))
                      {:status 500
                       :body {:message "user (owner) not found, unable to create collection"}}
                      (let [result (collections/UPDATE id body)]
                        (if (nil? result)
                          {:status 500
                           :body {:message "unable to update collection"}}
                          {:status 200
                           :body {:message (str 1 " collections updated")}})))))))})

(def collection-delete ;; Non-functional
  {:summary "Deletes the specified collection"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (collections/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body {:message (str 1 " collections deleted")}})))})

(def collection-add-user
  {:summary "Adds user to specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:username string? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (let [user-id (:id (first (users/READ-BY-USERNAME [(:username body)])))]
                  (if (nil? user-id)
                    {:status 500
                     :body {:message (str "user: " (:username body) " not found")}}
                    (if (user-collections-assoc/EXISTS-COLL-USER? id user-id)
                      {:status 500
                       :body {:message "user already connected to collection"}}
                      (let [result (utils/get-id (user-collections-assoc/CREATE (into (dissoc body :username) {:collection-id id :user-id user-id})))]
                        (if (nil? result)
                          {:status 500
                           :body {:message "unable to add user"}}
                          {:status 200
                           :body {:message (str 1 " users added to collection")
                                  :id result}})))))))})

(def collection-remove-user
  {:summary "Removes user from specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:username string?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (let [user-id (:id (first (users/READ-BY-USERNAME [(:username body)])))
                      body (assoc (dissoc body :username) :user-id user-id)]
                  (if (not (users/EXISTS? (:user-id body)))
                    {:status 500
                     :body {:message "user not found"}}
                    (if-not (user-collections-assoc/EXISTS-COLL-USER? id (:user-id body))
                      {:status 500
                       :body {:message "user not connected to collection"}}
                      (let [result (user-collections-assoc/DELETE-BY-IDS [id (:user-id body)])]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to remove user"}}
                          {:status 200
                           :body {:message (str result " users removed from collection")}})))))))})

(def collection-add-course
  {:summary "Adds course to specified collection. Creates course in database if does not already exist."
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}
                :body {:department string?
                       :catalog-number string?
                       :section-number string?}}
   :responses {200 {:body {:message string? :id string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path {:keys [department catalog-number section-number]} :body} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (do
                  (if-not (courses/EXISTS-DEP-CAT-SEC? department catalog-number section-number)
                    (courses/CREATE {:department department :catalog-number catalog-number :section-number section-number}))
                  (let [course-id (:id (first (courses/READ-ALL-BY-DEP-CAT-SEC [department catalog-number section-number])))]
                    (if (collection-courses-assoc/EXISTS-COLL-CRSE? id course-id)
                      {:status 500
                       :body {:message "course already connected to collection"}}
                      (let [result (utils/get-id (collection-courses-assoc/CREATE {:collection-id id :course-id course-id}))]
                        (if (= nil result)
                          {:status 500
                           :body {:message "unable to add course"}}
                          {:status 200
                           :body {:message (str 1 " courses added to collection")
                                  :id (str course-id)}})))))))})

(def collection-remove-course
  {:summary "Removes course from specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:course-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (if (not (courses/EXISTS? (:course-id body)))
                  {:status 500
                   :body {:message "course not found"}}
                  (if-not (collection-courses-assoc/EXISTS-COLL-CRSE? id (:course-id body))
                    {:status 500
                     :body {:message "course not connected to collection"}}
                    (let [result (collection-courses-assoc/DELETE-BY-IDS [id (:course-id body)])]
                      (if (= 0 result)
                        {:status 404
                         :body {:message "unable to remove course"}}
                        {:status 200
                         :body {:message (str result " courses removed from collection")}}))))))})


(def collection-get-all-contents ;; Non-functional
  {:summary "Retrieves all the resources for the specified collection"
   :permission-level "lab-assistant"
   :role-level "auditing"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/content]}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (let [raw-res (contents/READ-BY-COLLECTION id)
                      res (map #(utils/remove-db-only %) raw-res)]
                  {:status 200
                   :body res})))})

(def collection-get-all-courses ;; Non-functional
  {:summary "Retrieves all the courses for the specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/course]}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (let [course-collections-result (collection-courses-assoc/READ-COURSES-BY-COLLECTION id)
                      course-result (map #(-> %
                                              (utils/remove-db-only)
                                              (dissoc :collection-id))
                                         course-collections-result)]
                    {:status 200
                     :body course-result})))})

(def collection-get-all-users
  {:summary "Retrieves all users for the specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/user]}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (if (not (collections/EXISTS? id))
                {:status 404
                 :body {:message "collection not found"}}
                (let [user-collections-result (user-collections-assoc/READ-USERS-BY-COLLECTION id)
                      user-result (map #(-> %
                                            (utils/remove-db-only)
                                            (dissoc :collection-id)
                                            (dissoc :account-role))
                                       user-collections-result)]
                    {:status 200
                     :body user-result})))})
