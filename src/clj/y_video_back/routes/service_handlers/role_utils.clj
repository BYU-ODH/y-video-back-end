(ns y-video-back.routes.service-handlers.role-utils
  (:require [y-video-back.config :refer [env]]
            [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.routes.service-handlers.utils :as utils]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]
            [y-video-back.db.auth-tokens :as auth-tokens]
            [y-video-back.routes.service-handlers.db-utils :as dbu]))
            ;[y-video-back.config :refer [env]]))

; User account types
(def ADMIN 0) ; administrator
(def LA 1) ; lab assistant
(def INSTR 2) ; instructor
(def STUD 3) ; student

; User-Collection roles
(def CRSE-STUD 2) ; student enrolled in course
(def TA 1) ; student with TA privileges
(def OWNER 0) ; typically professor who own collection)

(defn token-to-user-id
  "Returns userID associated with token. Returns false if token invalid."
  [token]
  ; DEVELOPMENT ONLY - token is actually the userID, so just return it
  (let [res (auth-tokens/READ-UNEXPIRED token)]
    ;(println "token-to-user-id token=" token)
    ;(println "token-to-user-id res=" res)
    (:user-id res)))
  ;(:user-id (auth-tokens/READ token)))

(defn get-user-type
  "Returns user type from DB"
  [user-id]
  (let [account-type (db/READ :users user-id [:account-type])]
    account-type))

