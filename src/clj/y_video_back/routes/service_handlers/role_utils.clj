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


; Permission checks
(defn admin+
  "Returns true if user has at least admin privileges"
  [user-id]
  (<= user-id ADMIN))

(defn la+
  "Returns true if user has at least lab assistant privileges"
  [user-id]
  (<= user-id LA))

(defn instr+
  "Returns true if user has at least instructor privileges"
  [user-id]
  (<= user-id INSTR))

(defn stud+
  "Returns true if user has at least student privileges"
  [user-id]
  (<= user-id STUD))





(defn has-permission
  "Returns true if user has permission for route, else false"
  [token route args]
  (if (= token (utils/to-uuid "6bc824a6-f446-416d-8dd6-06350ae577f4")) ; Add if test before deployment
    true
    (let [user-id (token-to-user-id token)
          user-type (get-user-type user-id)]
      (case route

        ; Misc handlers
        "echo-post" (instr+ user-type)
        "echo-patch" (or (admin+ user-type) false)
        "connect-collection-and-course" (or (admin+ user-type) false)
        "search-by-term" (or (admin+ user-type) false)

        ; User handlers
        "user-create" true ; For development only
        "user-get-by-id" (or (admin+ user-type) false)
        "user-update" (or (admin+ user-type) false)
        "user-delete" (or (admin+ user-type) false)
        "user-get-logged-in" (or (admin+ user-type) false)
        "user-get-all-collections" (or (admin+ user-type) false)
        "user-get-all-courses" (or (admin+ user-type) false)
        "user-get-all-words" (or (admin+ user-type) false)

        ; Collection handlers
        "collection-create" (or (instr+ user-type))
        "collection-get-by-id" (or (instr+ user-type)
                                   (<= (get-user-role-coll user-id (:collection-id args)) CRSE-STUD)
                                   (user-crse-coll user-id (:collection-id args)))
        "collection-update" (or (la+ user-type)
                                (<= (get-user-role-coll user-id (:collection-id args)) TA))
        "collection-delete" (or (admin+ user-type))
        "collection-add-user" (or (la+ user-type)
                                  (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-remove-user" (or (la+ user-type)
                                     (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-add-content" (or (la+ user-type)
                                     (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-remove-content" (or (la+ user-type)
                                        (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-add-course" (or (la+ user-type)
                                    (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-remove-course" (or (la+ user-type)
                                       (<= (get-user-role-coll user-id (:collection-id args)) OWNER))
        "collection-get-all-contents" (or (la+ user-type)
                                          (<= (get-user-role-coll user-id (:collection-id args)) CRSE-STUD)
                                          (user-crse-coll user-id (:collection-id args)))

        "collection-get-all-courses" (la+ user-type)
        "collection-get-all-users" (la+ user-type)

        ; Content handlers
        "content-create" (or (admin+ user-type) false)
        "content-get-by-id" (or (admin+ user-type) false)
        "content-update" (or (admin+ user-type) false)
        "content-delete" (or (admin+ user-type) false)
        "content-get-all-collections" (or (admin+ user-type) false)
        "content-get-all-files" (or (admin+ user-type) false)
        "content-add-view" (or (admin+ user-type) false)
        "content-add-file" (or (admin+ user-type) false)
        "content-remove-file" (or (admin+ user-type) false)

        ; File handlers
        "file-create" (or (admin+ user-type) false)
        "file-get-by-id" (or (admin+ user-type) false)
        "file-update" (or (admin+ user-type) false)
        "file-delete" (or (admin+ user-type) false)
        "file-get-all-contents" (or (admin+ user-type) false)

        ; Course handlers
        "course-create" (or (admin+ user-type) false)
        "course-get-by-id" (or (admin+ user-type) false)
        "course-update" (or (admin+ user-type) false)
        "course-delete" (or (admin+ user-type) false)
        "course-add-collection" (or (admin+ user-type) false)
        "course-remove-collection" (or (admin+ user-type) false)
        "course-get-all-collections" (or (admin+ user-type) false)
        "course-add-user" (or (admin+ user-type) false)
        "course-remove-user" (or (admin+ user-type) false)
        "course-get-all-users" (or (admin+ user-type) false)

        ; Word handlers
        "word-create" (or (admin+ user-type) false)
        "word-get-by-id" (or (admin+ user-type) false)
        "word-update" (or (admin+ user-type) false)
        "word-delete" (or (admin+ user-type) false)

        ; Annotation handlers
        "annotation-create" (or (admin+ user-type) false)
        "annotation-get-by-id" (or (admin+ user-type) false)
        "annotation-update" (or (admin+ user-type) false)
        "annotation-delete" (or (admin+ user-type) false)


        false))))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "anakin_sitting.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
