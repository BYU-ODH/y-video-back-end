(ns y-video-back.utils.route-proxy-parts.rp-course
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))


(defn course-post
  "Create a course via app's post request"
  ([session-id course-without-id]
   (app (-> (request :post "/api/course")
            (json-body course-without-id)
            (header :session-id session-id))))
  ([course-without-id]
   (course-post (:session-id-bypass env) course-without-id)))

(defn course-id-get
  "Retrieves course via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id))
            (header :session-id session-id))))
  ([id]
   (course-id-get (:session-id-bypass env) id)))

(defn course-id-patch
  "Updates course via app's patch (id) request"
  ([session-id id new-course]
   (app (-> (request :patch (str "/api/course/" id))
            (json-body new-course)
            (header :session-id session-id))))
  ([id new-course]
   (course-id-patch (:session-id-bypass env) id new-course)))

(defn course-id-delete
  "Deletes course via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/course/" id))
            (header :session-id session-id))))
  ([id]
   (course-id-delete (:session-id-bypass env) id)))


(defn course-id-add-user
  "Connects user and course"
  ([session-id course-id user-id role]
   (app (-> (request :post (str "/api/course/" course-id "/add-user"))
            (header :session-id session-id)
            (json-body {:user-id user-id :account-role role}))))
  ([course-id user-id role]
   (course-id-add-user (:session-id-bypass env) course-id user-id role)))

(defn course-id-remove-user
  "Connects user and course"
  ([session-id course-id user-id]
   (app (-> (request :post (str "/api/course/" course-id "/remove-user"))
            (json-body {:user-id user-id})
            (header :session-id session-id))))
  ([course-id user-id]
   (course-id-remove-user (:session-id-bypass env) course-id user-id)))

(defn course-id-users
  "Reads all users connected to course"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id "/users"))
            (header :session-id session-id))))
  ([id]
   (course-id-users (:session-id-bypass env) id)))


(defn course-id-users
  "Reads all users connected to course"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id "/users"))
            (header :session-id session-id))))
  ([id]
   (course-id-users (:session-id-bypass env) id)))


(defn course-id-collections
  "Reads all collections connected to course"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (course-id-collections (:session-id-bypass env) id)))
