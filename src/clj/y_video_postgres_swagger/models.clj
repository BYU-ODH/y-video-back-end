(ns y-video-postgres-swagger.models
  (:require [schema.core :as sch]
            [spec-tools.core :as st]
            [clojure.spec.alpha :as s]))

(def echo_patch
  {:echo string?})

(sch/defschema user_without_id
  {:email string? :last_login string? :account_name string?
   :account_role int? :username string?})

(def user
  (into user_without_id {:id string?}))

(def word_without_id_or_user_id
  {:word string? :src_lang string? :dest_lang string?})

(def word_without_id
  (into word_without_id_or_user_id {:user_id string?}))

(def word
  (into word_without_id {:id string?}))

(def collection_without_id
  {:collection_name string? :published boolean? :archived boolean?})

(def collection
  (into collection_without_id {:id string?}))

(def course_without_id
  {:department string? :catalog_number string? :section_number string?})

(def course
  (into course_without_id {:id string?}))

(def content_without_id_or_collection_id
  {:content_name string? :content_type string? :requester_email string?
   :thumbnail string? :copyrighted boolean? :physical_copy_exists boolean?
   :full_video boolean? :published boolean? :allow_definitions boolean?
   :allow_notes boolean? :allow_captions boolean? :date_validated string?
   :views int? :metadata string?})

(def content_without_id
  (into content_without_id_or_collection_id {:collection_id string?}))

(def content
  (into content_without_id {:id string?}))

(def file_without_id
  {:filepath string? :mime string? :metadata string?})

(def file
  (into file_without_id {:id string?}))
