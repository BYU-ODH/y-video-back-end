(ns y-video-back.db.files
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :files))
(def READ  (partial db/READ :files-undeleted))
(def READ-ALL  (partial db/READ :files))
(def UPDATE (partial db/UPDATE :files))
(def DELETE (partial db/mark-deleted :files))
(def CLONE (partial db/CLONE :files))
(def PERMANENT-DELETE (partial db/DELETE :files))