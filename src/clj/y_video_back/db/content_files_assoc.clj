(ns y-video-back.db.content-files-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :content-files-assoc))
(def READ  (partial db/READ :content-files-assoc-undeleted))
(def READ-ALL  (partial db/READ :content-files-assoc))
(def UPDATE (partial db/UPDATE :content-files-assoc))
(def DELETE (partial db/mark-deleted :content-files-assoc))
(def CLONE (partial db/CLONE :content-files-assoc))
(def PERMANENT-DELETE (partial db/DELETE :content-files-assoc))
