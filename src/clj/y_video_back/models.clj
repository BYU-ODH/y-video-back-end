(ns y-video-back.models
  (:require [schema.core :as sch]
            [spec-tools.core :as st]
            [clojure.spec.alpha :as s]))

(defn add-namespace
  "Converts all keywords to namespace-keywords, returns vector of keywords"
  [namespace m]
  (into []
    (map (fn [val]
            (keyword
              namespace
              (clojure.string/replace
                (str
                  (get val 0))
                ":"
                "")))
      m)))

(def echo_patch
  {:echo string?})

(def user_without_id
  {:email string? :last-login string? :account-name string?
   :account-type int? :username string?})

(def user
  (into user_without_id {:id uuid?}))

(def user_without_id_ns_params  ; Not in use
  (add-namespace "user" {:variable string?}))

(def word_without_id_or_user_id
  {:word string? :src-lang string? :dest-lang string?})

(def word_without_id
  (into word_without_id_or_user_id {:user-id uuid?}))

(def word
  (into word_without_id {:id uuid?}))

(def collection_without_id
  {:collection-name string? :published boolean? :archived boolean?})

(def collection
  (into collection_without_id {:id uuid?}))

(def course_without_id
  {:department string? :catalog-number string? :section-number string?})

(def course
  (into course_without_id {:id uuid?}))

(def content_without_id
  {:content-name string? :content-type string? :requester-email string?
   :thumbnail string? :copyrighted boolean? :physical-copy-exists boolean?
   :full-video boolean? :published boolean? :allow-definitions boolean?
   :allow-notes boolean? :allow-captions boolean? :date-validated string?
   :views int? :metadata string?})

(def content
  (into content_without_id {:id uuid?}))

(def annotation_without_id
  {:content_id string? :collection_id string? :metadata string?})

(def annotation
  (into annotation_without_id {:id uuid?}))

(def file_without_id
  {:filepath string? :mime string? :metadata string?})

(def file
  (into file_without_id {:id uuid?}))

(def user_collections_assoc_without_any_ids
  {:account-role int?})

(def user_collections_assoc_without_id
  (into user_collections_assoc_without_any_ids {:user-id uuid? :collection-id uuid?}))

(def user_collections_assoc
  (into user_collections_assoc_without_id {:id uuid?}))

(def collection_contents_assoc_without_any_ids
  {})

(def collection_contents_assoc_without_id
  (into collection_contents_assoc_without_any_ids {:collection-id uuid? :content-id uuid?}))

(def collection_contents_assoc
  (into collection_contents_assoc_without_id {:id uuid?}))

(def collection_courses_assoc_without_any_ids
  {})

(def collection_courses_assoc_without_id
  (into collection_courses_assoc_without_any_ids {:collection-id uuid? :course-id uuid?}))

(def collection_courses_assoc
  (into collection_courses_assoc_without_id {:id uuid?}))
