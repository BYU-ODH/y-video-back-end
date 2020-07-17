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





(defn lots-of-params
  [table-keyword [& column-keywords] [& column-vals] & c]
  (println table-keyword)
  (println column-keywords)
  (println column-vals)
  (if c
    (println c))
  "done!")


(defn hey
  ([{:keys [first]}]
   (println (str "hey there, " first)))
  ([{:keys [first last]}]
   (str "why so formal, " first " " last "?")))

(defmulti hey (fn [m] [(keys m)]))

(defmethod hey
  ['(:first)]
  [{:keys [first]}]
  (str "hey there, " first))

(defmethod hey
  ['(:first :last)]
  [{:keys [first last]}]
  (str "why so formal, " first " " last "?"))


(try
  (throw (Exception. "one"))
  (catch (Exception. "one") e
    (prn "caught" e)))


(first (filter #(not (= "" %)) [(:thumbnail body) (get-thumbnail ("https://www.youtube.com/watch?v=eYYUAib5EWo") "none")]))
