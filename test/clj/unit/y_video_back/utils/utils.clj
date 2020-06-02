(ns y-video-back.utils.utils
  (:require
    [muuntaja.core :as m]))


(defn under-to-hyphen
  "Converts all underscores to hypens in map keywords"
  [m]
  (into {}
    (map (fn [val]
           {
            (keyword
              (subs
                (clojure.string/replace
                  (str
                    (get val 0))
                  "_"
                  "-")
                1))
            (get val 1)})
      m)))

(defn get-id
  "Gets id from raw response body"
  [res]
  (:id (m/decode-response-body res)))

(defn remove-db-only
  "Compares 2 maps, not counting created, updated, and deleted fields"
  [my_map]
  (dissoc my_map :created :updated :deleted))

(defn to-uuid
  [text_in]
  (java.util.UUID/fromString text_in))
