(ns y-video-back.db.words
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :words))
(def READ  (partial db/READ :words-undeleted))
(def READ-ALL  (partial db/READ :words))
(def UPDATE (partial db/UPDATE :words))
(def DELETE (partial db/mark-deleted :words))
(def CLONE (partial db/CLONE :words))
(def PERMANENT-DELETE (partial db/DELETE :words))
