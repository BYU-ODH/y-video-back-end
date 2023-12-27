(ns y-video-back.semesters
  "helpers for BYU's peculiar semester numbers")

(def parse-int #?(:cljs js/parseInt
                  :clj #(Integer. %)))

(def TERM-NAMES {"1" "Winter"
                 "2" "Spring+Summer"
                 "3" "Spring"
                 "4" "Summer"
                 "5" "Fall"})

(def TNUM [1 3 4 5])

(defn abs
  "absolute value"
  [x]
  (cond-> x
    (neg? x) (* -1)))


(defn wrap-around [v x]
  (let [c (count v)
        idx (mod x c)
        times-around (cond-> (quot x c)
                       (and (neg? x)
                            (not= 0 idx)) dec)]
    {:value (v idx)
     :times times-around}))

(defn extract-term 
  "Get the term digit from a 5-digit yt"
  [yt]
  (-> yt str (nth 4) str parse-int))

(defn extract-year
  "Get the 4-digit year from a 5-digit yt"
  [yt]
  (->> yt str (take 4) (reduce str) parse-int))

(def semester-index
  {1 0
   3 1
   4 2
   5 3})

(defn to-yt
  "Converts a year number, 4 or 5 digits, to a yt, taking y1 if only a 4-digit num.
  Returns nil on empty input."
  [y]
  (let [count-digits (comp count str)]
    (condp = (count-digits y)
      5 (str y)
      4 (str y 1)
      0 nil
      :else (throw (ex-info "Invalid input to to-yt" {:input y})))))

(defn add-yt
  "Add `ytstring` with `x`, coming up with a valid semester.
  e.g. (+ \"20191\" 5) is \"20203\", and (+ \"20191\" -1) is (\"20185\")"
  [ytstring x]
  (let [year (extract-year ytstring)
        term-index (semester-index (extract-term ytstring))
        pre-result (wrap-around TNUM (+ term-index x))
        newy (+ year (:times pre-result))
        newt (:value pre-result)]
    (str newy newt)))


(defn inc-yt
  "Taking a yt string, increment it appropriately and return a map. Skip 2, wrap at 5."
  [yt]
  (let [yt (if (string? yt) yt
               (parse-int yt))
        
        y (->> yt (take 4) (reduce str) parse-int)
        t (->> yt last (reduce str) parse-int)]
    (condp = t
      1 (str y 3)
      5 (str (inc y) 1)
      (str y (inc t)))))

(defn gen-yt-text
  "Take a yt string (20155) and return text (\"Fall, 2015\")"
  [yt]
  (let [yt (str yt)
        y (->> yt (take 4) (reduce str))
        t (->> yt last str)]
    (str (get TERM-NAMES t) " " y)))
