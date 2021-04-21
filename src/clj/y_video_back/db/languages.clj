(ns y-video-back.db.languages
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :languages))
(def READ  (partial db/READ :languages-undeleted))
(def READ-ALL  (partial db/READ :languages))
(def UPDATE (partial db/UPDATE :languages))
(def DELETE (partial db/mark-deleted :languages))
(def CLONE (partial db/CLONE :languages))
(def PERMANENT-DELETE (partial db/DELETE :languages))
(defn EXISTS? [id] (not (nil? (db/READ :languages-undeleted id))))
(def GET-ALL (partial db/read-all :languages-undeleted))
