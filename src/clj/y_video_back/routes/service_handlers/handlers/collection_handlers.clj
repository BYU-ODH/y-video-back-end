(ns y-video-back.routes.service-handlers.handlers.collection-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.collections :as collections]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [y-video-back.utils.account-permissions :as ac]
   [y-video-back.routes.service-handlers.handlers.collection-methods :as methods]))

(def collection-create
  {:summary "Creates a new collection with the given (temp) user as an owner"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Instructors may create a collection with their own user-id as the collection's owner. Only admins may create public collections."
   :parameters {:header {:session-id uuid?}
                :body models/collection-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters
                  p-vals :permission-values}]
              (let [user-id (ru/token-to-user-id session-id)]
                (if-not (or (= (:session-id-bypass env) (str session-id))
                            (:valid-type p-vals)
                            (and (= (ru/get-user-type user-id) (ac/to-int-type "instructor"))
                                 (= user-id (:owner body))))
                  {:status 403 :body {:message "forbidden"}}
                  (if (and (:public body)
                           (not (= (ac/to-int-type "admin") (ru/get-user-type user-id))))
                    {:status 403 :body {:message "forbidden"}}
                    (methods/collection-create body)))))})

(def collection-get-by-id
  {:summary "Retrieves specified collection"
   :permission-level "lab-assistant"
   :role-level "auditing"
   :path-to-id [:parameters :path :id]
   :bypass-permission true
   :permission-note "Any user may get public collections."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/collection}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters p-vals :permission-values}]
              (if (or (= (:session-id-bypass env) (str session-id))
                      (:valid-type p-vals) (:valid-role p-vals) (not (nil? (collections/READ-PUBLIC id))))
                (methods/collection-get-by-id id)
                {:status 403 :body {:message "forbidden"}}))})

(def collection-update
  {:summary "Updates the specified collection"
   :permission-level "lab-assistant"
   :role-level "ta"
   :permission-note "Only admin may change public flag. Admins should only change collections owned by admins (this is necessary, but not currently enforced)."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/collection}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if (and (not (= (:session-id-bypass env) (str session-id)))
                       (:public body)
                       (not (= (ac/to-int-type "admin") (ru/get-user-type (ru/token-to-user-id session-id)))))
                {:status 403 :body {:message "forbidden"}}
                (methods/collection-update id body)))})

(def collection-delete
  {:summary "Deletes the specified collection"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (methods/collection-delete id))})

(def collection-add-user
  {:summary "Adds user to specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :permission-note "If collection is public, any user may add other user to collection as auditing"
   :bypass-permission true
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:username string? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters p-vals :permission-values}]
              (if (or (= (:session-id-bypass env) (str session-id))
                      (:valid-role p-vals) (:valid-type p-vals) (and (not (nil? (collections/READ-PUBLIC id)))
                                                                     (= 3 (:account-role body))))
                (methods/collection-add-user id body)
                {:status 403 :body {:message "forbidden"}}))})

(def collection-add-users
  {:summary "Adds list of users to specified collection. All will have same role. Will not override existing connections."
   :permission-level "lab-assistant"
   :role-level "instructor"
   :bypass-permission true
   :permission-note "If collection is public, instructors may add any users to collection as auditing."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:usernames [string?] :account-role int?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters p-vals :permission-values}]
              (if (or (= (:session-id-bypass env) (str session-id))
                      (:valid-role p-vals) (:valid-type p-vals) (and (not (nil? (collections/READ-PUBLIC id)))
                                                                     (= 3 (:account-role body))))
                (methods/collection-add-users id body)
                {:status 403 :body {:message "forbidden"}}))})

(def collection-remove-user
  {:summary "Removes user from specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :bypass-permission true
   :permission-note "If collection is public, users may remove self from collection."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:username string?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters p-vals :permission-values}]
              (if (or (= (:session-id-bypass env) (str session-id))
                      (:valid-role p-vals) (:valid-type p-vals) (and (not (nil? (collections/READ-PUBLIC id)))
                                                                     (= (:username body) (:username (ru/token-to-user session-id)))))
                (methods/collection-remove-user id body)
                {:status 403 :body {:message "forbidden"}}))})

(def collection-add-course
  {:summary "Adds course to specified collection. Creates course in database if does not already exist."
   :permission-level "lab-assistant"
   :role-level "instructor"
   :permission-note "Instructors may add any course to any public collection."
   :bypass-permission true
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}
                :body {:department string?
                       :catalog-number string?
                       :section-number string?}}
   :responses {200 {:body {:message string? :id string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path
                   {:keys [department catalog-number section-number]} :body} :parameters
                   p-vals :permission-values}]
              (if (or (= (:session-id-bypass env) (str session-id))
                      (:valid-role p-vals) (:valid-type p-vals) (and (not (nil? (collections/READ-PUBLIC id)))
                                                                     (<= (ru/get-user-type (ru/token-to-user-id session-id))
                                                                         (ac/to-int-type "instructor"))))
                (methods/collection-add-course id department catalog-number section-number)
                {:status 403 :body {:message "forbidden"}}))})

(def collection-remove-course
  {:summary "Removes course from specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :permission-note "Instructors must contact lab-assistants to remove a course from a public collection. This will likely be a temporary fix."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:course-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 (:body {:message string?})
               500 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (methods/collection-remove-course id body))})

(def collection-get-all-contents
  {:summary "Retrieves all the resources for the specified collection"
   :permission-level "lab-assistant"
   :role-level "auditing"
   :bypass-permission true
   :permission-note "Any user may get contents for a public collection."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:content [models/content]
                           :expired-content [{:content-title string?
                                              :content-id uuid?
                                              :resource-id uuid?}]}}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters p-vals :permission-values}]
              (if (or (= (:session-id-bypass env) (str session-id))
                      (:valid-role p-vals) (:valid-type p-vals) (not (nil? (collections/READ-PUBLIC id))))
                (methods/collection-get-all-contents id)
                {:status 403 :body {:message "forbidden"}}))})

(def collection-get-all-courses
  {:summary "Retrieves all the courses for the specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/course]}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (methods/collection-get-all-courses id))})

(def collection-get-all-users
  {:summary "Retrieves all users for the specified collection"
   :permission-level "lab-assistant"
   :role-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(assoc models/user :account-role int?)]}
               404 (:body {:message string?})}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (methods/collection-get-all-users id))})







