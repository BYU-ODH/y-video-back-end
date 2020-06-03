(ns y-video-back.utils.route_proxy
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))

(defn user-post
  "Create a user via app's post request"
  [user_without_id]
  (app (-> (request :post "/api/user")
           (json-body user_without_id))))

(defn user-id-get
  "Retrieves user via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/user/" id)))))

(defn user-id-patch
  "Updates user via app's patch (id) request"
  [id new_user]
  (app (-> (request :patch (str "/api/user/" id))
           (json-body new_user))))

(defn user-id-delete
  "Deletes user via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/user/" id)))))

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
  [collection user_id]
  (app (-> (request :post "/api/collection")
           (json-body {:collection collection :user_id user_id}))))

(defn collection-id-get
  "Retrieves collection via app's get (id) request"
  [id]
  (app (-> (request :get (str "/api/collection/" id)))))

(defn collection-id-patch
  "Updates collection via app's patch (id) request"
  [id new_collection]
  (app (-> (request :patch (str "/api/collection/" id))
           (json-body new_collection))))

(defn collection-id-delete
  "Deletes collection via app's delete (id) request"
  [id]
  (app (-> (request :delete (str "/api/collection/" id)))))

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

(defn collection-id-add-user
  "Connects user and collection"
  [collection-id user-id role]
  (app (-> (request :post (str "/api/collection/" collection-id "/add-user"))
           (json-body {:user-id user-id :account-role role}))))

(defn collection-id-remove-user
  "Connects user and collection"
  [collection-id user-id]
  (app (-> (request :post (str "/api/collection/" collection-id "/remove-user"))
           (json-body {:user_id user-id}))))

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
  [collection-id content-id]
  (app (-> (request :post (str "/api/collection/" collection-id "/add-content"))
           (json-body {:content-id content-id}))))

(defn collection-id-remove-content
  "Connects content and collection"
  [collection-id content-id]
  (app (-> (request :post (str "/api/collection/" collection-id "/remove-content"))
           (json-body {:content-id content-id}))))

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

(defn course-id-collections
  "Reads all collections connected to course"
  [id]
  (app (-> (request :get (str "/api/course/" id "/collections")))))
