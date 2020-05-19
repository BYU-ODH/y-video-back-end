(ns y-video-postgres-swagger.routes.service_handlers
  (:require
    [y-video-postgres-swagger.dbaccess.access :as db-access]))


(def user-create ;; Non-functional
  {:summary "Creates a new user - FOR DEVELOPMENT ONLY"
   :parameters {:body {:id string? :name string? :published boolean? :archived boolean?
                       :assoc_courses [{:id string? :department string? :catalog_number string? :section_number string?}]
                       :assoc_users [{:id string? :role int?}]
                       :assoc_content [{:id string? :name string? :thumbnail string? :published boolean?
                                        :allow_definitions boolean? :allow_notes boolean? :allow_captions string?}]}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id name published archived]} :body} :parameters}]
             {:status 200
              :body (db-access/add_collection id name published archived)})})

(def user-get-by-id ;; Non-functional
  {:summary "Retrieves the current logged-in user"
   :parameters {:query {:user_id string?}}
   :responses {200 {:body {:user_id string? :email string? :lastlogin string? :name string? :role int? :username string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [user_id]} :query} :parameters}]
             (let [user_result (db-access/get_user user_id)]
              (if (nil? user_result)
                {:status 404
                 :body {:message "requested user not found"}}
                {:status 200
                 :body user_result})))})

(def user-update ;; Non-functional
  {:summary "Updates specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-delete ;; Non-functional
  {:summary "Deletes specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections for specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-word-create
  {:summary "Creates new word under the specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-word-get-by-id
  {:summary "Retrieves the specified word under specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-word-update
  {:summary "Updates specified word under specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-word-delete
  {:summary "Deletes specified word under specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def user-get-all-words
  {:summary "Retrieves all words under specified user"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def collection-get-all-by-user ;; Non-functional
  {:summary "Retrieves collection info for all collections available to the current user_id"
   :parameters {}
   :responses {200 {:body [{:collection_id string? :name string? :published boolean? :archived boolean?}]}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [user_id]} :query} :parameters}]
             (let [collection_result (db-access/get_collections user_id)]
              (if (nil? collection_result)
                {:status 404
                 :body {:message "requested collection not found"}}
                {:status 200
                 :body collection_result})))})

(def collection-create ;; Non-functional
  {:summary "Creates a new collection with the current user as an owner"
   :parameters {:body {:name string?}}
   :responses {200 {:body {:message string?
                           :success boolean?}}}
   :handler (fn [{{{:keys [current_user_id name published archived]} :body} :parameters}]
             {:status 200
              :body (db-access/add_collection current_user_id name published archived)})})

(def collection-get-by-id ;; Non-functional
  {:summary "Retrieves the specified collection"
   :parameters {:body {:collection_id string?}}
   :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [collection_id]} :query} :parameters}]
             (let [collection_result (db-access/get_collection collection_id)]
              (if (nil? collection_result)
                {:status 404
                 :body {:message "requested collection not found"}}
                {:status 200
                 :body collection_result})))})

(def collection-update ;; Non-functional
  {:summary "Updates the specified collection"
   :parameters {:body {:name string? :published boolean? :archived boolean?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def collection-delete ;; Non-functional
  {:summary "Deletes the specified collection"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def collection-add-user
  {:summary "Adds user to specified collection"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def collection-get-all-contents ;; Non-functional
  {:summary "Retrieves all the content for the specified collection"
   :parameters {:query {:collection_id string?}}
   :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [collection_id]} :query} :parameters}]
             (let [collection_result (db-access/get_collections collection_id)]
              (if (nil? collection_result)
                {:status 404
                 :body {:message "requested collection not found"}}
                {:status 200
                 :body collection_result})))})

