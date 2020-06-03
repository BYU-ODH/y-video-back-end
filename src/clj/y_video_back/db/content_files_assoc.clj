(ns y-video-back.db.content-files-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :content-files-assoc))
(def READ  (partial db/READ :content-files-assoc-undeleted))
(def READ-ALL  (partial db/READ :content-files-assoc))
(def UPDATE (partial db/UPDATE :content-files-assoc))
(def DELETE (partial db/mark-deleted :content-files-assoc))
(def CLONE (partial db/CLONE :content-files-assoc))
(def PERMANENT-DELETE (partial db/DELETE :content-files-assoc))
(def READ-BY-IDS "[column-vals & select-field-keys]\ncolumn-vals must be a content containing content-id then file-id. select-field-keys, if given, must be a content containing keywords representing columns to return from db." (partial db/read-where-and :content-files-assoc-undeleted [:content-id :file-id]))
(def DELETE-BY-IDS "[column-vals]\ncolumn-vals must be a content containing content-id then file-id." (partial db/delete-where-and :content-files-assoc-undeleted [:content-id :file-id]))
(def READ-CONTENTS-BY-COLLECTION (partial db/read-all-where :files-by-content :content_id))
(def READ-COLLECTIONS-BY-CONTENT (partial db/read-all-where :contents-by-file :file_id))
