(ns y-video-postgres-swagger.test.test_model_generator
  (:require
    [y-video-postgres-swagger.models :as models]))

; - - - - - - - - - LOCAL UTIL FUNCTIONS - - - - - - - - ;

(defn rand-str [len]
  (apply str (take (int (rand len)) (repeatedly #(char (+ (rand 26) 65))))))

(defn rand-bool []
  (if (< 0 (rand-int 2))
    true
    false))

(defn get_random_model
  "Generate a test user with random field values"
  [model]
  (into {} (map #(if (= (string? "") ((get % 1) ""))
                   [(get % 0) (rand-str 32)]
                   (if (= (boolean? true) ((get % 1) true))
                     [(get % 0) (rand-bool)]
                     (if (= (int? true) ((get % 1) true))
                       [(get % 0) (rand-int 1000)]
                       [(get % 0) (java.util.UUID/randomUUID)])))
               model)))


; - - - - - - - - - MODEL GENERATORS - - - - - - - - ;


(defn get_random_user_without_id
  []
  (get_random_model models/user_without_id))

(defn get_random_word_without_id_or_user_id
  []
  (get_random_model models/word_without_id_or_user_id))

(defn get_random_collection_without_id
  []
  (get_random_model models/collection_without_id))
