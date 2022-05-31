(ns legacy.utils.model-generator
  (:require
    [y-video-back.models :as models]))

; - - - - - - - - - LOCAL UTIL FUNCTIONS - - - - - - - - ;

(defn rand-str [len]
  (apply str (take (+ (int (rand (- len 7))) 7) (repeatedly #(char (+ (rand 26) 65))))))

(defn rand-bool []
  (if (< 0 (rand-int 2))
    true
    false))

(defn get-random-model
  "Generate a test model with random field values"
  [model]
  (into {} (map #(if (= (string? "") ((get % 1) ""))
                   [(get % 0) (rand-str 32)]
                   (if (= (boolean? true) ((get % 1) true))
                     [(get % 0) (rand-bool)]
                     (if (= (int? true) ((get % 1) true))
                       [(get % 0) (rand-int 1000)]
                       [(get % 0) (java.util.UUID/randomUUID)])))
               model)))

(defn get-invalid-model
  "Generate a test model with strings and bools flipped"
  [model]
  (into {} (map #(if (= (string? "") ((get % 1) ""))
                   [(get % 0) (rand-int 1000)]
                   (if (= (boolean? true) ((get % 1) true))
                     [(get % 0) (java.util.UUID/randomUUID)]
                     (if (= (int? true) ((get % 1) true))
                       [(get % 0) (rand-str 32)]
                       [(get % 0) (rand-bool)])))
               model)))


; - - - - - - - - - VALID MODEL GENERATORS - - - - - - - - ;


(defn get-random-user-without-id
  []
  (-> (get-random-model models/user-without-id)
      (dissoc :account-type)
      (assoc :account-type (rand-int 4))))

(defn get-random-word-without-id-or-user-id
  []
  (get-random-model models/word-without-id-or-user-id))

(defn get-random-word-without-id
  ([]
   (into (get-random-model models/word-without-id-or-user-id) {:user-id (java.util.UUID/randomUUID)}))
  ([user-id]
   (into (get-random-model models/word-without-id-or-user-id) {:user-id user-id})))

(defn get-random-word-without-id-or-user-id
  []
  (get-random-model models/word-without-id-or-user-id))

(defn get-random-collection-without-id-or-owner
  []
  (assoc
    (dissoc (get-random-model models/collection-without-id-or-owner)
            :public)
    :public
    false))

(defn get-random-collection-without-id
  ([owner-id]
   (into (get-random-collection-without-id-or-owner) {:owner owner-id}))
  ([]
   (get-random-collection-without-id (java.util.UUID/randomUUID))))

(defn get-random-collection-without-id-no-owner
  []
  (into (get-random-collection-without-id-or-owner) {:owner nil}))

(defn get-random-course-without-id
  []
  (get-random-model models/course-without-id))

(defn get-random-resource-without-id
  []
  (get-random-model models/resource-without-id))

(defn get-random-resource-access-without-id
  [username resource-id]
  (into (get-random-model models/resource-access-without-id) {:username username :resource-id resource-id}))

(defn get-random-language-without-id
  ([]
   (get-random-model models/language-without-id)))

(defn get-random-subtitle-without-id
  ([]
   (get-random-model models/subtitle-without-id))
  ([content-id]
   (into (get-random-model models/subtitle-without-any-ids) {:content-id content-id})))

(defn get-random-file-without-id
  ([]
   (get-random-model models/file-without-id))
  ([resource-id]
   (into (get-random-model models/file-without-any-ids) {:resource-id resource-id})))

(defn get-random-user-collections-assoc-without-id
  ([]
   (get-random-model models/user-collections-assoc-without-id))
  ([username collection-id]
   (into (get-random-model models/user-collections-assoc-without-any-ids) {:username username :collection-id collection-id})))

(defn get-random-user-courses-assoc-without-id
  ([]
   (get-random-model models/user-courses-assoc-without-id))
  ([user-id course-id]
   (into (get-random-model models/user-courses-assoc-without-any-ids) {:user-id user-id :course-id course-id})))

(defn get-random-collection-courses-assoc-without-id
  ([]
   (get-random-model models/collection-courses-assoc-without-id))
  ([collection-id course-id]
   (into (get-random-model models/collection-courses-assoc-without-any-ids) {:collection-id collection-id :course-id course-id})))

(defn get-random-content-without-id
  ([]
   (get-random-model models/content-without-id))
  ([collection-id resource-id file-id]
   (into (get-random-model models/content-without-any-ids) {:collection-id collection-id :resource-id resource-id :file-id file-id})))

; - - - - - - - - - INVALID MODEL GENERATORS - - - - - - - - ;

(defn get-invalid-user-without-id
  []
  (get-invalid-model models/user-without-id))
