(ns y-video-postgres-swagger.models)


(def user_without_id
  {:email string? :last_login string? :account_name string?
   :account_role int? :username string?})

(def user
  (into user_without_id {:id string?}))

(def word_without_id_or_user_id
  {:word string? :src_lang string? :dest_lang string?})

(def word
  (into word_without_id_or_user_id {:id string? :user_id string?}))

(def collection_without_id
  {:collection_name string? :published boolean? :archived boolean?})

(def collection
  (into collection_without_id {:id string?}))
