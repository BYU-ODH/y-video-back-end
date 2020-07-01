(ns y-video-back.utils.route-proxy
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))

(def SESSION-ID-BYPASS "6bc824a6-f446-416d-8dd6-06350ae577f4")

(defn echo-post
  "Echo word from request body"
  ([session-id word]
   (app (-> (request :post (str "/api/echo"))
            (header :session-id session-id)
            (json-body {:echo word}))))
  ([word]
   (echo-post SESSION-ID-BYPASS word)))

(defn user-post
  "Create a user via app's post request"
  ([session-id user-without-id]
   (app (-> (request :post "/api/user")
            (header :session-id session-id)
            (json-body user-without-id))))
  ([user-without-id]
   (user-post SESSION-ID-BYPASS user-without-id)))

(defn user-id-get
  "Retrieves user via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/user/" id))
            (header :session-id session-id))))
  ([id]
   (user-id-get SESSION-ID-BYPASS id)))

(defn user-id-patch
  "Updates user via app's patch (id) request"
  ([session-id id new-user]
   (app (-> (request :patch (str "/api/user/" id))
            (header :session-id session-id)
            (json-body new-user))))
  ([id new-user]
   (user-id-patch SESSION-ID-BYPASS id new-user)))

(defn user-id-delete
  "Deletes user via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/user/" id))
            (header :session-id session-id))))
  ([id]
   (user-id-delete SESSION-ID-BYPASS id)))

(defn user-id-get-words
  "Retrieves all words connected to user"
  ([session-id id]
   (app (-> (request :get (str "/api/user/" id "/words"))
            (header :session-id session-id))))
  ([id]
   (user-id-get-words SESSION-ID-BYPASS id)))

(defn word-post
  "Create a word via app's post request"
  ([session-id word-without-id]
   (app (-> (request :post "/api/word")
            (header :session-id session-id)
            (json-body word-without-id))))
  ([word-without-id]
   (word-post SESSION-ID-BYPASS word-without-id)))

(defn word-id-get
  "Retrieves word via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-get SESSION-ID-BYPASS id)))

(defn word-id-patch
  "Updates word via app's patch (id) request"
  ([session-id id new-word]
   (app (-> (request :patch (str "/api/word/" id))
            (json-body new-word)
            (header :session-id session-id))))
  ([id new-word]
   (word-id-patch SESSION-ID-BYPASS id new-word)))

(defn word-id-delete
  "Deletes word via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-delete SESSION-ID-BYPASS id)))


(defn collection-post
  "Create a collection via app's post request"
  ([session-id collection]
   (app (-> (request :post "/api/collection")
            (header :session-id session-id)
            (json-body collection))))
  ([collection]
   (collection-post SESSION-ID-BYPASS collection)))

(defn collection-id-get
  "Retrieves collection via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/collection/" id))
            (header :session-id session-id))))
  ([id]
   (collection-id-get SESSION-ID-BYPASS id)))

(defn collection-id-patch
  "Updates collection via app's patch (id) request"
  ([session-id id new-collection]
   (app (-> (request :patch (str "/api/collection/" id))
            (header :session-id session-id)
            (json-body new-collection))))
  ([id new-collection]
   (collection-id-patch SESSION-ID-BYPASS id new-collection)))

(defn collection-id-delete
  "Deletes collection via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/collection/" id))
            (header :session-id session-id))))
  ([id]
   (collection-id-delete SESSION-ID-BYPASS id)))

(defn resource-post
  "Create a resource via app's post request"
  ([session-id resource-without-id]
   (app (-> (request :post "/api/resource")
            (json-body resource-without-id)
            (header :session-id session-id))))
  ([resource-without-id]
   (resource-post SESSION-ID-BYPASS resource-without-id)))

(defn resource-id-get
  "Retrieves resource via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id))
            (header :session-id session-id))))
  ([id]
   (resource-id-get SESSION-ID-BYPASS id)))

(defn resource-id-patch
  "Updates resource via app's patch (id) request"
  ([session-id id new-resource]
   (app (-> (request :patch (str "/api/resource/" id))
            (json-body new-resource)
            (header :session-id session-id))))
  ([id new-resource]
   (resource-id-patch SESSION-ID-BYPASS id new-resource)))

(defn resource-id-delete
  "Deletes resource via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/resource/" id))
            (header :session-id session-id))))
  ([id]
   (resource-id-delete SESSION-ID-BYPASS id)))

(defn content-id-add-view
  "Adds a view to content"
  ([session-id id]
   (app (-> (request :post (str "/api/content/" id "/add-view"))
            (header :session-id session-id))))
  ([id]
   (content-id-add-view SESSION-ID-BYPASS id)))

(defn content-post
  "Create a content via app's post request"
  ([session-id content-without-id]
   (app (-> (request :post "/api/content")
            (json-body content-without-id)
            (header :session-id session-id))))
  ([content-without-id]
   (content-post SESSION-ID-BYPASS content-without-id)))

