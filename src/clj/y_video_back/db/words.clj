(ns y-video-back.db.words
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :words))
(def READ  (partial db/READ :words-undeleted))
(def READ-ALL  (partial db/READ :words))
(def UPDATE (partial db/UPDATE :words))
(def DELETE (partial db/mark-deleted :words))
(def CLONE (partial db/CLONE :words))
(def PERMANENT-DELETE (partial db/DELETE :words))
(defn EXISTS? [id] (not (nil? (db/READ :words-undeleted id))))
(defn EXISTS-BY-FIELDS? [user-id word src-lang dest-lang] (seq (db/read-where-and :words-undeleted [:user-id :word :src-lang :dest-lang] [user-id word src-lang dest-lang])))
(def READ-ALL-BY-FIELDS (partial db/read-where-and :words-undeleted [:user-id :word :src-lang :dest-lang]))
