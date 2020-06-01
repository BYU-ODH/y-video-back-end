(ns y-video-back.db.collections
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :collections))
(def READ  (partial db/READ :collections-undeleted))
(def READ-ALL  (partial db/READ :collections))
(def UPDATE (partial db/UPDATE :collections))
(def DELETE (partial db/mark-deleted :collections))
(def CLONE (partial db/CLONE :collections))
(def PERMANENT-DELETE (partial db/DELETE :collections))