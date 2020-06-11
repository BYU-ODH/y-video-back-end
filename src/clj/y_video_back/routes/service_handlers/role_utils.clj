(ns y-video-back.routes.service_handlers.role_utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.routes.service_handlers.utils :as utils]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]))

(defn token-to-user-id
  "Returns userID associated with token. Returns false if token invalid."
  [token]
  ; DEVELOPMENT ONLY - token is actually the userID, so just return it
  token)

(defn get-user-type
  "Returns user type from DB"
  [user-id]
  (let [account-type (db/READ :users user-id [:account-type])]
    account-type))

(defn get-user-role-coll
  "Returns user role for collection from DB"
  [user-id collection-id]
  (let [user-role (db/read-where-and :user_collections_assoc
                                     [:user-id :collection-id]
                                     [user-id collection-id]
                                     [:account-role])]
    (if-not (empty? user-role)
      (:account-role (first user-role))
      ##Inf)))

(defn user-crse-coll
  "Returns true if user connected to collection via course"
  [user-id collection-id]
  (let [user-colls (users/READ-COLLECTIONS-BY-USER-VIA-COURSES user-id)]
    (contains? (set (map #(:id %) user-colls)) collection-id)))


(defn has-permission
  "Returns true if user has permission for route, else false"
  [token route args]
  (if (= token (utils/to-uuid "6bc824a6-f446-416d-8dd6-06350ae577f4")) ; Add if test before deployment
    true
    (let [user-id (token-to-user-id token)
          user-type (get-user-type user-id)]
      (case route

        ; Misc handlers
        "echo-post" (<= user-type 2)
        "echo-patch" (or (<= user-type 0) false)
        "connect-collection-and-course" (or (<= user-type 0) false)
        "search-by-term" (or (<= user-type 0) false)

        ; User handlers
        "user-create" true ; For development only
        "user-get-by-id" (or (<= user-type 0) false)
        "user-update" (or (<= user-type 0) false)
        "user-delete" (or (<= user-type 0) false)
        "user-get-logged-in" (or (<= user-type 0) false)
        "user-get-all-collections" (or (<= user-type 0) false)
        "user-get-all-courses" (or (<= user-type 0) false)
        "user-get-all-words" (or (<= user-type 0) false)

        ; Collection handlers
        "collection-create" (or (<= user-type 2))
        "collection-get-by-id" (or (<= user-type 2)
                                   (<= (get-user-role-coll user-id (:collection-id args)) 2)
                                   (user-crse-coll user-id (:collection-id args)))
        "collection-update" (or (<= user-type 1)
                                (<= (get-user-role-coll user-id (:collection-id args)) 1))
        "collection-delete" (or (<= user-type 0))
        "collection-add-user" (or (<= user-type 1)
                                  (<= (get-user-role-coll user-id (:collection-id args)) 0))
        "collection-remove-user" (or (<= user-type 1)
                                     (<= (get-user-role-coll user-id (:collection-id args)) 0))
        "collection-add-content" (or (<= user-type 1)
                                     (<= (get-user-role-coll user-id (:collection-id args)) 0))
        "collection-remove-content" (or (<= user-type 1)
                                        (<= (get-user-role-coll user-id (:collection-id args)) 0))
        "collection-add-course" (or (<= user-type 0) false)
        "collection-remove-course" (or (<= user-type 0) false)
        "collection-get-all-contents" (or (<= user-type 1))

        "collection-get-all-courses" (or (<= user-type 0) false)
        "collection-get-all-users" (or (<= user-type 0) false)

        ; Content handlers
        "content-create" (or (<= user-type 0) false)
        "content-get-by-id" (or (<= user-type 0) false)
        "content-update" (or (<= user-type 0) false)
        "content-delete" (or (<= user-type 0) false)
        "content-get-all-collections" (or (<= user-type 0) false)
        "content-get-all-files" (or (<= user-type 0) false)
        "content-add-view" (or (<= user-type 0) false)
        "content-add-file" (or (<= user-type 0) false)
        "content-remove-file" (or (<= user-type 0) false)

        ; File handlers
        "file-create" (or (<= user-type 0) false)
        "file-get-by-id" (or (<= user-type 0) false)
        "file-update" (or (<= user-type 0) false)
        "file-delete" (or (<= user-type 0) false)
        "file-get-all-contents" (or (<= user-type 0) false)

        ; Course handlers
        "course-create" (or (<= user-type 0) false)
        "course-get-by-id" (or (<= user-type 0) false)
        "course-update" (or (<= user-type 0) false)
        "course-delete" (or (<= user-type 0) false)
        "course-add-collection" (or (<= user-type 0) false)
        "course-remove-collection" (or (<= user-type 0) false)
        "course-get-all-collections" (or (<= user-type 0) false)
        "course-add-user" (or (<= user-type 0) false)
        "course-remove-user" (or (<= user-type 0) false)
        "course-get-all-users" (or (<= user-type 0) false)

        ; Word handlers
        "word-create" (or (<= user-type 0) false)
        "word-get-by-id" (or (<= user-type 0) false)
        "word-update" (or (<= user-type 0) false)
        "word-delete" (or (<= user-type 0) false)

        ; Annotation handlers
        "annotation-create" (or (<= user-type 0) false)
        "annotation-get-by-id" (or (<= user-type 0) false)
        "annotation-update" (or (<= user-type 0) false)
        "annotation-delete" (or (<= user-type 0) false)


        false))))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
