(ns y-video-back.utils.utils
  (:require
    [muuntaja.core :as m]
    [clojure.test :refer :all]))


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
  [my-map]
  (dissoc my-map :created :updated :deleted))

(defn to-uuid
  [text-in]
  (java.util.UUID/fromString text-in))

(def nil-uuid
  (to-uuid "00000000-0000-0000-0000-000000000000"))

(defn random-submap
  "Returns a map containing n key-value pairs of map-in. min-take <= n <= max-take"
  ([map-in]
   (case (count map-in)
     0 map-in
     1 (random-submap map-in 0 1)
     2 (random-submap map-in 1 2)
     3 (random-submap map-in 2 3)
     (random-submap
       map-in
       (max 2 (int (Math/floor (/ (count map-in) 3))))
       (int (Math/ceil (* 2 (/ (count map-in) 3)))))))
  ([map-in min-take max-take]
   (let [shuffled-map (shuffle (seq map-in))
         cutoff (+ min-take (rand-int (- (inc max-take) min-take)))]
     (into {} (take cutoff shuffled-map)))))

(defn check-header
  "Checks if session-id is in response header"
  [res]
  (is (contains? (:headers res) "session-id")) ;; Change to actually decode header then check
  res)