(def collection-get-all-courses
  {:summary "Retrieves all courses for the specified collection"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def collection-get-all-users
  {:summary "Retrieves all users for the specified collection"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def course-create ;; Non-functional
  {:summary "Creates new course"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def course-get-by-id ;; Non-functional
  {:summary "Retrieves the specified course"
   :parameters {:query {:collection_id string?}}
   :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [collection_id]} :query} :parameters}]
             (let [collection_result (db-access/get_collections collection_id)]
              (if (nil? collection_result)
                {:status 404
                 :body {:message "requested collection not found"}}
                {:status 200
                 :body collection_result})))})

(def course-update ;; Non-functional
  {:summary "Updates the specified course"
   :parameters {:body {:id string? :name string? :published boolean? :archived boolean?
                       :assoc_courses [{:id string? :department string? :catalog_number string? :section_number string?}]
                       :assoc_users [{:id string? :role int?}]
                       :assoc_content [{:id string? :name string? :thumbnail string? :published boolean?
                                        :allow_definitions boolean? :allow_notes boolean? :allow_captions string?}]}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id name published archived]} :body} :parameters}]
             {:status 200
              :body (db-access/add_collection id name published archived)})})

(def course-delete ;; Non-functional
  {:summary "Deletes the specified course"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def course-get-all-collections ;; Non-functional
  {:summary "Retrieves all collections connected to specified course"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})


(def content-create ;; Non-functional
  {:summary "Creates new content"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def content-get-by-id ;; Non-functional
  {:summary "Retrieves the specified content"
   :parameters {:query {:collection_id string?}}
   :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [collection_id]} :query} :parameters}]
             (let [collection_result (db-access/get_collections collection_id)]
              (if (nil? collection_result)
                {:status 404
                 :body {:message "requested collection not found"}}
                {:status 200
                 :body collection_result})))})

(def content-update ;; Non-functional
  {:summary "Updates the specified content"
   :parameters {:body {:id string? :name string? :published boolean? :archived boolean?
                       :assoc_courses [{:id string? :department string? :catalog_number string? :section_number string?}]
                       :assoc_users [{:id string? :role int?}]
                       :assoc_content [{:id string? :name string? :thumbnail string? :published boolean?
                                        :allow_definitions boolean? :allow_notes boolean? :allow_captions string?}]}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id name published archived]} :body} :parameters}]
             {:status 200
              :body (db-access/add_collection id name published archived)})})

(def content-delete ;; Non-functional
  {:summary "Deletes the specified content"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})
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


(def file-create ;; Non-functional
  {:summary "Creates new file"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def file-get-by-id ;; Non-functional
  {:summary "Retrieves the specified file"
   :parameters {:query {:collection_id string?}}
   :responses {200 {:body {:collection_id string? :name string? :published boolean? :archived boolean?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [collection_id]} :query} :parameters}]
             (let [collection_result (db-access/get_collections collection_id)]
              (if (nil? collection_result)
                {:status 404
                 :body {:message "requested collection not found"}}
                {:status 200
                 :body collection_result})))})

(def file-update ;; Non-functional
  {:summary "Updates the specified file"
   :parameters {:body {:id string? :name string? :published boolean? :archived boolean?
                       :assoc_courses [{:id string? :department string? :catalog_number string? :section_number string?}]
                       :assoc_users [{:id string? :role int?}]
                       :assoc_file [{:id string? :name string? :thumbnail string? :published boolean?
                                        :allow_definitions boolean? :allow_notes boolean? :allow_captions string?}]}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id name published archived]} :body} :parameters}]
             {:status 200
              :body (db-access/add_collection id name published archived)})})

(def file-delete ;; Non-functional
  {:summary "Deletes the specified file"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def file-get-all-contents ;; Non-functional
  {:summary "Retrieves all contents that use the specified file"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})


(def connect-collection-and-course ;; Non-functional
  {:summary "Connects specified collection and course (bidirectional)"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})

(def search-by-term ;; Non-functional
  {:summary "Searches users, collections, and content by search term"
   :parameters {}
   :responses {200 {:body {:message string?}}}
   :handler (fn [args] {:status 200
                        :body {:message "placeholder"}})})
