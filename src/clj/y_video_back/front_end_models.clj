(ns y-video-back.front-end-models
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
  {:email string?
   :lastLogin string?
   :accountName string?
   :accountType int?
   :username string?})

(def user
  (into user-without-id {:id uuid?}))

(def user-without-id-ns-params  ; Not in use
  (add-namespace "user" {:variable string?}))

(def word-without-id-or-user-id
  {:word string?
   :srcLang string?
   :destLang string?})

(def word-without-id
  (into word-without-id-or-user-id {:userId uuid?}))

(def word
  (into word-without-id {:id uuid?}))

(def collection-without-id-or-owner
  {:collectionName string?
   :published boolean?
   :archived boolean?})

(def collection-without-id
  (into collection-without-id-or-owner {:owner uuid?}))

(def collection
  (into collection-without-id {:id uuid?}))

(def collection-read-from-db
  (-> collection-without-id-or-owner
      (into {:id uuid?})
      (into {:owner user})))

(def course-without-id
  {:department string?
   :catalogNumber string?
   :sectionNumber string?})

(def course
  (into course-without-id {:id uuid?}))

(def resource-without-id
  {:resourceName string?
   :resourceType string?
   :requesterEmail string?
   :copyrighted boolean?
   :physicalCopyExists boolean?
   :fullVideo boolean?
   :published boolean?
   :dateValidated string?
   :views int?
   :allFileVersions string?
   :metadata string?})

(def resource
  (into resource-without-id {:id uuid?}))

(def content-without-any-ids
  {:title string?
   :contentType string?
   :url string?
   :description string?
   :tags string?
   :annotations string?
   :thumbnail string?
   :allowDefinitions string?
   :allowNotes string?
   :allowCaptions string?
   :views integer?
   :fileVersion string?})

(def content-without-id
  (into content-without-any-ids {:resourceId uuid?
                                 :collectionId uuid?}))

(def content
  (into content-without-id {:id uuid?}))

(def file-without-any-ids
  {:filepath string?
   :fileVersion string?
   :mime string?
   :metadata string?})

(def file-without-id
  (into file-without-any-ids {:resourceId uuid?}))

(def file
  (into file-without-id {:id uuid?}))
