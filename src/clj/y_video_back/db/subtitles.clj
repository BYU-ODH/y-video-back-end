(ns y-video-back.db.subtitles
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :subtitles))
(def READ  (partial db/READ :subtitles-undeleted))
(def READ-ALL  (partial db/READ :subtitles))
(def UPDATE (partial db/UPDATE :subtitles))
(def DELETE (partial db/mark-deleted :subtitles))
(def CLONE (partial db/CLONE :subtitles))
(def PERMANENT-DELETE (partial db/DELETE :subtitles))
(defn EXISTS? [id] (not (nil? (db/READ :subtitles-undeleted id))))
(def READ-BY-TITLE-RSRC (partial db/read-where-and :subtitles-undeleted [:title :resource-id]))
(def READ-BY-CONTENT-ID (partial db/read-all-where :subtitles-undeleted :content-id))
