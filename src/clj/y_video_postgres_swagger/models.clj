(ns y-video-postgres-swagger.models)


(def user_without_id
           {:email string? :last_login string? :account_name string?
            :account_role int? :username string?})

(def user
  (into user_without_id {:id string?}))
