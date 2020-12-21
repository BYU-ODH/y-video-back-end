(ns y-video-back.db.email-logs
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :email-logs))
(def READ  (partial db/READ :email-logs-undeleted))
(def READ-ALL  (partial db/READ :email-logs))
(def UPDATE (partial db/UPDATE :email-logs))
(def DELETE (partial db/mark-deleted :email-logs))
(def CLONE (partial db/CLONE :email-logs))
(def PERMANENT-DELETE (partial db/DELETE :email-logs))
(defn EXISTS? [id] (not (nil? (db/READ :email-logs-undeleted id))))
