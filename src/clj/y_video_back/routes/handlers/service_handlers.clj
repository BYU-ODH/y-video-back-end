(ns y-video-back.routes.handlers.service_handlers
  (:require
   [y-video-back.db.annotations :as annotations]
   [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
   [y-video-back.db.users-by-collection :as users-by-collection]
   [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.content-files-assoc :as content_files_assoc]
   [y-video-back.db.contents :as contents]
   [y-video-back.db.courses :as courses]
   [y-video-back.db.files :as files]
   [y-video-back.db.user-collections-assoc :as user_collections_assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.words :as words]
   ;;[y-video-back.dbaccess.access :as db-access] ;; This should be refactored to the relevant db/TABLE files
   [y-video-back.models :as models]
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]))

(defn remove-db-only
  "Compares 2 maps, not counting created, updated, and deleted fields"
  [my_map]
  (dissoc my_map :created :updated :deleted))

(defn add-namespace ; Can probably delete this function, not in use?
  "Converts all keywords to namespace-keywords"
  [namespace m]
  (into {}
    (map (fn [val]
           {
            (keyword
              namespace
              (clojure.string/replace
                (str
                  (get val 0))
                ":"
                ""))
            (get val 1)})
      m)))

(defn to-uuid
  [text_in]
  (java.util.UUID/fromString text_in))

(defn get-id
  [res]
  (str (:id res)))

(s/def :echo/first string?)
(s/def :echo/second string?)
(s/def ::echo (s/keys :req-un [:echo/first]
                      :opt-un [:echo/second]))

; Optional parameter setup for user update route

(s/def :user/email string?)
(s/def :user/last-login string?)
(s/def :user/account-name string?)
(s/def :user/account-type int?)
(s/def :user/username string?)
(s/def ::user (s/keys :opt-un [:user/email
                               :user/last-login
                               :user/account-name
                               :user/account-type
                               :user/username]));(add-namespace "user" models/user_without_id)

; Optional parameter setup for collection update route

(s/def :collection/collection-name string?)
(s/def :collection/published boolean?)
(s/def :collection/archived boolean?)
(s/def ::collection (s/keys :opt-un [:collection/collection-name
                                     :collection/published
                                     :collection/archived]))



(def echo-patch
  {:summary "echo parameter post"
   :parameters {:body ::echo}
   :responses {200 {:body {:message string?}}}
   :handler (fn [ignore-me] {:status 200 :body {:message "this route does nothing!"}})})


(def connect-collection-and-course ;; Non-functional
  {:summary "Connects specified collection and course (bidirectional)"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def search-by-term ;; Non-functional
  {:summary "Searches users, collections, and content by search term"
   :parameters {:query {:query_term string?}}
   :responses {200 {:body {:users [models/user]
                           :collections [models/collection]
                           :courses [models/course]
                           :contents [models/content]}}}
   :handler (fn [{{{:keys [query_term]} :query} :parameters}]
              (let [result "placeholder"]
                {:status 200
                 :body result}))})


(def user-create
  {:summary "Creates a new user - FOR DEVELOPMENT ONLY"
   :parameters {:body models/user_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?
                           :error string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 user created"
                           :id (get-id (users/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message "unable to create user, email likely taken"
                             :error (.toString e)}})))})

(def user-get-by-id
  {:summary "Retrieves specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/user}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [user_result (users/READ id)]
                (if (nil? user_result)
                  {:status 404
                   :body {:message "user not found"}}
                  {:status 200
                   :body user_result})))})


(def user-update
  {:summary "Updates specified user"
   :parameters {:path {:id uuid?} :body ::user}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (users/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str 1 " users updated")}})))})  ; I know, hard coded. Will change later.

(def user-delete
  {:summary "Deletes specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (users/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message (str result " users deleted")}})))})


(def user-get-loggged-in ;; Non-functional
  {:summary "Retrieves the current logged-in user"
   :parameters {:query {:user_id string?}}
   :responses {200 {:body {:user_id string? :email string? :lastlogin string? :name string? :role int? :username string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :query} :parameters}]
              (let [user_result (users/READ id)]
                (if (nil? user_result)
                  {:status 404
                   :body {:message "requested user not found"}}
                  {:status 200
                   :body {:message user_result}})))})


(def user-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified user"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [models/collection]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result "placeholder"]
                {:status 200
                 :body result}))})


(def user-word-create
  {:summary "Creates a new word"
   :parameters {:body models/word_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{{:keys [user_id]} :path :keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 word created"
                           :id (get-id (words/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message e}})))})

