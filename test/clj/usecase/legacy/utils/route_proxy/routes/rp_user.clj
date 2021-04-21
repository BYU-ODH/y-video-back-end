(ns legacy.utils.route-proxy.routes.rp-user
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

(defn user-post
  "Create a user via app's post request"
  ([session-id user-without-id]
   (ap2 (-> (request :post "/api/user")
            (header :session-id session-id)
            (json-body user-without-id))))
  ([user-without-id]
   (user-post (:session-id-bypass env) user-without-id)))

(defn user-id-get
  "Retrieves user via app's get (id) request"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/user/" id))
            (header :session-id session-id))))
  ([id]
   (user-id-get (:session-id-bypass env) id)))

(defn user-id-patch
  "Updates user via app's patch (id) request"
  ([session-id id new-user]
   (ap2 (-> (request :patch (str "/api/user/" id))
            (header :session-id session-id)
            (json-body new-user))))
  ([id new-user]
   (user-id-patch (:session-id-bypass env) id new-user)))

(defn user-id-delete
  "Deletes user via app's delete (id) request"
  ([session-id id]
   (ap2 (-> (request :delete (str "/api/user/" id))
            (header :session-id session-id))))
  ([id]
   (user-id-delete (:session-id-bypass env) id)))

(defn user-id-get-words
  "Retrieves all words connected to user"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/user/" id "/words"))
            (header :session-id session-id))))
  ([id]
   (user-id-get-words (:session-id-bypass env) id)))


(defn user-id-courses
  "Reads all courses connected to user"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/user/" id "/courses"))
            (header :session-id session-id))))
  ([id]
   (user-id-courses (:session-id-bypass env) id)))


(defn user-id-collections
  "Reads all collections connected to user"
  ([session-id id]
   (ap2 (-> (request :get (str "/api/user/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (user-id-collections (:session-id-bypass env) id)))

(defn get-current-user
  "Retrieves current user (by session-id)"
  [session-id]
  (ap2 (-> (request :get (str "/api/user"))
           (header :session-id session-id))))

(defn login-current-user
  "Retrieves current user (by session-id)"
  ([username password]
   (app (-> (request :get (str "/api/get-session-id/" username "/" password)))))
  ([username]
   (login-current-user username (:NEW-USER-PASSWORD env))))

