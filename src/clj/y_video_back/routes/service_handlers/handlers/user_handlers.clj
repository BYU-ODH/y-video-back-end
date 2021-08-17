(ns y-video-back.routes.service-handlers.handlers.user-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.collections :as collections]
   [y-video-back.routes.service-handlers.handlers.collection-methods :as coll-methods]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.routes.service-handlers.utils.role-utils :as ru]
   [y-video-back.course-creator :as cc]
   [y-video-back.apis.persons :as persons]))

(def user-create
  {:summary "Creates a new user - FOR DEVELOPMENT ONLY"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :body models/user-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (if-not (= '() (users/READ-BY-USERNAME [(:username body)]))
                {:status 500
                 :body {:message "username already taken"}}
                {:status 200
                 :body {:message "1 user created"
                        :id (utils/get-id (users/CREATE body))}}))})


(def user-create-from-byu
  {:summary "Creates a new user only if byu data is valid"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :body models/user-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (if-not (= '() (users/READ-BY-USERNAME [(:username body)]))
                {:status 500
                 :body {:message "username already taken"}}
                {:status 200
                 :body (let [body body
                             byu-data (persons/get-user-data (:username body))
                             res (assoc body
                                        :account-name (get byu-data :full-name)
                                        :account-type (int (:account-type byu-data))
                                        :email (get byu-data :email))]
                              (if (get byu-data :byu-id) 
                                {:message "1 user created"
                                 :id (utils/get-id (users/CREATE res))}
                                {:message "username not created invalid BYU username" 
                                 :id "-"}))}))})
                 

(def user-get-by-id
  {:summary "Retrieves specified user"
   :permission-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/user}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [user-result (users/READ id)]
                (if (nil? user-result)
                  {:status 404
                   :body {:message "user not found"}}
                  {:status 200
                   :body user-result})))})


(def user-update
  {:summary "Updates specified user"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/user}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (users/UPDATE id body)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str 1 " users updated")}})))})  ; TODO Check that only 1 user really was updated

; delete everything that belongs to this user
(def user-delete
  {:summary "Deletes specified user"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (users/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str result " users deleted")}})))})


(def user-delete-with-collections
  {:summary "Deletes specified user and the collections"
   :permission-level "admin"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (users/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  (let [coll-deleted (for [x (collections/READ-ALL-BY-OWNER [id])
                              :let [y (:id x)]] (coll-methods/collection-delete y))] 
                    {:status 200
                     :body {:message (str (:username result) " user deleted. Called delete collections ->" coll-deleted)}})
                  )))})


(def user-get-logged-in
  {:summary "Retrieves the current logged-in user"
   :permission-level "student"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body models/user}
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


(def user-get-all-collections
  {:summary "Retrieves all collections the specified user owns"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may access this endpoint if their own user-id is the {id} in path."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/collection]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters
                  p-vals :permission-values}]
              (if-not (or (:valid-type p-vals)
                          (= (:session-id-bypass env) (str session-id))
                          (= (ru/token-to-user-id session-id) id))
                {:status 403 :body {:message "forbidden"}}
                (if-not (users/EXISTS? id)
                  {:status 404
                   :body {:message "user not found"}}
                  (let [owner-result (collections/READ-ALL-BY-OWNER [id])
                        collection-result (map #(-> %
                                                    (utils/remove-db-only)
                                                    (dissoc :user-id)
                                                    (dissoc :username)
                                                    (dissoc :account-role))
                                               owner-result)]
                    {:status 200
                     :body collection-result}))))})