(defn get-user-role-coll
  "Returns user role for collection from DB"
  [user-id collection-id]
  (let [user-role (db/read-where-and :user-collections-assoc
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
  [user-type]
  (<= user-type ADMIN))

(defn la+
  "Returns true if user has at least lab assistant privileges"
  [user-type]
  (<= user-type LA))

(defn instr+
  "Returns true if user has at least instructor privileges"
  [user-type]
  (<= user-type INSTR))

(defn stud+
  "Returns true if user has at least student privileges"
  [user-type]
  (<= user-type STUD))


(defn is-child?
  "Returns true if target-id is child of user-id and user-id has at least
  'role' permissions for relevant collections. If 'role' is omitted, all
  children regardless of role are checked."
  ([target-id user-id role]
   (contains? (dbu/get-all-child-ids user-id role) target-id))
  ([target-id user-id]
   (is-child? target-id user-id ##Inf)))

(defn get-new-session-id
  "Generate new session-id, associated with same user as given session-id. Invalidate old session-id."
  [session-id]
  (let [new-session-id (:id (auth-tokens/CREATE {:user-id (:user-id (auth-tokens/READ-UNEXPIRED session-id))}))]
    ;(println "deleting auth-token: " session-id)
    (auth-tokens/DELETE session-id)
    new-session-id))

(defn has-permission
  "Placeholder for real has-permission function. Checks for session-id-bypass or (any) user-id."
  [token route args]
  ;(println "in has-permission")
  (if (= token (utils/to-uuid (:session-id-bypass env)))
    true
    (let [user-id (token-to-user-id token)]
      ;(println "user-id: " user-id)
      (not (nil? user-id)))))

(defn req-has-permission
  "Checks for session-id in request header, then calls has-permission"
  [uri headers body]
  ;(println "headers=" headers)
  (if (or (clojure.string/starts-with? uri "/api/get-session-id/")
          (clojure.string/starts-with? uri "/api/api-docs")
          (clojure.string/starts-with? uri "/api/swagger")
          (clojure.string/starts-with? uri "/api/video")
          (clojure.string/starts-with? uri "/api/get-video-url");temporary
          (clojure.string/starts-with? uri "/api/media");temporary
          (clojure.string/starts-with? uri "/api/upload");temporary
          (= uri "/api/ping"))
    true
    (if (not (contains? headers :session-id))
      false
      (has-permission (:session-id headers) uri body))))

(comment
  (defn has-permission
    "Returns true if user has permission for route, else false"
    [token route args]
    (if (= token (utils/to-uuid "")) ; Add if test before deployment
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
                                     (is-child? (:collection-id args) user-id CRSE-STUD))
          "collection-update" (or (la+ user-type)
                                  (is-child? (:collection-id args) user-id TA))
          "collection-delete" (or (admin+ user-type))
          "collection-add-user" (or (la+ user-type)
                                    (is-child? (:collection-id args) user-id OWNER))
          "collection-remove-user" (or (la+ user-type)
                                       (is-child? (:collection-id args) user-id OWNER))
          "collection-add-resource" (or (la+ user-type)
                                       (is-child? (:collection-id args) user-id OWNER))
          "collection-remove-resource" (or (la+ user-type)
                                          (is-child? (:collection-id args) user-id OWNER))
          "collection-add-course" (or (la+ user-type)
                                      (is-child? (:collection-id args) user-id OWNER))
          "collection-remove-course" (or (la+ user-type)
                                         (is-child? (:collection-id args) user-id OWNER))
          "collection-get-all-resources" (or (la+ user-type)
                                            (is-child? (:collection-id args) user-id CRSE-STUD))

          "collection-get-all-courses" (la+ user-type)
          "collection-get-all-users" (la+ user-type)

          ; Content handlers
          "resource-create" (or (la+ user-type))
          "resource-get-by-id" (or (instr+ user-type)
                                  (is-child? (:resource-id args) user-id CRSE-STUD))
          "resource-update" (or (la+ user-type)
                               (is-child? (:resource-id args) user-id TA))
          "resource-delete" (or (admin+ user-type))
          "resource-get-all-collections" (or (la+ user-type))
          "resource-get-all-files" (or (la+ user-type)
                                      (is-child? (:resource-id args) user-id CRSE-STUD))
          "resource-add-view" (or (la+ user-type)
                                  (is-child? (:resource-id args) user-id CRSE-STUD))
          "resource-add-file" (or (la+ user-type)
                                  (is-child? (:resource-id args) user-id TA))
          "resource-remove-file" (or (la+ user-type)
                                     (is-child? (:resource-id args) user-id TA))

          ; File handlers
          "file-create" (or (admin+ user-type) false)
          "file-get-by-id" (or (admin+ user-type) false)
          "file-update" (or (admin+ user-type) false)
          "file-delete" (or (admin+ user-type) false)
          "file-get-all-resources" (or (admin+ user-type) false)

          ; Course handlers
          "course-create" (or (la+ user-type) false)
          "course-get-by-id" (or (la+ user-type) false
                                 (is-child? (:course-id args) user-id CRSE-STUD))
          "course-update" (or (la+ user-type) false)
          "course-delete" (or (admin+ user-type) false)
          "course-add-collection" (or (la+ user-type) false
                                      (is-child? (:course-id args) user-id INSTR))
          "course-remove-collection" (or (la+ user-type) false
                                         (is-child? (:course-id args) user-id INSTR))
          "course-get-all-collections" (or (la+ user-type) false
                                           (is-child? (:course-id args) user-id CRSE-STUD))
          "course-add-user" (or (la+ user-type) false
                                (is-child? (:course-id args) user-id INSTR))
          "course-remove-user" (or (la+ user-type) false
                                   (is-child? (:course-id args) user-id INSTR))
          "course-get-all-users" (or (la+ user-type) false
                                     (is-child? (:course-id args) user-id INSTR))

          ; Word handlers
          "word-create" (or (admin+ user-type) false)
          "word-get-by-id" (or (admin+ user-type) false)
          "word-update" (or (admin+ user-type) false)
          "word-delete" (or (admin+ user-type) false)

          ; Annotation handlers
          "content-create" (or (admin+ user-type) false)
          "content-get-by-id" (or (admin+ user-type) false)
          "content-update" (or (admin+ user-type) false)
          "content-delete" (or (admin+ user-type) false)


          false)))))

(def forbidden-page
  (error-page {:status 401, :title "401 - Unauthorized",
               :image "https://www.cheatsheet.com/wp-content/uploads/2020/02/anakin_council_ROTS.jpg", :caption "It's unfair! How can you be on this website and not be an admin?!"}))
