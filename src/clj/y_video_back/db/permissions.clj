(ns y-video-back.db.permissions
  (:require [y-video-back.db.core :as db]))

(def READ-ALL-BY-OBJ-ID (partial db/read-all-where :parent-collections :object-id))
(def READ-BY-USER (partial db/read-all-where :user_collection_roles :user-id))
