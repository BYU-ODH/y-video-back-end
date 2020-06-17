(ns y-video-back.utils.model_generator
  (:require
    [y-video-back.models :as models]))

; - - - - - - - - - LOCAL UTIL FUNCTIONS - - - - - - - - ;

(defn rand-str [len]
  (apply str (take (+ (int (rand (- len 7))) 7) (repeatedly #(char (+ (rand 26) 65))))))

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

(defn get_random_word_without_id
  ([]
   (get_random_model models/word_without_id))
  ([user-id]
   (into (get_random_model models/word_without_id_or_user_id) {:user-id user-id})))

(defn get_random_word_without_id_or_user_id
  []
  (get_random_model models/word_without_id_or_user_id))

(defn get_random_collection_without_id_or_owner
  []
  (get_random_model models/collection_without_id_or_owner))

;(update (update (get_random_model models/collection_without_id) :archived #(and false %)) :published #(and false %)))

(defn get_random_course_without_id
  []
  (get_random_model models/course_without_id))

(defn get_random_content_without_id
  []
  (get_random_model models/content_without_id))

(defn get_random_file_without_id
  []
  (get_random_model models/file_without_id))

(defn get_random_user_collections_assoc_without_id
  ([]
   (get_random_model models/user_collections_assoc_without_id))
  ([user-id collection-id]
   (into (get_random_model models/user_collections_assoc_without_any_ids) {:user-id user-id :collection-id collection-id})))

(defn get_random_user_courses_assoc_without_id
  ([]
   (get_random_model models/user_courses_assoc_without_id))
  ([user-id course-id]
   (into (get_random_model models/user_courses_assoc_without_any_ids) {:user-id user-id :course-id course-id})))


(defn get_random_collection_contents_assoc_without_id
  ([]
   (get_random_model models/collection_contents_assoc_without_id))
  ([collection-id content-id]
   (into (get_random_model models/collection_contents_assoc_without_any_ids) {:collection-id collection-id :content-id content-id})))

(defn get_random_collection_courses_assoc_without_id
  ([]
   (get_random_model models/collection_courses_assoc_without_id))
  ([collection-id course-id]
   (into (get_random_model models/collection_courses_assoc_without_any_ids) {:collection-id collection-id :course-id course-id})))

(defn get_random_content_files_assoc_without_id
  ([]
   (get_random_model models/content_files_assoc_without_id))
  ([content-id file-id]
   (into (get_random_model models/content_files_assoc_without_any_ids) {:content-id content-id :file-id file-id})))

(defn get_random_annotation_without_id
  ([]
   (get_random_model models/annotation_without_id))
  ([collection-id content-id]
   (into (get_random_model models/annotation_without_any_ids) {:collection-id collection-id :content-id content-id})))
