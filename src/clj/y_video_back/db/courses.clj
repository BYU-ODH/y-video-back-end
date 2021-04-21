(ns y-video-back.db.courses
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :courses))
(def READ  (partial db/READ :courses-undeleted))
(def READ-ALL  (partial db/READ :courses))
(def UPDATE (partial db/UPDATE :courses))
(def DELETE (partial db/mark-deleted :courses))
(def CLONE (partial db/CLONE :courses))
(def PERMANENT-DELETE (partial db/DELETE :courses))
(defn EXISTS? [id] (not (nil? (db/READ :courses-undeleted id))))
(defn EXISTS-DEP-CAT-SEC? [department catalog-number section-number] (seq (db/read-where-and :courses-undeleted [:department :catalog-number :section-number] [department catalog-number section-number])))
(def READ-ALL-BY-DEP-CAT-SEC (partial db/read-where-and :courses-undeleted [:department :catalog-number :section-number]))
