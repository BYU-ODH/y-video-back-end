(ns y-video-back.db.TABLE
  (:require [y-video-back.db.core :as db]))

   (def CREATE (partial db/CREATE :TABLE))
   (def READ  (partial db/READ :TABLE_undeleted))
   (def READ-ALL  (partial db/READ :TABLE))
   (def UPDATE (partial db/UPDATE :TABLE))
   (def DELETE (partial db/DELETE :TABLE))
   (def CLONE (partial db/CLONE :TABLE))
   (def PERMANENT-DELETE (partial db/PERMANENT-DELETE :TABLE))
