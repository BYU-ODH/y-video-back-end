(ns y-video-back.routes.service_handlers.role_utils
  (:require [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.routes.service_handlers.utils :as utils]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]))

; User account types
(def ADMIN 0) ; administrator
(def LA 1) ; lab assistant
(def INSTR 2) ; instructor
(def STUD 3) ; student

; User-Collection roles
(def CRSE-STUD 2) ; student enrolled in course
(def TA 1) ; student with TA privileges
(def OWNER 0) ; typically professor who own collection

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
        "echo-post" (<= user-type INSTR)
        "echo-patch" (or (<= user-type ADMIN) false)
        "connect-collection-and-course" (or (<= user-type ADMIN) false)
        "search-by-term" (or (<= user-type ADMIN) false)

        ; User handlers
        "user-create" true ; For development only
        "user-get-by-id" (or (<= user-type ADMIN) false)
        "user-update" (or (<= user-type ADMIN) false)
        "user-delete" (or (<= user-type ADMIN) false)
        "user-get-logged-in" (or (<= user-type ADMIN) false)
        "user-get-all-collections" (or (<= user-type ADMIN) false)
        "user-get-all-courses" (or (<= user-type ADMIN) false)
        "user-get-all-words" (or (<= user-type ADMIN) false)

        ; Collection handlers
        "collection-create" (or (<= user-type INSTR))
        "collection-get-by-id" (or (<= user-type INSTR)
                                   (<= (get-user-role-coll user-id (:collection-id args)) CRSE-STUD)
                                   (user-crse-coll user-id (:collection-id args)))
        "collection-update" (or (<= user-type LA)
                                (<= (get-user-role-coll user-id (:collection-id args)) TA))
        "collection-delete" (or (<= user-type ADMIN))
        "collection-add-user" (or (<= user-type LA)
                                  (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-remove-user" (or (<= user-type LA)
                                     (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-add-content" (or (<= user-type LA)
                                     (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-remove-content" (or (<= user-type LA)
                                        (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-add-course" (or (<= user-type LA)
                                    (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-remove-course" (or (<= user-type LA)
                                       (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-get-all-contents" (or (<= user-type LA)
                                          (<= (get-user-role-coll user-id (:collection-id args)) CRSE-STUD)
                                          (user-crse-coll user-id (:collection-id args)))

        "collection-get-all-courses" (<= user-type LA)
        "collection-get-all-users" (<= user-type LA)

        ; Content handlers
        "content-create" (or (<= user-type ADMIN) false)
        "content-get-by-id" (or (<= user-type ADMIN) false)
        "content-update" (or (<= user-type ADMIN) false)
        "content-delete" (or (<= user-type ADMIN) false)
        "content-get-all-collections" (or (<= user-type ADMIN) false)
        "content-get-all-files" (or (<= user-type ADMIN) false)
        "content-add-view" (or (<= user-type ADMIN) false)
        "content-add-file" (or (<= user-type ADMIN) false)
        "content-remove-file" (or (<= user-type ADMIN) false)

        ; File handlers
        "file-create" (or (<= user-type ADMIN) false)
        "file-get-by-id" (or (<= user-type ADMIN) false)
        "file-update" (or (<= user-type ADMIN) false)
        "file-delete" (or (<= user-type ADMIN) false)
        "file-get-all-contents" (or (<= user-type ADMIN) false)

        ; Course handlers
        "course-create" (or (<= user-type ADMIN) false)
        "course-get-by-id" (or (<= user-type ADMIN) false)
        "course-update" (or (<= user-type ADMIN) false)
        "course-delete" (or (<= user-type ADMIN) false)
        "course-add-collection" (or (<= user-type ADMIN) false)
        "course-remove-collection" (or (<= user-type ADMIN) false)
        "course-get-all-collections" (or (<= user-type ADMIN) false)
        "course-add-user" (or (<= user-type ADMIN) false)
        "course-remove-user" (or (<= user-type ADMIN) false)
        "course-get-all-users" (or (<= user-type ADMIN) false)

        ; Word handlers
        "word-create" (or (<= user-type ADMIN) false)
        "word-get-by-id" (or (<= user-type ADMIN) false)
        "word-update" (or (<= user-type ADMIN) false)
        "word-delete" (or (<= user-type ADMIN) false)

        ; Annotation handlers
        "annotation-create" (or (<= user-type ADMIN) false)
        "annotation-get-by-id" (or (<= user-type ADMIN) false)
        "annotation-update" (or (<= user-type ADMIN) false)
        "annotation-delete" (or (<= user-type ADMIN) false)


        false))))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
