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

(s/def :resource/requester-email string?)
(s/def :resource/physical-copy-exists boolean?)
(s/def :resource/allow-definitions boolean?)
(s/def :resource/resource-type string?)
(s/def :resource/resource-name string?)
(s/def :resource/date-validated string?)
(s/def :resource/allow-notes boolean?)
(s/def :resource/views int?)
(s/def :resource/copyrighted boolean?)
(s/def :resource/published boolean?)
(s/def :resource/metadata string?)
(s/def :resource/allow-captions boolean?)
(s/def :resource/full-video boolean?)
(s/def :resource/thumbnail string?)
(s/def ::resource
  (s/keys :opt-un [:resource/requester-email
                   :resource/physical-copy-exists
                   :resource/allow-definitions
                   :resource/resource-type
                   :resource/resource-name
                   :resource/date-validated
                   :resource/allow-notes
                   :resource/views
                   :resource/copyrighted
                   :resource/published
                   :resource/metadata
                   :resource/allow-captions
                   :resource/full-video
                   :resource/thumbnail]))

(s/def :annotation/metadata string?)
(s/def :annotation/resource-id uuid?)
(s/def :annotation/collection-id uuid?)
(s/def ::annotation
  (s/keys :opt-un [:annotation/metadata
                   :annotation/resource-id
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
