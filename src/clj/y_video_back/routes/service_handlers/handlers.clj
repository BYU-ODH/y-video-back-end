(ns y-video-back.routes.service-handlers.handlers
  (:require
   [y-video-back.routes.service-handlers.misc-handlers :as miscs]
   [y-video-back.routes.service-handlers.user-handlers :as users]
   [y-video-back.routes.service-handlers.collection-handlers :as collections]
   [y-video-back.routes.service-handlers.content-handlers :as contents]
   [y-video-back.routes.service-handlers.course-handlers :as courses]
   [y-video-back.routes.service-handlers.file-handlers :as files]
   [y-video-back.routes.service-handlers.annotation-handlers :as annotations]
   [y-video-back.routes.service-handlers.word-handlers :as words]
   [y-video-back.routes.service-handlers.admin-handlers :as admin]))


; Misc handlers
(def echo-patch miscs/echo-patch)
(def connect-collection-and-course miscs/connect-collection-and-course)
(def search-by-term miscs/search-by-term)

; User handlers
(def user-create users/user-create)
(def user-get-by-id users/user-get-by-id)
(def user-update users/user-update)
(def user-delete users/user-delete)
(def user-get-logged-in users/user-get-logged-in)
(def user-get-all-collections users/user-get-all-collections)
(def user-get-all-collections-by-logged-in users/user-get-all-collections-by-logged-in)
(def user-get-all-courses users/user-get-all-courses)
(def user-get-all-words users/user-get-all-words)

; Collection handlers
(def collection-create collections/collection-create)
(def collection-get-by-id collections/collection-get-by-id)
(def collection-update collections/collection-update)
(def collection-delete collections/collection-delete)
(def collection-add-user collections/collection-add-user)
(def collection-remove-user collections/collection-remove-user)
(def collection-add-content collections/collection-add-content)
(def collection-remove-content collections/collection-remove-content)
(def collection-add-course collections/collection-add-course)
(def collection-remove-course collections/collection-remove-course)
(def collection-get-all-contents collections/collection-get-all-contents)
(def collection-get-all-courses collections/collection-get-all-courses)
(def collection-get-all-users collections/collection-get-all-users)

; Content handlers
(def content-create contents/content-create)
(def content-get-by-id contents/content-get-by-id)
(def content-update contents/content-update)
(def content-delete contents/content-delete)
(def content-get-all-collections contents/content-get-all-collections)
(def content-get-all-files contents/content-get-all-files)
(def content-add-view contents/content-add-view)
(def content-add-file contents/content-add-file)
(def content-remove-file contents/content-remove-file)

; File handlers
(def file-create files/file-create)
(def file-get-by-id files/file-get-by-id)
(def file-update files/file-update)
(def file-delete files/file-delete)
(def file-get-all-contents files/file-get-all-contents)

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

; Annotation handlers
(def annotation-create annotations/annotation-create)
(def annotation-get-by-id annotations/annotation-get-by-id)
(def annotation-update annotations/annotation-update)
(def annotation-delete annotations/annotation-delete)
(def annotation-get-by-collection-and-content annotations/annotation-get-by-collection-and-content)

; Admin handlers
(def search-by-user admin/search-by-user)
(def search-by-collection admin/search-by-collection)
(def search-by-content admin/search-by-content)
