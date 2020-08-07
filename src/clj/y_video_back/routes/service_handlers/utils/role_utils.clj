(ns y-video-back.routes.service-handlers.utils.role-utils
  (:require [y-video-back.config :refer [env]]
            [y-video-back.layout :refer [error-page]]
            [y-video-back.db.core :as db]
            [y-video-back.routes.service-handlers.utils.utils :as utils]
            [y-video-back.db.user-courses-assoc :as user-courses-assoc]
            [y-video-back.db.users :as users]
            [y-video-back.db.auth-tokens :as auth-tokens]
            [y-video-back.routes.service-handlers.utils.db-utils :as dbu]))
            ;[y-video-back.config :refer [env]]))

(defn bypass-uri
  [uri]
  (or (clojure.string/starts-with? uri "/api/get-session-id/")
      (clojure.string/starts-with? uri "/api/docs")
      (clojure.string/starts-with? uri "/api/swagger")
      (clojure.string/starts-with? uri "/api/video")
      (clojure.string/starts-with? uri "/api/get-video-url");temporary
      (clojure.string/starts-with? uri "/api/media/stream-media/");temporary
      (clojure.string/starts-with? uri "/api/upload");temporary))
      (clojure.string/starts-with? uri "/api/ping")));temporary))

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
  (<= user-type (:admin env)))

(defn la+
  "Returns true if user has at least lab assistant privileges"
  [user-type]
  (<= user-type (:lab-assistant env)))

(defn instr+
  "Returns true if user has at least instructor privileges"
  [user-type]
  (<= user-type (:instructor env)))

(defn stud+
  "Returns true if user has at least student privileges"
  [user-type]
  (<= user-type (:student env)))


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

(defn has-permission-free-for-all
  "Placeholder for real has-permission function. Checks for session-id-bypass or (any) user-id."
  [token route args]
  ;(println "in has-permission")
  (if (= token (utils/to-uuid (:session-id-bypass env)))
    true
    (let [user-id (token-to-user-id token)]
      ;(println "user-id: " user-id)
      (not (nil? user-id)))))

