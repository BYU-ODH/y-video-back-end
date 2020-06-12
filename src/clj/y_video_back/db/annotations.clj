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
