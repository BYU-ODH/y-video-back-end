(ns y-video-back.db.contents
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :contents))
(def READ  (partial db/READ :contents-undeleted))
(def READ-ALL  (partial db/READ :contents))
(def UPDATE (partial db/UPDATE :contents))
(def DELETE (partial db/mark-deleted :contents))
(def CLONE (partial db/CLONE :contents))
(def PERMANENT-DELETE (partial db/DELETE :contents))
(defn EXISTS? [id] (not (nil? (db/READ :contents-undeleted id))))
(defn NAME-TAKEN? [content-name] (not (empty? (db/read-where-and :contents-undeleted [:content-name] [content-name]))))
(def READ-ALL-BY-NAME (partial db/read-where-and :contents-undeleted [:content-name]))