(defn has-permission
  "Checks if user has permission to access route."
  [token uri req-method args]
  (if (= token (utils/to-uuid (:session-id-bypass env)))
    true
    (let [user-id (token-to-user-id token)
          user-type (get-user-type user-id)
          route (str req-method ": " uri)]
      (if (nil? user-id)
        false
        (case route

          ; Misc routes
          "get: /api/auth-ping" (admin+ user-type)
          "post: /api/surely-a-get-method" (admin+ user-type)
          "get: /api/jedi-council" false
          "get: /api/echo" (admin+ user-type)
          "post: /api/echo" (admin+ user-type)
          "patch: /api/echo" (admin+ user-type)
          "get: /api/echo/{word}" (admin+ user-type)

          ; User routes
          "get: /api/user" (or (stud+ user-type) false)
          "post: /api/user" (or (admin+ user-type) false)
          "get: /api/user/{id}" (or (instr+ user-type) false)
          "delete: /api/user/{id}" (or (admin+ user-type) false)
          "patch: /api/user/{id}" (or (admin+ user-type) false)
          "get: /api/user/{id}/collections" (or (la+ user-type) false)
          "get: /api/user/{id}/courses" (or (la+ user-type) false)
          "get: /api/user/{id}/words" (or (la+ user-type) false)

          ; Word routes
          "post: /api/word" (or (la+ user-type) false)
          "get: /api/word/{id}" (or (la+ user-type) false)
          "delete: /api/word/{id}" (or (la+ user-type) false)
          "patch: /api/word/{id}" (or (la+ user-type) false)

          ; Collection routes
          "get: /api/collections" (or (stud+ user-type) false)
          "post: /api/collection" (or (la+ user-type) false)
          "get: /api/collection/{id}" (or (la+ user-type) false)
          "delete: /api/collection/{id}" (or (admin+ user-type) false)
          "patch: /api/collection/{id}" (or (la+ user-type) false)
          "post: /api/collection/{id}/add-user" (or (la+ user-type) false)
          "post: /api/collection/{id}/remove-user" (or (la+ user-type) false)
          "post: /api/collection/{id}/add-course" (or (la+ user-type) false)
          "post: /api/collection/{id}/remove-course" (or (la+ user-type) false)
          "get: /api/collection/{id}/contents" (or (la+ user-type) false)
          "get: /api/collection/{id}/courses" (or (la+ user-type) false)
          "get: /api/collection/{id}/users" (or (la+ user-type) false)

          ; Course routes
          "post: /api/course" (or (instr+ user-type) false)
          "get: /api/course/{id}" (or (instr+ user-type) false)
          "delete: /api/course/{id}" (or (admin+ user-type) false)
          "patch: /api/course/{id}" (or (admin+ user-type) false)
          "get: /api/course/{id}/collections" (or (la+ user-type) false)
          "post: /api/course/{id}/add-user" (or (la+ user-type) false)
          "post: /api/course/{id}/remove-user" (or (la+ user-type) false)
          "get: /api/course/{id}/users" (or (la+ user-type) false)

          ; Resource routes
          "post: /api/resource" (or (la+ user-type) false)
          "get: /api/resource/{id}" (or (instr+ user-type) false)
          "delete: /api/resource/{id}" (or (admin+ user-type) false)
          "patch: /api/resource/{id}" (or (la+ user-type) false)
          "get: /api/resource/{id}/files" (or (instr+ user-type) false)
          "get: /api/resource/{id}/collections" (or (la+ user-type) false)
          "get: /api/resource/{id}/contents" (or (la+ user-type) false)

          ; Content routes
          "post: /api/content" (or (la+ user-type) false)
          "get: /api/content/{id}" (or (la+ user-type) false)
          "delete: /api/content/{id}" (or (admin+ user-type) false)
          "patch: /api/content/{id}" (or (la+ user-type) false)
          "post: /api/content/{id}/add-view" (or (la+ user-type) false)
          "post: /api/content/{id}/add-subtitle" (or (la+ user-type) false)
          "post: /api/content/{id}/remove-subtitle" (or (la+ user-type) false)

          ; Subtitile routes
          "post: /api/subtitle" (or (la+ user-type) false)
          "get: /api/subtitle/{id}" (or (la+ user-type) false)
          "delete: /api/subtitle/{id}" (or (admin+ user-type) false)
          "patch: /api/subtitle/{id}" (or (la+ user-type) false)

          ; File routes
          "post: /api/file" (or (la+ user-type) false)
          "get: /api/file/{id}" (or (instr+ user-type) false)
          "delete: /api/file/{id}" (or (admin+ user-type) false)
          "patch: /api/file/{id}" (or (la+ user-type) false)

          ; Admin routes
          "get: /api/admin/user/{term}" (or (instr+ user-type) false)
          "get: /api/admin/collection/{term}" (or (la+ user-type) false)
          "get: /api/admin/content/{term}" (or (la+ user-type) false)
          "get: /api/admin/resource/{term}" (or (instr+ user-type) false)

          ; Media routes
          "get: /api/media/get-file-key/{file-id}" (or (la+ user-type) false)


          true))))) ; will be false when all routes are done


(defn req-has-permission
  "Checks for session-id in request header, then calls has-permission"
  [uri req-method headers body]
  ;(println "headers=" headers)
  (if (or (clojure.string/starts-with? uri "/api/get-session-id/")
          (clojure.string/starts-with? uri "/api/docs")
          (clojure.string/starts-with? uri "/api/swagger")
          (clojure.string/starts-with? uri "/api/video")
          (clojure.string/starts-with? uri "/api/get-video-url");temporary
          (clojure.string/starts-with? uri "/api/media/stream-media/");temporary
          (clojure.string/starts-with? uri "/api/upload");temporary
          (= uri "/api/ping"))
    true
    (if (not (contains? headers :session-id))
      false
      (has-permission (:session-id headers) uri req-method body))))

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
