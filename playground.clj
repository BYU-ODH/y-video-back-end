(doseq [n-itr (range n)]
    (def x (Double/parseDouble (clojure.string/trim (read-line))))
    (defn fact
      [n]
      (if (= n 0)
        1
        (* n (fact (- n 1)))))
    (defn exp
      [base pow]
      (if (= pow 0)
        1
        (* base (exp base (- pow 1)))))
    (println (reduce + (map #(/ (exp x %) (fact %)) (range 10)))))


(defn fact
  [n]
  (if (= n 0)
    1
    (* n (fact (- n 1)))))

(defn exp
  [base pow]
  (if (= pow 0)
    1
    (* base (exp base (- pow 1)))))
