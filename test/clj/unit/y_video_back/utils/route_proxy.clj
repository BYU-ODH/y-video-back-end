(ns y-video-back.utils.route_proxy
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
  ([session-id user_without_id]
   (app (-> (request :post "/api/user")
            (header :session-id session-id)
            (json-body user_without_id))))
  ([user_without_id]
   (user-post SESSION-ID-BYPASS user_without_id)))

(defn user-id-get
  "Retrieves user via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/user/" id))
            (header :session-id session-id))))
  ([id]
   (user-id-get SESSION-ID-BYPASS id)))

(defn user-id-patch
  "Updates user via app's patch (id) request"
  ([session-id id new_user]
   (app (-> (request :patch (str "/api/user/" id))
            (header :session-id session-id)
            (json-body new_user))))
  ([id new_user]
   (user-id-patch SESSION-ID-BYPASS id new_user)))

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
  ([session-id word_without_id]
   (app (-> (request :post "/api/word")
            (header :session-id session-id)
            (json-body word_without_id))))
  ([word_without_id]
   (word-post SESSION-ID-BYPASS word_without_id)))

(defn word-id-get
  "Retrieves word via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-get SESSION-ID-BYPASS id)))

(defn word-id-patch
  "Updates word via app's patch (id) request"
  ([session-id id new_word]
   (app (-> (request :patch (str "/api/word/" id))
            (json-body new_word)
            (header :session-id session-id))))
  ([id new_word]
   (word-id-patch SESSION-ID-BYPASS id new_word)))

(defn word-id-delete
  "Deletes word via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/word/" id))
            (header :session-id session-id))))
  ([id]
   (word-id-delete SESSION-ID-BYPASS id)))


(defn collection-post
  "Create a collection via app's post request"
  ([session-id collection user_id]
   (app (-> (request :post "/api/collection")
            (header :session-id session-id)
            (json-body {:collection collection :user_id user_id}))))
  ([collection user_id]
   (collection-post SESSION-ID-BYPASS collection user_id)))

(defn collection-id-get
  "Retrieves collection via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/collection/" id))
            (header :session-id session-id))))
  ([id]
   (collection-id-get SESSION-ID-BYPASS id)))

(defn collection-id-patch
  "Updates collection via app's patch (id) request"
  ([session-id id new_collection]
   (app (-> (request :patch (str "/api/collection/" id))
            (header :session-id session-id)
            (json-body new_collection))))
  ([id new_collection]
   (collection-id-patch SESSION-ID-BYPASS id new_collection)))

(defn collection-id-delete
  "Deletes collection via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/collection/" id))
            (header :session-id session-id))))
  ([id]
   (collection-id-delete SESSION-ID-BYPASS id)))

(defn content-post
  "Create a content via app's post request"
  ([session-id content_without_id]
   (app (-> (request :post "/api/content")
            (json-body content_without_id)
            (header :session-id session-id))))
  ([content_without_id]
   (content-post SESSION-ID-BYPASS content_without_id)))

(defn content-id-get
  "Retrieves content via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-get SESSION-ID-BYPASS id)))

(defn content-id-patch
  "Updates content via app's patch (id) request"
  ([session-id id new_content]
   (app (-> (request :patch (str "/api/content/" id))
            (json-body new_content)
            (header :session-id session-id))))
  ([id new_content]
   (content-id-patch SESSION-ID-BYPASS id new_content)))

(defn content-id-delete
  "Deletes content via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-delete SESSION-ID-BYPASS id)))

(defn annotation-post
  "Create a annotation via app's post request"
  ([session-id annotation_without_id]
   (app (-> (request :post "/api/annotation")
            (json-body annotation_without_id)
            (header :session-id session-id))))
  ([annotation_without_id]
   (annotation-post SESSION-ID-BYPASS annotation_without_id)))

(defn annotation-id-get
  "Retrieves annotation via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/annotation/" id))
            (header :session-id session-id))))
  ([id]
   (annotation-id-get SESSION-ID-BYPASS id)))

(defn annotation-id-patch
  "Updates annotation via app's patch (id) request"
  ([session-id id new_annotation]
   (app (-> (request :patch (str "/api/annotation/" id))
            (json-body new_annotation)
            (header :session-id session-id))))
  ([id new_annotation]
   (annotation-id-patch SESSION-ID-BYPASS id new_annotation)))

(defn annotation-id-delete
  "Deletes annotation via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/annotation/" id))
            (header :session-id session-id))))
  ([id]
   (annotation-id-delete SESSION-ID-BYPASS id)))



