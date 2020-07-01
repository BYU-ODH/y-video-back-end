(ns y-video-back.utils.route-proxy
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [y-video-back.utils.route-proxy-parts.rp-admin :as admin]
    [y-video-back.utils.route-proxy-parts.rp-collection :as collection]
    [y-video-back.utils.route-proxy-parts.rp-content :as content]
    [y-video-back.utils.route-proxy-parts.rp-course :as course]
    [y-video-back.utils.route-proxy-parts.rp-file :as file]
    [y-video-back.utils.route-proxy-parts.rp-resource :as resource]
    [y-video-back.utils.route-proxy-parts.rp-user :as user]
    [y-video-back.utils.route-proxy-parts.rp-word :as word]))

(defn echo-post
  "Echo word from request body"
  ([session-id word]
   (app (-> (request :post (str "/api/echo"))
            (header :session-id session-id)
            (json-body {:echo word}))))
  ([word]
   (echo-post (:session-id-bypass env) word)))

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

; content routes
(def content-id-add-view content/content-id-add-view)
(def content-post content/content-post)
(def content-id-get content/content-id-get)
(def content-id-patch content/content-id-patch)
(def content-id-delete content/content-id-delete)

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

; admin routes
(def search admin/search)
(def search-by-user admin/search-by-user)
(def search-by-collection admin/search-by-collection)
(def search-by-resource admin/search-by-resource)
