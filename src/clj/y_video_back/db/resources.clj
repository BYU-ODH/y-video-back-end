(ns y-video-back.db.resources
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :resources))
(def READ  (partial db/READ :resources-undeleted))
(def READ-ALL  (partial db/READ :resources))
(def UPDATE (partial db/UPDATE :resources))
(def DELETE (partial db/mark-deleted :resources))
(def CLONE (partial db/CLONE :resources))
(def PERMANENT-DELETE (partial db/DELETE :resources))
(defn EXISTS? [id] (not (nil? (db/READ :resources-undeleted id))))
; (defn NAME-TAKEN? [resource-name] (seq (db/read-where-and :resources-undeleted [:resource-name] [resource-name])))
(def READ-ALL-BY-NAME (partial db/read-where-and :resources-undeleted [:resource-name]))
(def COLLECTIONS-BY-RESOURCE (partial db/read-all-where :collections-by-resource :resource-id))
(def FILES-BY-RESOURCE (partial db/read-all-where :files-undeleted :resource-id))
(def CONTENTS-BY-RESOURCE (partial db/read-all-where :contents-undeleted :resource-id))
(def INCR-VIEWS (partial db/increment-field :resources :views))
(def READ-SBTL-BY-RSRC (partial db/read-all-where :subtitles_by_resource :resource-id))
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
;   (db/read-all-where :resources-undeleted :public true))
