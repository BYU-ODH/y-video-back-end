(ns y-video-back.db.courses
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :courses))
(def READ  (partial db/READ :courses-undeleted))
(def READ-ALL  (partial db/READ :courses))
(def UPDATE (partial db/UPDATE :courses))
(def DELETE (partial db/mark-deleted :courses))
(def CLONE (partial db/CLONE :courses))
(def PERMANENT-DELETE (partial db/DELETE :courses))
