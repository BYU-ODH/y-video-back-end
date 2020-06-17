(ns y-video-back.model-specs
  (:require
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]))


(s/def :user/email string?)
(s/def :user/last-login string?)
(s/def :user/account-name string?)
(s/def :user/account-type int?)
(s/def :user/username string?)
(s/def ::user
  (s/keys :opt-un [:user/email
                   :user/last-login
                   :user/account-name
                   :user/account-type
                   :user/username]))

(s/def :word/word string?)
(s/def :word/src-lang string?)
(s/def :word/dest-lang string?)
(s/def :word/user-id uuid?)
(s/def ::word
  (s/keys :opt-un [:word/word
                   :word/src-lang
                   :word/dest-lang
                   :word/user-id]))

(s/def :collection/collection-name string?)
(s/def :collection/published boolean?)
(s/def :collection/archived boolean?)
(s/def :collection/owner uuid?)
(s/def ::collection
  (s/keys :opt-un [:collection/collection-name
                   :collection/published
                   :collection/archived
                   :collection/owner]))

(s/def :content/requester-email string?)
(s/def :content/physical-copy-exists boolean?)
(s/def :content/allow-definitions boolean?)
(s/def :content/content-type string?)
(s/def :content/content-name string?)
(s/def :content/date-validated string?)
(s/def :content/allow-notes boolean?)
(s/def :content/views int?)
(s/def :content/copyrighted boolean?)
(s/def :content/published boolean?)
(s/def :content/metadata string?)
(s/def :content/allow-captions boolean?)
(s/def :content/full-video boolean?)
(s/def :content/thumbnail string?)
(s/def ::content
  (s/keys :opt-un [:content/requester-email
                   :content/physical-copy-exists
                   :content/allow-definitions
                   :content/content-type
                   :content/content-name
                   :content/date-validated
                   :content/allow-notes
                   :content/views
                   :content/copyrighted
                   :content/published
                   :content/metadata
                   :content/allow-captions
                   :content/full-video
                   :content/thumbnail]))

(s/def :annotation/metadata string?)
(s/def :annotation/content-id uuid?)
(s/def :annotation/collection-id uuid?)
(s/def ::annotation
  (s/keys :opt-un [:annotation/metadata
                   :annotation/content-id
                   :annotation/collection-id]))

(s/def :file/filepath string?)
(s/def :file/mime string?)
(s/def :file/metadata string?)
(s/def ::file
  (s/keys :opt-un [:file/filepath
                   :file/mime
                   :file/metadata]))

(s/def :course/department string?)
(s/def :course/catalog-number string?)
(s/def :course/section-number string?)
(s/def ::course
  (s/keys :opt-un [:course/department
                   :course/catalog-number
                   :course/section-number]))
