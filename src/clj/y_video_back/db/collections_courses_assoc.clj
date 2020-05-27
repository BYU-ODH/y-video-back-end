(ns y-video-back.db.collections-courses-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :collections-courses-assoc))
(def READ  (partial db/READ :collections-courses-assoc-undeleted))
(def READ-ALL  (partial db/READ :collections-courses-assoc))
(def UPDATE (partial db/UPDATE :collections-courses-assoc))
(def DELETE (partial db/mark-deleted :collections-courses-assoc))
(def CLONE (partial db/CLONE :collections-courses-assoc))
(def PERMANENT-DELETE (partial db/DELETE :collections-courses-assoc))