(defn content-id-get
  "Retrieves content via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-get SESSION-ID-BYPASS id)))

(defn content-id-patch
  "Updates content via app's patch (id) request"
  ([session-id id new-content]
   (app (-> (request :patch (str "/api/content/" id))
            (json-body new-content)
            (header :session-id session-id))))
  ([id new-content]
   (content-id-patch SESSION-ID-BYPASS id new-content)))

(defn content-id-delete
  "Deletes content via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-delete SESSION-ID-BYPASS id)))

(defn course-post
  "Create a course via app's post request"
  ([session-id course-without-id]
   (app (-> (request :post "/api/course")
            (json-body course-without-id)
            (header :session-id session-id))))
  ([course-without-id]
   (course-post SESSION-ID-BYPASS course-without-id)))

(defn course-id-get
  "Retrieves course via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id))
            (header :session-id session-id))))
  ([id]
   (course-id-get SESSION-ID-BYPASS id)))

(defn course-id-patch
  "Updates course via app's patch (id) request"
  ([session-id id new-course]
   (app (-> (request :patch (str "/api/course/" id))
            (json-body new-course)
            (header :session-id session-id))))
  ([id new-course]
   (course-id-patch SESSION-ID-BYPASS id new-course)))

(defn course-id-delete
  "Deletes course via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/course/" id))
            (header :session-id session-id))))
  ([id]
   (course-id-delete SESSION-ID-BYPASS id)))

(defn collection-id-add-user
  "Connects user and collection"
  ([session-id collection-id user-id role]
   (app (-> (request :post (str "/api/collection/" collection-id "/add-user"))
            (header :session-id session-id)
            (json-body {:user-id user-id :account-role role}))))
  ([collection-id user-id role]
   (collection-id-add-user SESSION-ID-BYPASS collection-id user-id role)))

(defn collection-id-remove-user
  "Connects user and collection"
  ([session-id collection-id user-id]
   (app (-> (request :post (str "/api/collection/" collection-id "/remove-user"))
            (header :session-id session-id)
            (json-body {:user-id user-id}))))
  ([collection-id user-id]
   (collection-id-remove-user SESSION-ID-BYPASS collection-id user-id)))

(defn file-post
  "Create a file via app's post request"
  ([session-id file-without-id]
   (app (-> (request :post "/api/file")
            (json-body file-without-id)
            (header :session-id session-id))))
  ([file-without-id]
   (file-post SESSION-ID-BYPASS file-without-id)))

(defn file-id-get
  "Retrieves file via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/file/" id))
            (header :session-id session-id))))
  ([id]
   (file-id-get SESSION-ID-BYPASS id)))

(defn file-id-patch
  "Updates file via app's patch (id) request"
  ([session-id id new-file]
   (app (-> (request :patch (str "/api/file/" id))
            (json-body new-file)
            (header :session-id session-id))))
  ([id new-file]
   (file-id-patch SESSION-ID-BYPASS id new-file)))

(defn file-id-delete
  "Deletes file via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/file/" id))
            (header :session-id session-id))))
  ([id]
   (file-id-delete SESSION-ID-BYPASS id)))

(defn course-id-add-user
  "Connects user and course"
  ([session-id course-id user-id role]
   (app (-> (request :post (str "/api/course/" course-id "/add-user"))
            (header :session-id session-id)
            (json-body {:user-id user-id :account-role role}))))
  ([course-id user-id role]
   (course-id-add-user SESSION-ID-BYPASS course-id user-id role)))

(defn course-id-remove-user
  "Connects user and course"
  ([session-id course-id user-id]
   (app (-> (request :post (str "/api/course/" course-id "/remove-user"))
            (json-body {:user-id user-id})
            (header :session-id session-id))))
  ([course-id user-id]
   (course-id-remove-user SESSION-ID-BYPASS course-id user-id)))

(defn course-id-users
  "Reads all users connected to course"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id "/users"))
            (header :session-id session-id))))
  ([id]
   (course-id-users SESSION-ID-BYPASS id)))

(defn user-id-courses
  "Reads all courses connected to user"
  ([session-id id]
   (app (-> (request :get (str "/api/user/" id "/courses"))
            (header :session-id session-id))))
  ([id]
   (user-id-courses SESSION-ID-BYPASS id)))


(defn collection-id-users
  "Reads all users connected to collection"
  ([session-id id]
   (app (-> (request :get (str "/api/collection/" id "/users"))
            (header :session-id session-id))))
  ([id]
   (collection-id-users SESSION-ID-BYPASS id)))

