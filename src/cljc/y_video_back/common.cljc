(ns y-video-back.common)

(defn deep-merge* [& maps]
  (let [f (fn [old new]
             (if (and (map? old) (map? new))
                 (merge-with deep-merge* old new)
                 new))]
    (if (every? map? maps)
      (apply merge-with f maps)
     (last maps))))

(defn deep-merge [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
   (apply merge-with deep-merge* maps)))
