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
  [id new_user]
  (app (-> (request :patch (str "/api/user/" id))
           (json-body new_user))))

(defn user-id-delete
  "Deletes user via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/user/" id)))))

(defn user-id-get-words
  "Retrieves all words connected to user"
  [id]
  (app (-> (request :get (str "/api/user/" id "/words")))))

(defn word-post
  "Create a word via app's post request"
  [word_without_id]
  (app (-> (request :post "/api/word")
           (json-body word_without_id))))

(defn word-id-get
  "Retrieves word via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/word/" id)))))

(defn word-id-patch
  "Updates word via app's patch (id) request"
  [id new_word]
  (app (-> (request :patch (str "/api/word/" id))
           (json-body new_word))))

(defn word-id-delete
  "Deletes word via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/word/" id)))))


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
  [content_without_id]
  (app (-> (request :post "/api/content")
           (json-body content_without_id))))

(defn content-id-get
  "Retrieves content via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/content/" id)))))

(defn content-id-patch
  "Updates content via app's patch (id) request"
  [id new_content]
  (app (-> (request :patch (str "/api/content/" id))
           (json-body new_content))))

(defn content-id-delete
  "Deletes content via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/content/" id)))))

(defn annotation-post
  "Create a annotation via app's post request"
  [annotation_without_id]
  (app (-> (request :post "/api/annotation")
           (json-body annotation_without_id))))

(defn annotation-id-get
  "Retrieves annotation via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/annotation/" id)))))

(defn annotation-id-patch
  "Updates annotation via app's patch (id) request"
  [id new_annotation]
  (app (-> (request :patch (str "/api/annotation/" id))
           (json-body new_annotation))))

(defn annotation-id-delete
  "Deletes annotation via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/annotation/" id)))))



(defn course-post
  "Create a course via app's post request"
  [course_without_id]
  (app (-> (request :post "/api/course")
           (json-body course_without_id))))

(defn course-id-get
  "Retrieves course via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/course/" id)))))

(defn course-id-patch
  "Updates course via app's patch (id) request"
  [id new_course]
  (app (-> (request :patch (str "/api/course/" id))
           (json-body new_course))))

(defn course-id-delete
  "Deletes course via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/course/" id)))))

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
  [file_without_id]
  (app (-> (request :post "/api/file")
           (json-body file_without_id))))

(defn file-id-get
  "Retrieves file via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/file/" id)))))

(defn file-id-patch
  "Updates file via app's patch (id) request"
  [id new_file]
  (app (-> (request :patch (str "/api/file/" id))
           (json-body new_file))))

(defn file-id-delete
  "Deletes file via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/file/" id)))))

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
  [course-id user-id]
  (app (-> (request :post (str "/api/course/" course-id "/remove-user"))
           (json-body {:user_id user-id}))))

(defn course-id-users
  "Reads all users connected to course"
  [id]
  (app (-> (request :get (str "/api/course/" id "/users")))))

(defn user-id-courses
  "Reads all courses connected to user"
  [id]
  (app (-> (request :get (str "/api/user/" id "/courses")))))


(defn collection-id-users
  "Reads all users connected to collection"
  [id]
  (app (-> (request :get (str "/api/collection/" id "/users")))))

(defn user-id-collections
  "Reads all collections connected to user"
  [id]
  (app (-> (request :get (str "/api/user/" id "/collections")))))

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
  [id]
  (app (-> (request :get (str "/api/collection/" id "/contents")))))

(defn content-id-collections
  "Reads all collections connected to content"
  [id]
  (app (-> (request :get (str "/api/content/" id "/collections")))))

(defn collection-id-add-course
  "Connects course and collection"
  [collection-id course-id]
  (app (-> (request :post (str "/api/collection/" collection-id "/add-course"))
           (json-body {:course-id course-id}))))

(defn collection-id-remove-course
  "Connects course and collection"
  [collection-id course-id]
  (app (-> (request :post (str "/api/collection/" collection-id "/remove-course"))
           (json-body {:course-id course-id}))))

(defn collection-id-courses
  "Reads all courses connected to collection"
  [id]
  (app (-> (request :get (str "/api/collection/" id "/courses")))))

(defn course-id-users
  "Reads all users connected to course"
  [id]
  (app (-> (request :get (str "/api/course/" id "/users")))))


(defn course-id-collections
  "Reads all collections connected to course"
  [id]
  (app (-> (request :get (str "/api/course/" id "/collections")))))

(defn content-id-add-file
  "Connects file and content"
  [content-id file-id]
  (app (-> (request :post (str "/api/content/" content-id "/add-file"))
           (json-body {:file-id file-id}))))

(defn content-id-remove-file
  "Connects file and content"
  [content-id file-id]
  (app (-> (request :post (str "/api/content/" content-id "/remove-file"))
           (json-body {:file-id file-id}))))

(defn content-id-files
  "Reads all files connected to content"
  [id]
  (app (-> (request :get (str "/api/content/" id "/files")))))

(defn file-id-contents
  "Reads all contents connected to file"
  [id]
  (app (-> (request :get (str "/api/file/" id "/contents")))))

(defn search
  "Searches by query_term"
  [query_term]
  (app (-> (request :get (str "/api/search?query_term=" (java.net.URLEncoder/encode query_term))))))
