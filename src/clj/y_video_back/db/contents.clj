(ns y-video-back.db.contents
  (:require [y-video-back.db.core :as db]
            [y-video-back.db.collections :as collections]
            [y-video-back.db.users :as users]
            [y-video-back.db.resource-access :as resource-access]))

(def CREATE (partial db/CREATE :contents))
(def READ  (partial db/READ :contents-undeleted))
(def READ-ALL  (partial db/READ :contents))
(def UPDATE (partial db/UPDATE :contents))
(def DELETE (partial db/mark-deleted :contents))
(def CLONE (partial db/CLONE :contents))
(def PERMANENT-DELETE (partial db/DELETE :contents))
; (defn EXISTS-COLL-CONT? [collection-id resource-id] (seq (db/read-where-and :contents-undeleted [:collection-id :resource-id] [collection-id resource-id])))
(defn EXISTS? [id] (not (nil? (db/READ :contents-undeleted id))))
(def READ-BY-COLLECTION (partial db/read-all-where :contents-undeleted :collection-id))
(defn READ-BY-COLLECTION-WITH-LAST-VERIFIED
  [id]
  (let [coll (collections/READ id)
        owner (users/READ (:owner coll))
        res-without-ver (db/read-all-where :contents-undeleted :collection-id id)
        res-with-ver (map (fn [arg]
                            (let [res-acc (resource-access/READ-BY-USERNAME-RESOURCE (:username owner) (:resource-id arg))]
                              (assoc arg :last-verified (:last-verified res-acc))))
                          res-without-ver)]
    (doall res-with-ver)))
(def INCR-VIEWS (partial db/increment-field :contents :views))
; (defn ELIGIBLE-CONT-SUB? [content-id subtitle-id] (seq (db/read-where-and :cont_res_sub [:content-id :subtitle-id] [content-id subtitle-id])))
; (defn READ-PUBLIC
;   "Read by id, restrict to public results only"
;   [id]
;   (let [res (READ id)]
;     (if-not (:public res)
;       nil
;       res)))
; (defn READ-ALL-PUBLIC
;   "Read all public results"
;   []
;   (db/read-all-where :contents-undeleted :public true))