(def user-word-get-by-id
  {:summary "Retrieves specified word"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/word}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [word_result (words/READ id)]
                (if (= "" (:id word_result))
                  {:status 404
                   :body {:message "requested word not found"}}
                  {:status 200
                   :body word_result})))})

(def user-word-update
  {:summary "Updates specified word"
   :parameters {:path {:id uuid?} :body models/word_without_id}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (words/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested word not found"}}
                  {:status 200
                   :body {:message (str result " words updated")}})))})

(def user-word-delete
  {:summary "Deletes specified word"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (words/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested word not found"}}
                  {:status 200
                   :body {:message (str result " words deleted")}})))})

(def user-get-all-words
  {:summary "Retrieves all words under specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})


(def collection-create ;; Non-functional
  {:summary "Creates a new collection with the given (temp) user as an owner"
   :parameters {:body {:collection models/collection_without_id :user_id uuid?}}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 collection created"
                           :id (let [collection_id (get-id (collections/CREATE (:collection body)))]
                                 (user_collections_assoc/CREATE {:user_id (:user_id body)
                                                                 :collection_id (to-uuid collection_id)
                                                                 :account_role 0})
                                 collection_id)}}
                   (catch Exception e
                     {:status 409
                      :body {:message e}})))})

(def collection-get-by-id ;; Not tested
  {:summary "Retrieves specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/collection}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [res (collections/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body res})))})

(def collection-update ;; Non-functional
  {:summary "Updates the specified collection"
   :parameters {:path {:id uuid?} :body ::collection}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (collections/UPDATE id body)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body {:message (str 1 " collections updated")}})))})

(def collection-delete ;; Non-functional
  {:summary "Deletes the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (collections/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested collection not found"}}
                  {:status 200
                   :body {:message (str 1 " collections deleted")}})))})

(def collection-add-user
  {:summary "Adds user to specified collection"
   :parameters {:path {:id uuid?} :body {:user-id uuid? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (get-id (user_collections_assoc/CREATE (into body {:collection-id id})))]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to add user"}}
                  {:status 200
                   :body {:message (str 1 " users added to collection")
                          :id result}})))})

(def collection-remove-user
  {:summary "Removes user from specified collection"
   :parameters {:path {:id uuid?} :body {:user_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (user_collections_assoc/DELETE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove user"}}
                  {:status 200
                   :body {:message (str result " users removed from collection")}})))})

(def collection-add-content
  {:summary "Adds content to specified collection"
   :parameters {:path {:id uuid?} :body {:content_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (get-id (collection_contents_assoc/CREATE id body))]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to add content"}}
                  {:status 200
                   :body {:message (str result " contents added to collection")}})))})


(def collection-remove-content
  {:summary "Removes content from specified collection"
   :parameters {:path {:id uuid?} :body {:content_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (collection_contents_assoc/DELETE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove content"}}
                  {:status 200
                   :body {:message (str result " contents removed from collection")}})))})

(def collection-get-all-contents ;; Non-functional
  {:summary "Retrieves all the contents for the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body string?}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              {:status 200
               :body "placeholder"}
              {:status 404
               :body {:message "collection not found"}})})

(def collection-get-all-courses
  {:summary "Retrieves all courses for the specified collection"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def collection-get-all-users
  {:summary "Retrieves all users for the specified collection"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/user {:account-role int? :collection-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [user_collections_result (users-by-collection/READ-BY-COLLECTION id)]
                (let [user_result (map #(remove-db-only %) user_collections_result)]
                  (if (= 0 (count user_result))
                    {:status 404
                     :body {:message "no users found for given collection"}}
                    {:status 200
                     :body user_result}))))})


(comment (def collection-get-all-users
           {:summary "Retrieves all users for the specified collection"
            :parameters {:path {:id uuid?}}
            :responses {200 {:body [models/user]}}
            :handler (fn [{{{:keys [id]} :path} :parameters}]
                       (let [user_collections_result (user_collections_assoc/READ-BY-COLLECTION id)]
                         (let [user_list (into [] (map (fn [res] (remove-db-only (users/READ (:user-id res)))) user_collections_result))]
                           {:status 200
                            :body user_list})))}))


(def content-create ;; Non-functional
  {:summary "Creates new content"
   :parameters {:body models/content_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 content created"
                           :id (get-id (contents/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message "unable to create content, likely bad collection id"
                             :error e}})))})

(def content-get-by-id
  {:summary "Retrieves specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/content}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [content_result (contents/READ id)]
                (if (= "" (:id content_result))
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body content_result})))})