(defn user-id-collections
  "Reads all collections connected to user"
  ([session-id id]
   (app (-> (request :get (str "/api/user/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (user-id-collections SESSION-ID-BYPASS id)))

;(defn collection-id-add-resource ; fix this name later
;  "Connects content and collection"
;  ([session-id collection-id content-id]
;   (app (-> (request :post (str "/api/collection/" collection-id "/add-content"))
;            (header :session-id session-id)
;            (json-body {:content-id content-id})))
;  ([collection-id content-id]
;   (collection-id-add-resource SESSION-ID-BYPASS collection-id content-id)))

;(defn collection-id-remove-resource
;  "Connects resource and collection"
;  ([session-id collection-id resource-id]
;   (app (-> (request :post (str "/api/collection/" collection-id "/remove-resource"))
;            (header :session-id session-id)
;            (json-body {:resource-id resource-id})))
;  ([collection-id resource-id]
;   (collection-id-remove-resource SESSION-ID-BYPASS collection-id resource-id)))

;(defn collection-id-contents
;  "Reads all resources connected to collection"
;  ([session-id id]
;   (app (-> (request :get (str "/api/collection/" id "/contents"))
;            (header :session-id session-id)))
;  ([id]
;   (collection-id-contents SESSION-ID-BYPASS id)))

(defn resource-id-collections
  "Reads all collections connected to resource"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (resource-id-collections SESSION-ID-BYPASS id)))

(defn collection-id-add-course
  "Connects course and collection"
  ([session-id collection-id course-id]
   (app (-> (request :post (str "/api/collection/" collection-id "/add-course"))
            (json-body {:course-id course-id})
            (header :session-id session-id))))
  ([collection-id course-id]
   (collection-id-add-course SESSION-ID-BYPASS collection-id course-id)))

(defn collection-id-remove-course
  "Connects course and collection"
  ([session-id collection-id course-id]
   (app (-> (request :post (str "/api/collection/" collection-id "/remove-course"))
            (json-body {:course-id course-id})
            (header :session-id session-id))))
  ([collection-id course-id]
   (collection-id-remove-course SESSION-ID-BYPASS collection-id course-id)))

(defn collection-id-courses
  "Reads all courses connected to collection"
  ([session-id id]
   (app (-> (request :get (str "/api/collection/" id "/courses"))
            (header :session-id session-id))))
  ([id]
   (collection-id-courses SESSION-ID-BYPASS id)))

(defn collection-id-contents
  "Reads all contents connected to collection"
  ([session-id id]
   (app (-> (request :get (str "/api/collection/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (collection-id-contents SESSION-ID-BYPASS id)))

(defn course-id-users
  "Reads all users connected to course"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id "/users"))
            (header :session-id session-id))))
  ([id]
   (course-id-users SESSION-ID-BYPASS id)))


(defn course-id-collections
  "Reads all collections connected to course"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (course-id-collections SESSION-ID-BYPASS id)))

(comment (defn resource-id-add-file)
  "Connects file and resource"
  ([session-id resource-id file-id]
   (app (-> (request :post (str "/api/resource/" resource-id "/add-file"))
            (json-body {:file-id file-id})
            (header :session-id session-id))))
  ([resource-id file-id]
   (resource-id-add-file SESSION-ID-BYPASS resource-id file-id)))

(comment (defn resource-id-remove-file)
  "Connects file and resource"
  ([session-id resource-id file-id]
   (app (-> (request :post (str "/api/resource/" resource-id "/remove-file"))
            (json-body {:file-id file-id})
            (header :session-id session-id))))
  ([resource-id file-id]
   (resource-id-remove-file SESSION-ID-BYPASS resource-id file-id)))

(defn resource-id-files
  "Reads all files connected to resource"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id "/files"))
            (header :session-id session-id))))
  ([id]
   (resource-id-files SESSION-ID-BYPASS id)))

(defn resource-id-contents
  "Reads all contents connected to resource"
  ([session-id id]
   (app (-> (request :get (str "/api/resource/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (resource-id-contents SESSION-ID-BYPASS id)))

;(defn file-id-resources
;  "Reads all resources connected to file"
;  ([session-id id]
;   (app (-> (request :get (str "/api/file/" id "/resources"))
;            (header :session-id session-id)))
;  ([id]
;   (file-id-resources SESSION-ID-BYPASS id)))

(defn search
  "Searches by query-term"
  ([session-id query-term]
   (app (-> (request :get (str "/api/search?query-term=" (java.net.URLEncoder/encode query-term)))
            (header :session-id session-id))))
  ([query-term]
   (search SESSION-ID-BYPASS query-term)))

(defn collections-by-logged-in
  "Retrieves all collections for current user (by session id)"
  [session-id]
  (app (-> (request :get (str "/api/collections"))
           (header :session-id session-id))))

(defn get-current-user
  "Retrieves current user (by session-id)"
  [session-id]
  (app (-> (request :get (str "/api/user"))
           (header :session-id session-id))))

(defn login-current-user
  "Retrieves current user (by session-id)"
  [username]
  (app (-> (request :get (str "/api/get-session-id/" username "/98bf2d2e-3d5d-4c4f-a656-9ac9c011b6b7")))))


(defn search-by-user
  "Search user table by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/user/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-user SESSION-ID-BYPASS term)))

(defn search-by-collection
  "Search collection table by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/collection/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-collection SESSION-ID-BYPASS term)))

(defn search-by-resource
  "Search resource table by term"
  ([session-id term]
   (app (-> (request :get (str "/api/admin/resource/" (java.net.URLEncoder/encode term)))
            (header :session-id session-id))))
  ([term]
   (search-by-resource SESSION-ID-BYPASS term)))
