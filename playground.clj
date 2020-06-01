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