(def user-get-all-collections-by-logged-in
  {:summary "Retrieves all collections for session user"
   :permission-level "student"
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body [(assoc (assoc models/collection :content [models/content]) :expired-content [{:content-title string?
                                                                                                         :content-id uuid?
                                                                                                         :resource-id uuid?}])]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header} :parameters}]
              (let [user-id (ru/token-to-user-id session-id)]
                (if-not (users/EXISTS? user-id)
                  {:status 404
                   :body {:message "user not found"}}
                  (let [user-owner-result (collections/READ-ALL-BY-OWNER [user-id])
                        username (:username (users/READ user-id))
                        user-collections-result (user-collections-assoc/READ-COLLECTIONS-BY-USER username)
                        user-courses-result (users/READ-COLLECTIONS-BY-USER-VIA-COURSES user-id)
                        courses-result (map #(-> %
                                                 (utils/remove-db-only)
                                                 (dissoc :user-id))
                                            user-courses-result)
                        collections-result (map #(-> %
                                                     (utils/remove-db-only)
                                                     (dissoc :user-id)
                                                     (dissoc :username)
                                                     (dissoc :account-role))
                                                user-collections-result)
                        owner-result (map #(-> %
                                               (utils/remove-db-only)
                                               (dissoc :user-id))
                                           user-owner-result)
                        total-result (map (fn [arg]
                                              (let [raw-res-all (contents/READ-BY-COLLECTION-WITH-LAST-VERIFIED (:id arg))
                                                    ; TODO also need to indicate there are contents with no resource-access at all
                                                    ; they are currently just getting dropped and ignored
                                                    ; TODO Currently, public collections still require a valid resource-access for
                                                    ; the owner. Ask Rob is this is correct, of if we need to change that.
                                                    raw-res (doall (filter #(or (not (nil? (:last-verified %)))
                                                                                (= "00000000-0000-0000-0000-000000000000" (str (:resource-id %))))
                                                                           raw-res-all))
                                                    raw-valid (doall (filter #(or (= "00000000-0000-0000-0000-000000000000" (str (:resource-id %)))
                                                                                  (utils/is-valid-access-time (:last-verified %)))
                                                                             raw-res))
                                                    raw-expired (doall (filter #(and (not (= "00000000-0000-0000-0000-000000000000" (str (:resource-id %))))
                                                                                     (not (utils/is-valid-access-time (:last-verified %))))
                                                                               raw-res))
                                                    res-valid (map #(utils/remove-db-only %) raw-valid)
                                                    res-expired (map (fn [arg]
                                                                       {:content-title (:title arg)
                                                                        :content-id (:id arg)
                                                                        :resource-id (:resource-id arg)})
                                                                     raw-expired)]

                                                (-> arg
                                                    (assoc :content res-valid)
                                                    (assoc :expired-content res-expired))))
                                          (distinct (concat courses-result collections-result owner-result)))]
                    {:status 200
                     :body total-result}))))})

(def user-get-all-courses
  {:summary "Retrieves all courses for specified user"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may access this endpoint if their own user-id is the {id} in path."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/course]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters
                  p-vals :permission-values}]
              (if-not (or (:valid-type p-vals)
                          (= (:session-id-bypass env) (str session-id))
                          (= (ru/token-to-user-id session-id) id))
                {:status 403 :body {:message "forbidden"}}
                (if-not (users/EXISTS? id)
                  {:status 404
                   :body {:message "user not found"}}
                  (let [user-courses-result (user-courses-assoc/READ-COURSES-BY-USER id)
                        course-result (map #(-> %
                                                (utils/remove-db-only)
                                                (dissoc :user-id)
                                                (dissoc :account-role))
                                           user-courses-result)]
                    {:status 200
                     :body course-result}))))})


(def user-get-all-words
  {:summary "Retrieves all words under specified user"
   :permission-level "lab-assistant"
   :bypass-permission true
   :permission-note "Any user may access this endpoint if their own user-id is the {id} in path."
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [models/word]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters
                  p-vals :permission-values}]
              (if-not (or (:valid-type p-vals)
                          (= (:session-id-bypass env) (str session-id))
                          (= (ru/token-to-user-id session-id) id))
                {:status 403 :body {:message "forbidden"}}
                (if-not (users/EXISTS? id)
                  {:status 404
                   :body {:message "user not found"}}
                  (let [user-words-result (users/READ-WORDS id)
                        word-result (map #(utils/remove-db-only %) user-words-result)]
                    {:status 200
                     :body word-result}))))})

(def refresh-courses
  {:summary "Queries api to refresh courses is enrolled in"
   :permission-level "student"
   ;:bypass-permission true
   :parameters {:header {:session-id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header } :parameters}]
              (let [user-id (ru/token-to-user-id session-id)
                    user-res (users/READ user-id)]
                (if (nil? user-res)
                  {:status 404
                   :body {:message "user not found"}}
                  (do
                    (cc/check-courses-with-api (:username user-res) true)
                    {:status 200
                     :body {:message "courses updated"}}))))})
