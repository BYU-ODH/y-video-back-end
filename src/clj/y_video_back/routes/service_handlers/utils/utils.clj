(ns y-video-back.routes.service-handlers.utils.utils
  (:require [y-video-back.config :refer [env]]
            [y-video-back.db.files :as files]
            [y-video-back.db.collections :as collections]
            [y-video-back.db.users :as users]
            [y-video-back.db.resource-access :as resource-access]
            [clojure.string :as str]))

(defn remove-db-only
  "Removes created, updated, deleted, and other db-specific fields from map"
  [my-map]
  (dissoc my-map :created :updated :deleted :last-person-api :last-course-api :byu-person-id :last-verified))

(defn to-uuid
  [text-in]
  (java.util.UUID/fromString text-in))

(defn get-id
  [res]
  (str (:id res)))

(defn now [] (new java.util.Date))

(defn get-thumbnail
  [url]
  (if (str/includes? url "youtube")
    (let [video-args (get (str/split url #"v=") 1)]
      (if-not (nil? video-args)
        (let [video-id (get (str/split video-args #"&") 0)]
          (str "https://img.youtube.com/vi/" video-id "/0.jpg"))
        nil))
    nil))

(defn file-id-to-path
  "Returns absolute version of filepath for file-id"
  [file-id]
  (str (-> env :FILES :media-url)
       (:filepath (files/READ file-id))))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (let [num (rand-int 36)]
                                               (if (< num 10)
                                                 num
                                                 (+ num 39)))
                                             48))))))

(defn get-filename
  "Generates random file name with extension"
  [given-name]
  (str (rand-str 8)
       (str "." (last (str/split given-name #"\.")))))


(defn is-valid-access-time
  "Checks whether resource-access last-verified time is recent enough"
  [last-verified]
  (> (inst-ms last-verified)
     (- (System/currentTimeMillis) (* 3600000 (-> env :resource-access-expire-after)))))


(defn has-resource-permission
  "Checks if user with username has permission to add resource to content. Body must contain resource-id and collection-id. Must verify collection and resource before calling."
  [resource-id collection-id]
  (let [coll (collections/READ collection-id)
        owner (users/READ (:owner coll))]
    (if (nil? owner)
      false
      (resource-access/EXISTS-USERNAME-RESOURCE? (:username owner) resource-id))))
      ; TODO need to include check for user connected via user-collections-assoc
      ; Currently, the owner of the collection must have access to the resource.
      ; This means that TAs cannot create contents with restricted resources.
