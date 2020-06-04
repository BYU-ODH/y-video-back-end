(ns y-video-back.routes.service_handlers.course_handlers
  (:require
   [y-video-back.db.courses :as courses]
   [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))

(def course-create ;; Non-functional
  {:summary "Creates a new course"
   :parameters {:body models/course_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              (try {:status 200
                    :body {:message "1 course created"
                           :id (utils/get-id (courses/CREATE body))}}
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
   :parameters {:path {:id uuid?} :body ::sp/course}
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
  {:summary "Retrieves all collections for specified course"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/collection {:course-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [course_collections_result (collection_courses_assoc/READ-COLLECTIONS-BY-CONTENT id)]
                (let [collection_result (map #(utils/remove-db-only %) course_collections_result)]
                  (if (= 0 (count collection_result))
                    {:status 404
                     :body {:message "no courses found for given collection"}}
                    {:status 200
                     :body collection_result}))))})
