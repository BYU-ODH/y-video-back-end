(ns y-video-back.models)

(def echo-patch
  {:echo string?})

(def user-without-id
  {:email string?
   :last-login string?
   :account-name string?
   :account-type int?
   :username string?})

(def user-byu 
  {:full-name string?
   :email string?
   :account-type int?})

(def user
  (into user-without-id {:id uuid?}))

(def word-without-id-or-user-id
  {:word string?
   :src-lang string?
   :dest-lang string?})

(def word-without-id
  (into word-without-id-or-user-id {:user-id uuid?}))

(def word
  (into word-without-id {:id uuid?}))

(def collection-without-id-or-owner
  {:collection-name string?
   :published boolean?
   :archived boolean?
   :public boolean?
   :copyrighted boolean?})

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
   :catalog-number string?
   :section-number string?})

(def course
  (into course-without-id {:id uuid?}))

(def resource-without-id
  {:resource-name string?
   :resource-type string?
   :requester-email string?
   :copyrighted boolean?
   :physical-copy-exists boolean?
   :full-video boolean?
   :published boolean?
   :date-validated string?
   :views int?
   :all-file-versions string?
   :metadata string?})

(def resource
  (into resource-without-id {:id uuid?}))

(def resource-access-without-id
  {:username string?
   :resource-id string?})

(def resource-access
  (into resource-access-without-id {:id uuid?}))

(def content-without-any-ids
  {:title string?
   :content-type string?
   :url string?
   :description string?
   :tags string?
   :annotations string?
   :thumbnail string?
   :allow-definitions boolean?
   :allow-notes boolean?
   :allow-captions boolean?
   :views integer?
   :file-version string?
   :published boolean?
   :words string?
   :clips string?})

(def content-without-id
  (into content-without-any-ids {:resource-id uuid?
                                 :collection-id uuid?
                                 :file-id uuid?}))

(def content
  (into content-without-id {:id uuid?}))

(def subtitle-without-any-ids
  {:title string?
   :content string?
   :language string?
   :words string?})

(def subtitle-without-id
  (into subtitle-without-any-ids {:content-id uuid?}))

(def subtitle
  (into subtitle-without-id {:id uuid?}))

(def file-without-any-ids
  {:filepath string?
   :file-version string?
   :metadata string?
   :aspect-ratio string?})

(def file-without-id
  (into file-without-any-ids {:resource-id uuid?}))

(def file
  (into file-without-id {:id uuid?}))

(def language-without-id
  {:id string?})

(def language
  language-without-id)

(def user-collections-assoc-without-any-ids
  {:account-role int?})

(def user-collections-assoc-without-id
  (into user-collections-assoc-without-any-ids {:username string? :collection-id uuid?}))

(def user-collections-assoc
  (into user-collections-assoc-without-id {:id uuid?}))

(def user-courses-assoc-without-any-ids
  {:account-role int?})

(def user-courses-assoc-without-id
  (into user-courses-assoc-without-any-ids {:user-id uuid? :course-id uuid?}))

(def user-courses-assoc
  (into user-courses-assoc-without-id {:id uuid?}))

(def collection-courses-assoc-without-any-ids
  {})

(def collection-courses-assoc-without-id
  (into collection-courses-assoc-without-any-ids {:collection-id uuid? :course-id uuid?}))

(def collection-courses-assoc
  (into collection-courses-assoc-without-id {:id uuid?}))
