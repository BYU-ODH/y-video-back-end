(ns y-video-back.routes.service_handlers.db-utils
  (:require
   ;[y-video-back.models :as models]
   ;[y-video-back.model-specs :as sp]
   ;[y-video-back.routes.service_handlers.utils :as utils]
   ;[y-video-back.routes.service_handlers.role_utils :as ru]
   [y-video-back.db.annotations :as annotations]
   [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
   [y-video-back.db.users-by-collection :as users-by-collection]
   [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.content-files-assoc :as content_files_assoc]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.courses :as courses]
   [y-video-back.db.files :as files]
   [y-video-back.db.user-collections-assoc :as user_collections_assoc]
   [y-video-back.db.user-courses-assoc :as user_courses_assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.words :as words]))

(defn get-all-child-ids
  "Returns all ids of all objects reachable from user (downward tree only)"
  ([user-id]
   (get-all-child-ids user-id ##Inf))
  ([user-id role]
   (let [all-dir-collections (map
                               (fn [arg]
                                 (if (<= (:account-role arg) role)
                                   (:id arg)))
                               (user_collections_assoc/READ-COLLECTIONS-BY-USER user-id))]
     (let [all-courses (map
                         #(:id %)
                         (user_courses_assoc/READ-COURSES-BY-USER user-id))]
       (let [all-collections (if (= role 2)
                               (clojure.set/union
                                 all-dir-collections
                                 (set
                                   (clojure.core/flatten
                                     (map
                                       (fn [arg1]
                                         (map
                                            (fn [arg2]
                                                (:id arg2))
                                            (collection_courses_assoc/READ-COLLECTIONS-BY-COURSE arg1)))
                                       all-courses))))
                               all-dir-collections)]
         (let [all-annotations (clojure.core/flatten
                                 (map
                                   (fn [arg1]
                                     (map
                                       #(:id %)
                                       (collections/READ-ANNOTATIONS arg1)))
                                   all-collections))]
           (let [all-contents (clojure.core/flatten
                                (map
                                  (fn [arg1]
                                    (map
                                      #(:id %)
                                      (collection_contents_assoc/READ-CONTENTS-BY-COLLECTION arg1)))
                                  all-collections))]
             (let [all-files (clojure.core/flatten
                               (map
                                 (fn [arg1]
                                   (map
                                     #(:id %)
                                     (content_files_assoc/READ-FILES-BY-CONTENT arg1)))
                                 all-contents))]
               (let [all-words (map
                                 #(:id %)
                                 (users/READ-WORDS user-id))]
                 (-> (set [user-id])
                     (into all-collections)
                     (into all-annotations)
                     (into all-courses)
                     (into all-contents)
                     (into all-files)
                     (into all-words)))))))))))
