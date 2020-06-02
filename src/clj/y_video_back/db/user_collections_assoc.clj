(ns y-video-back.db.user-collections-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :user-collections-assoc))
(def READ  (partial db/READ :user-collections-assoc-undeleted))
(def READ-ALL  (partial db/READ :user-collections-assoc))
(def UPDATE (partial db/UPDATE :user-collections-assoc))
(def DELETE (partial db/mark-deleted :user-collections-assoc))
(def CLONE (partial db/CLONE :user-collections-assoc))
(def PERMANENT-DELETE (partial db/DELETE :user-collections-assoc))
(def READ-BY-COLLECTION (partial db/read-all-where :user-collections-assoc-undeleted :collection_id))
(def READ-BY-IDS (partial db/read-where-and :user-collections-assoc-undeleted [:collection-id :user-id]))
