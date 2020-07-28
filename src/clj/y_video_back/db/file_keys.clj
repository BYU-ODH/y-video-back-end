(ns y-video-back.db.file-keys
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :file-keys))
(def READ  (partial db/READ :file-keys-undeleted))
(def READ-ALL  (partial db/READ :file-keys))
(def UPDATE (partial db/UPDATE :file-keys))
(def DELETE (partial db/mark-deleted :file-keys))
(def CLONE (partial db/CLONE :file-keys))
(def PERMANENT-DELETE (partial db/DELETE :file-keys))
