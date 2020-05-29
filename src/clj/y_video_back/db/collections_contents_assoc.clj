(ns y-video-back.db.collections-contents-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :collections-contents-assoc))
(def READ  (partial db/READ :collections-contents-assoc-undeleted))
(def READ-ALL  (partial db/READ :collections-contents-assoc))
(def UPDATE (partial db/UPDATE :collections-contents-assoc))
(def DELETE (partial db/mark-deleted :collections-contents-assoc))
(def CLONE (partial db/CLONE :collections-contents-assoc))
(def PERMANENT-DELETE (partial db/DELETE :collections-contents-assoc))