(def content-update ;; Non-functional
  {:summary "Updates the specified content"
   :parameters {:path {:id uuid?} :body models/content_without_id}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (contents/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body {:message (str result " contents updated")}})))})

(def content-delete ;; Non-functional
  {:summary "Deletes the specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (contents/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested content not found"}}
                  {:status 200
                   :body {:message (str result " contents deleted")}})))})

(def content-connect-file ;; Non-functional
  {:summary "Connects specified file and content (bidirectional)"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})
(def content-get-all-files ;; Non-functional
  {:summary "Retrieves all files connected to specified content"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def content-add-view ;; Non-functional
  {:summary "Adds 1 view to specified content"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result "placeholder"]
                (if result
                  {:status 200
                   :body {:message "view successfully added"}}
                  {:status 404
                   :body {:message "requested content not found"}})))})

(def content-add-file
  {:summary "Adds file to specified content"
   :parameters {:path {:id uuid?} :body {:file_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result "placeholder"]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to add file"}}
                  {:status 200
                   :body {:message (str result " files added to content")}})))})


(def content-remove-file
  {:summary "Removes file from specified content"
   :parameters {:path {:id uuid?} :body {:file_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result "placeholder"]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove file"}}
                  {:status 200
                   :body {:message (str result " files removed from content")}})))})

(def annotation-create ;; Non-functional
  {:summary "Creates new annotation"
   :parameters {:body models/annotation_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 annotation created"
                           :id (get-id (annotations/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message "unable to create annotation, likely bad collection id"
                             :error e}})))})

(def annotation-get-by-id
  {:summary "Retrieves specified annotation"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/annotation}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [annotation_result (annotations/READ id)]
                (if (= "" (:id annotation_result))
                  {:status 404
                   :body {:message "requested annotation not found"}}
                  {:status 200
                   :body annotation_result})))})

(def annotation-update ;; Non-functional
  {:summary "Updates the specified annotation"
   :parameters {:path {:id uuid?} :body models/annotation_without_id}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (annotations/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested annotation not found"}}
                  {:status 200
                   :body {:message (str result " annotations updated")}})))})

(def annotation-delete ;; Non-functional
  {:summary "Deletes the specified annotation"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (annotations/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested annotation not found"}}
                  {:status 200
                   :body {:message (str result " annotations deleted")}})))})

(def course-create ;; Non-functional
  {:summary "Creates a new course"
   :parameters {:body models/course_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 course created"
                           :id (get-id (courses/CREATE body))}}
                   (catch Exception e
                     {:status 409
                      :body {:message (e)}})))})

(def course-get-by-id ;; Non-functional
  {:summary "Retrieves specified course"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/course}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (courses/READ id)]
                (if (= "" (:id result))
                  {:status 404
                   :body {:message "requested course not found"}}
                  {:status 200
                   :body result})))})

(def course-update ;; Non-functional
  {:summary "Updates the specified course"
   :parameters {:path {:id uuid?} :body models/course_without_id}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (courses/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested course not found"}}
                  {:status 200
                   :body {:message (str result " courses updated")}})))})

(def course-delete ;; Non-functional
  {:summary "Deletes the specified course"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (courses/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested course not found"}}
                  {:status 200
                   :body {:message (str result " courses deleted")}})))})

(def course-add-collection
  {:summary "Adds collection to specified course"
   :parameters {:path {:id uuid?} :body {:collection_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result "placeholder"]
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to add collection"}}
                  {:status 200
                   :body {:message (str result " collections added to course")}})))})


(def course-remove-collection
  {:summary "Removes collection from specified course"
   :parameters {:path {:id uuid?} :body {:collection_id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result "placeholder"] ;; needs adjustment
                (if (= 0 result)
                  {:status 404
                   :body {:message "unable to remove collection"}}
                  {:status 200
                   :body {:message (str result " collections removed from course")}})))})

(def course-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections connected to specified course"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def file-create
  {:summary "Creates a new file"
   :parameters {:body models/file_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              {:status 200
               :body {:message "1 file created"
                      :id (get-id (files/CREATE body))}})})



(def file-get-by-id
  {:summary "Retrieves specified file"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/file}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [file_result (files/READ id)]
                (if (= "" (:id file_result))
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body file_result})))})

(def file-update
  {:summary "Updates specified file"
   :parameters {:path {:id uuid?} :body models/file_without_id}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (files/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body {:message (str result " files updated")}})))})

(def file-delete
  {:summary "Deletes specified file"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (files/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body {:message (str result " files deleted")}})))})


(def file-get-all-contents ;; Non-functional
  {:summary "Retrieves all contents that use the specified file"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})
