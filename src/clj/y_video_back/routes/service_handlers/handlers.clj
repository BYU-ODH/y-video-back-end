(ns y-video-back.routes.service-handlers.handlers
  (:require
   [y-video-back.routes.service-handlers.handlers.misc-handlers :as miscs]
   [y-video-back.routes.service-handlers.handlers.user-handlers :as users]
   [y-video-back.routes.service-handlers.handlers.collection-handlers :as collections]
   [y-video-back.routes.service-handlers.handlers.resource-handlers :as resources]
   [y-video-back.routes.service-handlers.handlers.course-handlers :as courses]
   [y-video-back.routes.service-handlers.handlers.file-handlers :as files]
   [y-video-back.routes.service-handlers.handlers.language-handlers :as languages]
   [y-video-back.routes.service-handlers.handlers.subtitle-handlers :as subtitles]
   [y-video-back.routes.service-handlers.handlers.content-handlers :as contents]
   [y-video-back.routes.service-handlers.handlers.word-handlers :as words]
   [y-video-back.routes.service-handlers.handlers.admin-handlers :as admin]
   [y-video-back.routes.service-handlers.handlers.media-handlers :as media]
   [y-video-back.routes.service-handlers.handlers.email-handlers :as email]))


; Misc handlers
(def echo-patch miscs/echo-patch)

; User handlers
(def user-create users/user-create)
(def user-create-from-byu users/user-create-from-byu)
(def user-get-by-id users/user-get-by-id)
(def user-update users/user-update)
(def user-delete users/user-delete)
(def user-delete-with-collections users/user-delete-with-collections)
(def user-get-logged-in users/user-get-logged-in)
(def user-get-all-collections users/user-get-all-collections)
(def user-get-all-collections-by-logged-in users/user-get-all-collections-by-logged-in)
(def user-get-all-courses users/user-get-all-courses)
(def user-get-all-words users/user-get-all-words)
(def refresh-courses users/refresh-courses)

; Collection handlers
(def collection-create collections/collection-create)
(def collection-get-by-id collections/collection-get-by-id)
(def collection-update collections/collection-update)
(def collection-delete collections/collection-delete)
(def collection-add-user collections/collection-add-user)
(def collection-add-users collections/collection-add-users)
(def collection-remove-user collections/collection-remove-user)
(def collection-add-course collections/collection-add-course)
(def collection-remove-course collections/collection-remove-course)
(def collection-get-all-contents collections/collection-get-all-contents)
(def collection-get-all-courses collections/collection-get-all-courses)
(def collection-get-all-users collections/collection-get-all-users)

; Resource handlers
(def resource-create resources/resource-create)
(def resource-get-by-id resources/resource-get-by-id)
(def resource-update resources/resource-update)
(def resource-delete resources/resource-delete)
(def resource-get-all-collections resources/resource-get-all-collections)
(def resource-get-all-contents resources/resource-get-all-contents)
(def resource-get-all-subtitles resources/resource-get-all-subtitles)
(def resource-get-all-files resources/resource-get-all-files)
(def resource-add-access resources/resource-add-access)
(def resource-remove-access resources/resource-remove-access)
(def resource-read-all-access resources/resource-read-all-access)

; Language handlers
(def language-create languages/language-create)
(def language-delete languages/language-delete)
(def language-get-all languages/language-get-all)

; Subtitle handlers
(def subtitle-create subtitles/subtitle-create)
(def subtitle-get-by-id subtitles/subtitle-get-by-id)
(def subtitle-update subtitles/subtitle-update)
(def subtitle-delete subtitles/subtitle-delete)

; File handlers
(def file-create files/file-create)
(def file-get-by-id files/file-get-by-id)
(def file-update files/file-update)
(def file-delete files/file-delete)

; Course handlers
(def course-create courses/course-create)
(def course-get-by-id courses/course-get-by-id)
(def course-update courses/course-update)
(def course-delete courses/course-delete)
(def course-get-all-collections courses/course-get-all-collections)
(def course-add-user courses/course-add-user)
(def course-remove-user courses/course-remove-user)
(def course-get-all-users courses/course-get-all-users)

; Word handlers
(def word-create words/word-create)
(def word-get-by-id words/word-get-by-id)
(def word-update words/word-update)
(def word-delete words/word-delete)

; Content handlers
(def content-create contents/content-create)
(def content-get-by-id contents/content-get-by-id)
(def content-update contents/content-update)
(def content-delete contents/content-delete)
(def content-add-view contents/content-add-view)
(def content-subtitles contents/content-subtitles)
(def content-clone-subtitle contents/content-clone-subtitle)

; Admin handlers
(def search-by-user admin/search-by-user)
(def search-by-collection admin/search-by-collection)
(def search-public-collections admin/search-public-collections)
(def search-by-content admin/search-by-content)
(def search-by-resource admin/search-by-resource)

; Media handlers
(def get-file-key media/get-file-key)
(def stream-media media/stream-media)
(def stream-partial-media media/stream-partial-media)

; Email handlers
(def send-email email/send-email)
(def send-email-with-attachment email/send-email-with-attachment)
