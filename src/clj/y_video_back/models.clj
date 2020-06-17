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

(def echo-patch
  {:echo string?})

(def user-without-id
  {:email string? :last-login string? :account-name string?
   :account-type int? :username string?})

(def user
  (into user-without-id {:id uuid?}))

(def user-without-id-ns-params  ; Not in use
  (add-namespace "user" {:variable string?}))

(def word-without-id-or-user-id
  {:word string? :src-lang string? :dest-lang string?})

(def word-without-id
  (into word-without-id-or-user-id {:user-id uuid?}))

(def word
  (into word-without-id {:id uuid?}))

(def collection-without-id-or-owner
  {:collection-name string? :published boolean? :archived boolean?})

(def collection-without-id
  (into collection-without-id-or-owner {:owner uuid?}))

(def collection
  (into collection-without-id {:id uuid?}))

(def collection-read-from-db
  (-> collection-without-id-or-owner
      (into {:id uuid?})
      (into {:owner user})))

(def course-without-id
  {:department string? :catalog-number string? :section-number string?})

(def course
  (into course-without-id {:id uuid?}))

(def content-without-id
  {:content-name string? :content-type string? :requester-email string?
   :thumbnail string? :copyrighted boolean? :physical-copy-exists boolean?
   :full-video boolean? :published boolean? :allow-definitions boolean?
   :allow-notes boolean? :allow-captions boolean? :date-validated string?
   :views int? :metadata string?})

(def content
  (into content-without-id {:id uuid?}))

(def annotation-without-any-ids
  {:metadata string?})

(def annotation-without-id
  (into annotation-without-any-ids {:content-id uuid? :collection-id uuid?}))

(def annotation
  (into annotation-without-id {:id uuid?}))

(def file-without-id
  {:filepath string? :mime string? :metadata string?})

(def file
  (into file-without-id {:id uuid?}))

(def user-collections-assoc-without-any-ids
  {:account-role int?})

(def user-collections-assoc-without-id
  (into user-collections-assoc-without-any-ids {:user-id uuid? :collection-id uuid?}))

(def user-collections-assoc
  (into user-collections-assoc-without-id {:id uuid?}))

(def user-courses-assoc-without-any-ids
  {:account-role int?})

(def user-courses-assoc-without-id
  (into user-courses-assoc-without-any-ids {:user-id uuid? :course-id uuid?}))

(def user-courses-assoc
  (into user-courses-assoc-without-id {:id uuid?}))


(def collection-contents-assoc-without-any-ids
  {})

(def collection-contents-assoc-without-id
  (into collection-contents-assoc-without-any-ids {:collection-id uuid? :content-id uuid?}))

(def collection-contents-assoc
  (into collection-contents-assoc-without-id {:id uuid?}))

(def collection-courses-assoc-without-any-ids
  {})

(def collection-courses-assoc-without-id
  (into collection-courses-assoc-without-any-ids {:collection-id uuid? :course-id uuid?}))

(def collection-courses-assoc
  (into collection-courses-assoc-without-id {:id uuid?}))

(def content-files-assoc-without-any-ids
  {})

(def content-files-assoc-without-id
  (into content-files-assoc-without-any-ids {:content-id uuid? :file-id uuid?}))

(def content-files-assoc
  (into content-files-assoc-without-id {:id uuid?}))
