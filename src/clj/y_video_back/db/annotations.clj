(ns y-video-back.db.annotations
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :annotations))
(def READ  (partial db/READ :annotations-undeleted))
(def READ-ALL  (partial db/READ :annotations))
(def UPDATE (partial db/UPDATE :annotations))
(def DELETE (partial db/mark-deleted :annotations))
(def CLONE (partial db/CLONE :annotations))
(def PERMANENT-DELETE (partial db/DELETE :annotations))
(def READ-BY-IDS (partial db/read-where-and :annotations [:collection-id :content-id]))
(def DELETE-BY-IDS "[column-vals]\ncolumn-vals must be a collection containing collection-id then content-id." (partial db/delete-where-and :annotations-undeleted [:collection-id :content-id]))
(def READ-CONTENTS-BY-COLLECTION (partial db/read-all-where :contents-by-collection :collection-id))
(def READ-COLLECTIONS-BY-CONTENT (partial db/read-all-where :collections-by-content :content-id))
