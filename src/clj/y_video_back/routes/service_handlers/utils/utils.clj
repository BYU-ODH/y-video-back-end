(ns y-video-back.routes.service-handlers.utils.utils
  (:require [y-video-back.config :refer [env]]
            [y-video-back.db.files :as files]
            [clojure.string :as str]))
(defn remove-db-only
  "Removes created, updated, and deleted fields from map"
  [my-map]
  (dissoc my-map :created :updated :deleted :last-person-api :last-course-api :byu-person-id))

(defn add-namespace ; Can probably delete this function, not in use?
  "Converts all keywords to namespace-keywords"
  [namespace m]
  (into {}
    (map (fn [val]
           {
            (keyword
              namespace
              (str/replace
                (str
                  (get val 0))
                ":"
                ""))
            (get val 1)})
      m)))

(defn to-uuid
  [text-in]
  (java.util.UUID/fromString text-in))

(defn nuuid?
  "Returns true if val is uuid or nil"
  [val]
  (or (uuid? val)
      (nil? val)))

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

(defn split-first [re s]
  (str/split s re 2))

(defn split-last [re s]
  (let [pattern (re-pattern (str re "(?!.*" re ")"))]
    (split-first pattern s)))

(defn insert-before-ext
  "Inserts ins before file extension. If no extension, appends ins."
  [file-name, ins]
  (let [file-split (split-last #"\." file-name)]
    (if (empty? file-split)
      (str file-name ins)
      (str
        (reduce str (drop-last file-split))
        "-"
        ins
        "."
        (last file-split)))))

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

(defn user-db-to-front
  "Replace keywords with what the front end expects"
  [user]
  {:id (:id user)
   :username (:username user)
   :name (:account-name user)
   :email (:email user)
   :roles [(:account-type user)]
   :lastLogin (:last-login user)})

(defn coll-db-to-front
  "Replace keywords with what the front end expects"
  [coll]
  {:id (:id coll)
   :name (:collection-name coll)
   :published (:published coll)
   :archived (:archived coll)})

(defn cont-db-to-front
  "Replace keywords with what the front end expects"
  [cont]
  {:id (:id cont)
   :name (:resource-name cont)
   :resourceType (:resource-type cont)
   :requester (:requester-email cont)
   :thumbnail (:thumbnail cont)
   :isCopyrighted (:copyrighted cont)
   :physicalCopyExists (:physical-copy-exists cont)
   :fullVideo (:full-video cont)
   :published (:published cont)
   :allow-definitions (:allow-definitions cont)
   :allow-notes (:allow-notes cont)
   :allow-captions (:allow-captions cont)
   :dateValidated (:date-validated cont)
   :views (:views cont)
   :metadata (:metadata cont)})


(defn temp
  [& args]
  args)

(def v [[false 1] [false 2] [true 3] [true 4]])
