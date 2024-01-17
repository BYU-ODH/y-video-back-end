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

(defn READ-UNEXPIRED
  "Reads unexpired auth-tokens. If expired, deletes and returns nil."
  [auth-token-id]
  (when auth-token-id
    (let [auth-token (READ auth-token-id)
          timeout (-> env :auth :timeout)
          time-created #(inst-ms (:created auth-token))
          current-ms (System/currentTimeMillis)
          expired? #(< (time-created)
                       (-  current-ms timeout))]
      (log/debug "" {:auth-token-id (str auth-token-id)
                     :auth-token (str auth-token)})
      (when auth-token
        #_(log/debug "Are we expired? If so, why?" {:expired? (expired?)
                                                  :time-token-created (time-created)
                                                  :current-ms current-ms
                                                  :timeout timeout
                                                  :diff (- (time-created)
                                                           (-  current-ms timeout))
                                                  :auth-token auth-token})
        (if-not (expired?)
          auth-token
          (let [d! (DELETE auth-token-id)]
              (log/info "log returns nil" {:deleted d!})))))))

