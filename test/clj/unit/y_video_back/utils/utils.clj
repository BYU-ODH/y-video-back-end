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
  "Removes created, updated, and deleted fields"
  [my_map]
  (dissoc my_map :created :updated :deleted))

(defn to-uuid
  [text_in]
  (java.util.UUID/fromString text_in))

(defn random-submap
  "Returns a map containing n key-value pairs of map_in. min_take <= n <= max_take"
  ([map_in]
   (case (count map_in)
     0 map_in
     1 (random-submap map_in 0 1)
     2 (random-submap map_in 1 2)
     3 (random-submap map_in 2 3)
     (random-submap
       map_in
       (max 2 (int (Math/floor (/ (count map_in) 3))))
       (int (Math/ceil (* 2 (/ (count map_in) 3)))))))
  ([map_in min_take max_take]
   (let [shuffled_map (shuffle (seq map_in))
         cutoff (+ min_take (rand-int (- (inc max_take) min_take)))]
     (into {} (take cutoff shuffled_map)))))
