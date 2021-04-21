(ns legacy.utils.route-proxy.proxy
  (:require
    [y-video-back.config :refer [env]]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [legacy.utils.route-proxy.routes.rp-home :as home]
    [legacy.utils.route-proxy.routes.rp-admin :as admin]
    [legacy.utils.route-proxy.routes.rp-collection :as collection]
    [legacy.utils.route-proxy.routes.rp-content :as content]
    [legacy.utils.route-proxy.routes.rp-course :as course]
    [legacy.utils.route-proxy.routes.rp-file :as file]
    [legacy.utils.route-proxy.routes.rp-media :as media]
    [legacy.utils.route-proxy.routes.rp-language :as language]
    [legacy.utils.route-proxy.routes.rp-subtitle :as subtitle]
    [legacy.utils.route-proxy.routes.rp-resource :as resource]
    [legacy.utils.route-proxy.routes.rp-user :as user]
    [legacy.utils.route-proxy.routes.rp-word :as word]
    [legacy.utils.route-proxy.routes.rp-public :as public]))

(defn echo-post
  "Echo word from request body"
  ([session-id word]
   (app (-> (request :post (str "/api/echo"))
            (header :session-id session-id)
            (json-body {:echo word}))))
  ([word]
   (echo-post (:session-id-bypass env) word)))

; home routes
(def home-page home/home-page)

; user routes
(def user-post user/user-post)
(def user-id-get user/user-id-get)
(def user-id-patch user/user-id-patch)
(def user-id-delete user/user-id-delete)
(def user-id-get-words user/user-id-get-words)
(def user-id-courses user/user-id-courses)
(def user-id-collections user/user-id-collections)
(def get-current-user user/get-current-user)
(def login-current-user user/login-current-user)

; word routes
(def word-post word/word-post)
(def word-id-get word/word-id-get)
(def word-id-patch word/word-id-patch)
(def word-id-delete word/word-id-delete)

; collection routes
(def collection-post collection/collection-post)
(def collection-id-get collection/collection-id-get)
(def collection-id-patch collection/collection-id-patch)
(def collection-id-delete collection/collection-id-delete)
(def collection-id-add-user collection/collection-id-add-user)
(def collection-id-add-users collection/collection-id-add-users)
(def collection-id-remove-user collection/collection-id-remove-user)
(def collection-id-users collection/collection-id-users)
(def collection-id-add-course collection/collection-id-add-course)
(def collection-id-remove-course collection/collection-id-remove-course)
(def collection-id-courses collection/collection-id-courses)
(def collection-id-contents collection/collection-id-contents)
(def collections-by-logged-in collection/collections-by-logged-in)

; resource routes
(def resource-post resource/resource-post)
(def resource-id-get resource/resource-id-get)
(def resource-id-patch resource/resource-id-patch)
(def resource-id-delete resource/resource-id-delete)
(def resource-id-collections resource/resource-id-collections)
(def resource-id-files resource/resource-id-files)
(def resource-id-contents resource/resource-id-contents)
(def resource-id-subtitles resource/resource-id-subtitles)
(def resource-add-access resource/resource-add-access)
(def resource-remove-access resource/resource-remove-access)
(def resource-read-all-access resource/resource-read-all-access)

; content routes
(def content-id-add-view content/content-id-add-view)
(def content-post content/content-post)
(def content-id-get content/content-id-get)
(def content-id-patch content/content-id-patch)
(def content-id-delete content/content-id-delete)
(def content-id-add-subtitle content/content-id-add-subtitle)
(def content-id-remove-subtitle content/content-id-remove-subtitle)
(def content-id-subtitles content/content-id-subtitles)
(def content-id-clone-subtitle content/content-id-clone-subtitle)

; language routes
(def language-post language/language-post)
(def language-id-delete language/language-id-delete)
(def language-get-all language/language-get-all)

; subtitle routes
(def subtitle-post subtitle/subtitle-post)
(def subtitle-id-get subtitle/subtitle-id-get)
(def subtitle-id-patch subtitle/subtitle-id-patch)
(def subtitle-id-delete subtitle/subtitle-id-delete)

; course routes
(def course-post course/course-post)
(def course-id-get course/course-id-get)
(def course-id-patch course/course-id-patch)
(def course-id-delete course/course-id-delete)
(def course-id-add-user course/course-id-add-user)
(def course-id-remove-user course/course-id-remove-user)
(def course-id-users course/course-id-users)
(def course-id-users course/course-id-users)
(def course-id-collections course/course-id-collections)

; file routes
(def file-post file/file-post)
(def file-id-get file/file-id-get)
(def file-id-patch file/file-id-patch)
(def file-id-delete file/file-id-delete)

; public routes
(def public-collection-id-get public/collection-id-get)
(def public-collection-get-all public/collection-get-all)
(def public-content-id-get public/content-id-get)
(def public-content-get-all public/content-get-all)
(def public-resource-id-get public/resource-id-get)
(def public-resource-get-all public/resource-get-all)

; media routes
(def get-file-key media/get-file-key)
(def stream-media media/stream-media)

; admin routes
(def search admin/search)
(def search-by-user admin/search-by-user)
(def search-by-collection admin/search-by-collection)
(def search-public-collections admin/search-public-collections)
(def search-by-resource admin/search-by-resource)
(def search-by-content admin/search-by-content)

(defn auth-ping
  "ping route, requires valid session-id (any permission level)"
  [session-id]
  (app (-> (request :get "/api/auth-ping")
           (header :session-id session-id))))

(defn refresh-courses
  "use api to refresh which courses user is enrolled in"
  [session-id]
  (app (-> (request :post "/api/refresh-courses")
           (header :session-id session-id))))

