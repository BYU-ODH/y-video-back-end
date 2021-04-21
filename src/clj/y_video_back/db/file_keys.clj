(ns y-video-back.db.file-keys
  (:require [y-video-back.config :refer [env]]
            [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :file-keys))
(def READ  (partial db/READ :file-keys-undeleted))
(def READ-ALL  (partial db/READ :file-keys))
(def UPDATE (partial db/UPDATE :file-keys))
(def DELETE (partial db/mark-deleted :file-keys))
(def CLONE (partial db/CLONE :file-keys))
(def PERMANENT-DELETE (partial db/DELETE :file-keys))
(defn READ-UNEXPIRED
  "Reads unexpired file-keys. If expired, deletes and returns nil."
  [file-key-id]
  (let [file-key (READ file-key-id)]
    (if (nil? file-key)
      nil
      (if-not (< (inst-ms (:created file-key)) (- (System/currentTimeMillis) (-> env :FILES :timeout)))
        file-key
        (do (DELETE file-key-id)
            nil)))))
