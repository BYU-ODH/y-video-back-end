(ns y-video-back.model-specs
  (:require
   [clojure.spec.alpha :as s]))


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
(s/def :collection/public boolean?)
(s/def :collection/copyrighted boolean?)
(s/def :collection/owner uuid?)
(s/def ::collection
  (s/keys :opt-un [:collection/collection-name
                   :collection/published
                   :collection/archived
                   :collection/public
                   :collection/copyrighted
                   :collection/owner]))

(s/def :resource/resource-name string?)
(s/def :resource/resource-type string?)
(s/def :resource/requester-email string?)
(s/def :resource/copyrighted boolean?)
(s/def :resource/physical-copy-exists boolean?)
(s/def :resource/full-video boolean?)
(s/def :resource/published boolean?)
(s/def :resource/date-validated string?)
(s/def :resource/views int?)
(s/def :resource/all-file-versions string?)
(s/def :resource/metadata string?)
(s/def ::resource
  (s/keys :opt-un [:resource/resource-name
                   :resource/resource-type
                   :resource/requester-email
                   :resource/copyrighted
                   :resource/physical-copy-exists
                   :resource/full-video
                   :resource/published
                   :resource/date-validated
                   :resource/views
                   :resource/all-file-versions
                   :resource/metadata]))

(s/def :content/title string?)
(s/def :content/content-type string?)
(s/def :content/url string?)
(s/def :content/description string?)
(s/def :content/tags string?)
(s/def :content/annotations string?)
(s/def :content/thumbnail string?)
(s/def :content/allow-definitions boolean?)
(s/def :content/allow-notes boolean?)
(s/def :content/allow-captions boolean?)
(s/def :content/views integer?)
(s/def :content/file-version string?)
(s/def :content/published boolean?)
(s/def :content/words string?)
(s/def :content/clips string?)
(s/def :content/resource-id uuid?)
(s/def :content/collection-id uuid?)
(s/def :content/file-id uuid?)
(s/def ::content
  (s/keys :opt-un [:content/title
                   :content/content-type
                   :content/url
                   :content/description
                   :content/tags
                   :content/annotations
                   :content/thumbnail
                   :content/allow-definitions
                   :content/allow-notes
                   :content/allow-captions
                   :content/views
                   :content/file-version
                   :content/published
                   :content/words
                   :content/clips
                   :content/resource-id
                   :content/collection-id
                   :content/file-id]))


(s/def :subtitle/title string?)
(s/def :subtitle/language string?)
(s/def :subtitle/content string?)
(s/def :subtitle/words string?)
(s/def :subtitle/content-id uuid?)
(s/def ::subtitle
  (s/keys :opt-un [:subtitle/title
                   :subtitle/language
                   :subtitle/content
                   :subtitle/words
                   :subtitle/content-id]))


(s/def :file/filepath string?)
(s/def :file/file-version string?)
(s/def :file/resource-id uuid?)
(s/def :file/metadata string?)
(s/def :file/aspect-ratio string?)
(s/def ::file
  (s/keys :opt-un [:file/filepath
                   :file/file-version
                   :file/resource-id
                   :file/metadata
                   :file/aspect-ratio]))

(s/def :course/department string?)
(s/def :course/catalog-number string?)
(s/def :course/section-number string?)
(s/def ::course
  (s/keys :opt-un [:course/department
                   :course/catalog-number
                   :course/section-number]))
