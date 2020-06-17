(ns y-video-back.db.collections-contents-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :collection-contents-assoc))
(def READ  (partial db/READ :collection-contents-assoc-undeleted))
(def READ-ALL  (partial db/READ :collection-contents-assoc))
(def UPDATE (partial db/UPDATE :collection-contents-assoc))
(def DELETE (partial db/mark-deleted :collection-contents-assoc))
(def CLONE (partial db/CLONE :collection-contents-assoc))
(def PERMANENT-DELETE (partial db/DELETE :collection-contents-assoc))
(def READ-BY-IDS "[column-vals & select-field-keys]\ncolumn-vals must be a collection containing collection-id then content-id. select-field-keys, if given, must be a collection containing keywords representing columns to return from db." (partial db/read-where-and :collection-contents-assoc-undeleted [:collection-id :content-id]))
(def DELETE-BY-IDS "[column-vals]\ncolumn-vals must be a collection containing collection-id then content-id." (partial db/delete-where-and :collection-contents-assoc-undeleted [:collection-id :content-id]))
(def READ-CONTENTS-BY-COLLECTION (partial db/read-all-where :contents-by-collection :collection-id))
(def READ-COLLECTIONS-BY-CONTENT (partial db/read-all-where :collections-by-content :content-id))
