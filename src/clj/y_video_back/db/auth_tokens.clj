(ns y-video-back.db.auth-tokens
  (:require [y-video-back.config :refer [env]]
            [y-video-back.db.core :as db]
            [taoensso.timbre :as log]))

(def CREATE (partial db/CREATE :auth-tokens))
(def READ  (partial db/READ :auth-tokens-undeleted))
(def READ-ALL  (partial db/READ :auth-tokens))
(def UPDATE (partial db/UPDATE :auth-tokens))
(def DELETE (partial db/mark-deleted :auth-tokens))
(def CLONE (partial db/CLONE :auth-tokens))
(def PERMANENT-DELETE (partial db/DELETE :auth-tokens))
(defn orig-READ-UNEXPIRED
  "Reads unexpired auth-tokens. If expired, deletes and returns nil."
  [auth-token-id]
  (log/debug "" {:auth-token-id (str auth-token-id)})
  (if (nil? auth-token-id)
    nil
    (let [auth-token (READ auth-token-id)]
      (log/debug "" {:auth-token-id (str auth-token-id)
                     :auth-token (str auth-token)})
      (if (nil? auth-token)
        nil
        (if-not (< (inst-ms (:created auth-token)) (- (System/currentTimeMillis) (-> env :auth :timeout)))
          auth-token
          (do (DELETE auth-token-id)
              nil))))))

(defn READ-UNEXPIRED
  "Reads unexpired auth-tokens. If expired, deletes and returns nil."
  [auth-token-id]
  (when auth-token-id    
    (when-let [auth-token (READ auth-token-id)]
       (if-not (< (inst-ms (:created auth-token)) (- (System/currentTimeMillis) (-> env :auth :timeout)))
         auth-token
         (do (DELETE auth-token-id)
             nil))))) ;; TODO resume refactoring carefully here

#_(defn READ-UNEXPIRED
  "Reads unexpired auth-tokens. If expired, deletes and returns nil."
  [auth-token-id]
  (when auth-token-id
    (let [auth-token (READ auth-token-id)
          now (System/currentTimeMillis)
          created (when auth-token (inst-ms (:created auth-token)))
          timeout (-> env :auth :timeout)
          expired? (< created (- now timeout))
          expired-delete-this! (fn [] (DELETE auth-token-id) nil)]
      (log/debug "" {:auth-token-id (str auth-token-id)
                     :auth-token (str auth-token)})
      (if (and auth-token
               (not expired?))
        auth-token
        expired-delete-this!))))
