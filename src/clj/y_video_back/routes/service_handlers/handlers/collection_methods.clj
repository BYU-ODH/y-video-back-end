(ns y-video-back.routes.service-handlers.handlers.collection-methods
  (:require
   [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.users :as users]
   [y-video-back.db.courses :as courses]
   [y-video-back.db.contents :as contents]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.user-creator :as uc]))

(defn collection-create
  [body]
  (if (not (users/EXISTS? (:owner body)))
    {:status 500
     :body {:message "user (owner) not found, unable to create collection"}}
    (if (collections/EXISTS-NAME-OWNER? (:collection-name body)
                                        (:owner body))
      {:status 500
       :body {:message "collection name / owner combination already in use, unable to create collection"}}
      {:status 200
       :body {:message "1 collection created"
              :id (utils/get-id (collections/CREATE body))}})))

(defn collection-get-by-id
  [id]
  (let [res (collections/READ id)]
    (if (nil? res)
      {:status 404
       :body {:message "requested collection not found"}}
      {:status 200
       :body res})))

(defn collection-update
  [id body]
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
               :body {:message (str 1 " collections updated")}})))))))

(defn collection-delete
  [id]
  (let [result (collections/DELETE id)]
    (if (nil? result)
      {:status 404
       :body {:message "requested collection not found"}}
      {:status 200
       :body {:message (str 1 " collections deleted")}})))

(defn collection-add-user
  [id body]
  (if (not (collections/EXISTS? id))
    {:status 404
     :body {:message "collection not found"}}
    (let [username (:username body)]
      (if (nil? (users/READ-BY-USERNAME username))
        (uc/get-session-id username))
      (if (user-collections-assoc/EXISTS-COLL-USER? id username)
        (user-collections-assoc/DELETE-BY-IDS [id username]))
      (let [result (utils/get-id (user-collections-assoc/CREATE (into (dissoc body :username) {:collection-id id :username username})))]
        (if (nil? result)
          {:status 500
           :body {:message "unable to add user"}}
          {:status 200
           :body {:message (str 1 " users added to collection")
                  :id result}})))))

(defn collection-add-users
  [id body]
  (if (not (collections/EXISTS? id))
    {:status 404
     :body {:message "collection not found"}}
    (do
      (doseq [username (:usernames body)]
        (do
          (if (user-collections-assoc/EXISTS-COLL-USER? id username)
            (user-collections-assoc/DELETE-BY-IDS [id username]))
          (user-collections-assoc/CREATE {:collection-id id :username username
                                          :account-role (:account-role body)})
          (if (nil? (users/READ-BY-USERNAME username))
            (uc/get-session-id username))))
      {:status 200
       :body {:message (str (count (:usernames body)) " users added to collection")}})))

(defn collection-remove-user
  [id body]
  (if (not (collections/EXISTS? id))
    {:status 404
     :body {:message "collection not found"}}
    (let [user-id (:id (first (users/READ-BY-USERNAME [(:username body)])))
          body (assoc body :user-id user-id)]
      (if (not (users/EXISTS? (:user-id body)))
        {:status 500
         :body {:message "user not found"}}
        (if-not (user-collections-assoc/EXISTS-COLL-USER? id (:username body))
          {:status 500
           :body {:message "user not connected to collection"}}
          (let [result (user-collections-assoc/DELETE-BY-IDS [id (:username body)])]
            (if (= 0 result)
              {:status 500
               :body {:message "unable to remove user"}}
              {:status 200
               :body {:message (str result " users removed from collection")}})))))))

(defn collection-add-course
  [id department catalog-number section-number]
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
                      :id (str course-id)}})))))))

(defn collection-remove-course
  [id body]
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
             :body {:message (str result " courses removed from collection")}}))))))

(defn collection-get-all-contents
  [id]
  (if (not (collections/EXISTS? id))
    {:status 404
     :body {:message "collection not found"}}
    (let [raw-res (contents/READ-BY-COLLECTION-WITH-LAST-VERIFIED id)
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
      {:status 200
       :body {:content res-valid
              :expired-content res-expired}})))

(defn collection-get-all-courses
  [id]
  (if (not (collections/EXISTS? id))
    {:status 404
     :body {:message "collection not found"}}
    (let [course-collections-result (collection-courses-assoc/READ-COURSES-BY-COLLECTION id)
          course-result (map #(-> %
                                  (utils/remove-db-only)
                                  (dissoc :collection-id))
                             course-collections-result)]
        {:status 200
         :body course-result})))

(defn collection-get-all-users
  [id]
  (if (not (collections/EXISTS? id))
    {:status 404
     :body {:message "collection not found"}}
    (let [user-collections-result (user-collections-assoc/READ-USERS-BY-COLLECTION id)
          user-result (map #(-> %
                                (utils/remove-db-only)
                                (dissoc :collection-id))
                           user-collections-result)]
        {:status 200
         :body user-result})))






