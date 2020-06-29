(ns y-video-back.routes.service-handlers.db-utils
  (:require
   ;[y-video-back.models :as models]
   ;[y-video-back.model-specs :as sp]
   ;[y-video-back.routes.service-handlers.utils :as utils]
   ;[y-video-back.routes.service-handlers.role-utils :as ru]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.users-by-collection :as users-by-collection]
   [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.resources :as resources]
   [y-video-back.db.courses :as courses]
   [y-video-back.db.files :as files]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.words :as words]))

(defn get-all-child-ids
  "Returns all ids of all objects reachable from user (downward tree only)"
  ([user-id]
   (get-all-child-ids user-id ##Inf))
  ([user-id role]
   '{}))
