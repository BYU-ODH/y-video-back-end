(ns y-video-back.db.auth-tokens
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :auth-tokens))
(def READ  (partial db/READ :auth-tokens-undeleted))
(def READ-ALL  (partial db/READ :auth-tokens))
(def UPDATE (partial db/UPDATE :auth-tokens))
(def DELETE (partial db/mark-deleted :auth-tokens))
(def CLONE (partial db/CLONE :auth-tokens))
(def PERMANENT-DELETE (partial db/DELETE :auth-tokens))
