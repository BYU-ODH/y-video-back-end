(defn add-namespace
  "Converts all keywords to namespace-keywords"
  [namespace m]
  (into {}
    (map (fn [val]
           {
            (keyword
              namespace
              (clojure.string/replace
                (str
                  (get val 0))
                ":"
                ""))
            (get val 1)})
      m)))

(into {}
  (map (fn [val]
         {
          (keyword
            word
            (clojure.string/replace
              (str
                (get val 0))
              ":"
              ""))
          (get val 1)})
    m))


(map #(list (symbol (clojure.string/replace (str (get % 0)) ":" (str ":" word "/")))) m)

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



(defn x-to-y
  "Convert all instances of x to y in map names"
  [map_in])
