(ns legacy.utils.route-proxy.routes.rp-collection
  (:require
    [y-video-back.config :refer [env]]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.utils.utils :as ut]))

(defn ap2
  [arg]
  (let [res (app arg)]
    (ut/check-header res)
    res))

(defn collection-post
  "Create a collection via app's post request"
  ([session-id collection]
   (ap2 (-> (request :post "/api/collection")
            (header :session-id session-id)
            (json-body collection))))
  ([collection]
   (collection-post (:session-id-bypass env) collection)))

(defn collection-id-get
  "Retrieves collection via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/collection/" id))
            (header :session-id session-id))))
  ([id]
   (collection-id-get (:session-id-bypass env) id)))

(defn collection-id-patch
  "Updates collection via app's patch (id) request"
  ([session-id id new-collection]
   (ap2 (-> (request :patch (str "/api/collection/" id))
            (header :session-id session-id)
            (json-body new-collection))))
  ([id new-collection]
   (collection-id-patch (:session-id-bypass env) id new-collection)))

(defn collection-id-delete
  "Deletes collection via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/collection/" id))
            (header :session-id session-id))))
  ([id]
   (collection-id-delete (:session-id-bypass env) id)))


(defn collection-id-add-user
  "Connects user and collection"
  ([session-id collection-id username role]
   (ap2 (-> (request :post (str "/api/collection/" collection-id "/add-user"))
            (header :session-id session-id)
            (json-body {:username username :account-role role}))))
  ([collection-id username role]
   (collection-id-add-user (:session-id-bypass env) collection-id username role)))

(defn collection-id-add-users
  "Connects list of users and collection"
  ([session-id collection-id usernames role]
   (ap2 (-> (request :post (str "/api/collection/" collection-id "/add-users"))
            (header :session-id session-id)
            (json-body {:usernames usernames :account-role role}))))
  ([collection-id usernames role]
   (collection-id-add-users (:session-id-bypass env) collection-id usernames role)))

(defn collection-id-remove-user
  "Connects user and collection"
  ([session-id collection-id username]
   (ap2 (-> (request :post (str "/api/collection/" collection-id "/remove-user"))
            (header :session-id session-id)
            (json-body {:username username}))))
  ([collection-id username]
   (collection-id-remove-user (:session-id-bypass env) collection-id username)))


(defn collection-id-users
  "Reads all users connected to collection"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/collection/" id "/users"))
            (header :session-id session-id))))
  ([id]
   (collection-id-users (:session-id-bypass env) id)))

(defn collection-id-add-course
  "Connects course and collection"
  ([session-id collection-id dept ctlg sctn]
   (ap2 (-> (request :post (str "/api/collection/" collection-id "/add-course"))
            (json-body {:department dept
                        :catalog-number ctlg
                        :section-number sctn})
            (header :session-id session-id))))
  ([collection-id dept ctlg sctn]
   (collection-id-add-course (:session-id-bypass env) collection-id dept ctlg sctn)))

(defn collection-id-remove-course
  "Connects course and collection"
  ([session-id collection-id course-id]
   (ap2 (-> (request :post (str "/api/collection/" collection-id "/remove-course"))
            (json-body {:course-id course-id})
            (header :session-id session-id))))
  ([collection-id course-id]
   (collection-id-remove-course (:session-id-bypass env) collection-id course-id)))

(defn collection-id-courses
  "Reads all courses connected to collection"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/collection/" id "/courses"))
            (header :session-id session-id))))
  ([id]
   (collection-id-courses (:session-id-bypass env) id)))

(defn collection-id-contents
  "Reads all contents connected to collection"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/collection/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (collection-id-contents (:session-id-bypass env) id)))


(defn collections-by-logged-in
  "Retrieves all collections for current user (by session id)"
  [session-id]
  (ap2 (-> (request :get (str "/api/collections"))
           (header :session-id session-id))))