(defn course-post
  "Create a course via app's post request"
  ([session-id course_without_id]
   (app (-> (request :post "/api/course")
            (json-body course_without_id)
            (header :session-id session-id))))
  ([course_without_id]
   (course-post SESSION-ID-BYPASS course_without_id)))

(defn course-id-get
  "Retrieves course via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/course/" id))
            (header :session-id session-id))))
  ([id]
   (course-id-get SESSION-ID-BYPASS id)))

(defn course-id-patch
  "Updates course via app's patch (id) request"
  ([session-id id new_course]
   (app (-> (request :patch (str "/api/course/" id))
            (json-body new_course)
            (header :session-id session-id))))
  ([id new_course]
   (course-id-patch SESSION-ID-BYPASS id new_course)))

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
            (json-body {:user_id user-id}))))
  ([collection-id user-id]
   (collection-id-remove-user SESSION-ID-BYPASS collection-id user-id)))

(defn file-post
  "Create a file via app's post request"
  ([session-id file_without_id]
   (app (-> (request :post "/api/file")
            (json-body file_without_id)
            (header :session-id session-id))))
  ([file_without_id]
   (file-post SESSION-ID-BYPASS file_without_id)))

(defn file-id-get
  "Retrieves file via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/file/" id))
            (header :session-id session-id))))
  ([id]
   (file-id-get SESSION-ID-BYPASS id)))

(defn file-id-patch
  "Updates file via app's patch (id) request"
  ([session-id id new_file]
   (app (-> (request :patch (str "/api/file/" id))
            (json-body new_file)
            (header :session-id session-id))))
  ([id new_file]
   (file-id-patch SESSION-ID-BYPASS id new_file)))

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
            (json-body {:user_id user-id})
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

(defn collection-id-add-content
  "Connects content and collection"
  ([session-id collection-id content-id]
   (app (-> (request :post (str "/api/collection/" collection-id "/add-content"))
            (header :session-id session-id)
            (json-body {:content-id content-id}))))
  ([collection-id content-id]
   (collection-id-add-content SESSION-ID-BYPASS collection-id content-id)))

(defn collection-id-remove-content
  "Connects content and collection"
  ([session-id collection-id content-id]
   (app (-> (request :post (str "/api/collection/" collection-id "/remove-content"))
            (header :session-id session-id)
            (json-body {:content-id content-id}))))
  ([collection-id content-id]
   (collection-id-remove-content SESSION-ID-BYPASS collection-id content-id)))

(defn collection-id-contents
  "Reads all contents connected to collection"
  ([session-id id]
   (app (-> (request :get (str "/api/collection/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (collection-id-contents SESSION-ID-BYPASS id)))

(defn content-id-collections
  "Reads all collections connected to content"
  ([session-id id]
   (app (-> (request :get (str "/api/content/" id "/collections"))
            (header :session-id session-id))))
  ([id]
   (content-id-collections SESSION-ID-BYPASS id)))

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

(defn content-id-add-file
  "Connects file and content"
  ([session-id content-id file-id]
   (app (-> (request :post (str "/api/content/" content-id "/add-file"))
            (json-body {:file-id file-id})
            (header :session-id session-id))))
  ([content-id file-id]
   (content-id-add-file SESSION-ID-BYPASS content-id file-id)))

(defn content-id-remove-file
  "Connects file and content"
  ([session-id content-id file-id]
   (app (-> (request :post (str "/api/content/" content-id "/remove-file"))
            (json-body {:file-id file-id})
            (header :session-id session-id))))
  ([content-id file-id]
   (content-id-remove-file SESSION-ID-BYPASS content-id file-id)))

(defn content-id-files
  "Reads all files connected to content"
  ([session-id id]
   (app (-> (request :get (str "/api/content/" id "/files"))
            (header :session-id session-id))))
  ([id]
   (content-id-files SESSION-ID-BYPASS id)))

(defn file-id-contents
  "Reads all contents connected to file"
  ([session-id id]
   (app (-> (request :get (str "/api/file/" id "/contents"))
            (header :session-id session-id))))
  ([id]
   (file-id-contents SESSION-ID-BYPASS id)))

(defn search
  "Searches by query_term"
  ([session-id query_term]
   (app (-> (request :get (str "/api/search?query_term=" (java.net.URLEncoder/encode query_term)))
            (header :session-id session-id))))
  ([query_term]
   (search SESSION-ID-BYPASS query_term)))
