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
  {:word string? :src_lang string? :dest_lang string?})

(def word_without_id
  (into word_without_id_or_user_id {:user_id uuid?}))

(def word
  (into word_without_id {:id uuid?}))

(def collection_without_id
  {:collection-name string? :published boolean? :archived boolean?})

(def collection
  (into collection_without_id {:id uuid?}))

(def course_without_id
  {:department string? :catalog_number string? :section_number string?})

(def course
  (into course_without_id {:id uuid?}))

(def content_without_id
  {:content_name string? :content_type string? :requester_email string?
   :thumbnail string? :filters string? :copyrighted boolean? :physical_copy_exists boolean?
   :full_video boolean? :published boolean? :allow_definitions boolean?
   :allow_notes boolean? :allow_captions boolean? :date_validated string?
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
