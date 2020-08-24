(ns y-video-back.routes.service-handlers.handlers.admin-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.users :as users]
   [y-video-back.db.courses :as courses]
   [y-video-back.models :as models]
   [y-video-back.routes.service-handlers.utils.utils :as utils]
   [y-video-back.db.core :as db]
   [y-video-back.utils.account-permissions :as ac]))
   ;[y-video-back.course-data :as cd-api]))

; TODO - sort results by more than just alphabetical


(comment
  (defn add-course-to-db
    [dpt course-data cur-sem]
    (println course-data)
    (let [sections (cd-api/get-class-sections dpt
                                              (get course-data "curriculum_id")
                                              (get course-data "title_code")
                                              cur-sem)
          res (map #(if-not (courses/EXISTS-DEP-CAT-SEC? dpt (get course-data "catalog_number") %)
                      (courses/CREATE {:department dpt
                                       :catalog-number (get course-data "catalog_number")
                                       :section-number %})
                      nil)
                   sections)]
      (count res)))

  (defn add-teaching-area-to-db
    [teaching-area cur-sem]
    (println (get teaching-area "department"))
    (let [dpt (get teaching-area "department")
          dpt-courses (cd-api/get-department-classes dpt cur-sem)
          res (map #(add-course-to-db dpt % cur-sem) dpt-courses)]
      (reduce + res)))

  (def refresh-course-list
    {:summary "Refreshes courses in database. DANGEROUS - NOT FUNCTIONAL."
     :permission-level "admin"
     :parameters {:header {:session-id uuid?}
                  :path {:password uuid?}}
     :responses {200 {:body {:message string?}}
                 401 {:body {:message string?}}}
     :handler (fn [{{{:keys [password]} :path} :parameters}]
                (println "password=" password)
                (if-not (= (:REFRESH-COURSES-PASSWORD env) (str password))
                  {:status 401
                   :body {:message "unauthorized"}}
                  (let [cur-sem (cd-api/get-current-sem)
                        teaching-areas (cd-api/get-teaching-areas cur-sem)
                        res (map #(add-teaching-area-to-db % cur-sem) teaching-areas)]
                    {:status 200
                     :body {:message (str (reduce + res) " courses added to db")}})))}))

(def search-by-user ;; Non-functional
  {:summary "Searches users, collections, resources, and courses by search term"
   :permission-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/user]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :users-undeleted
                                                  [:email :account-name :username]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})

(def search-by-collection ;; Non-functional
  {:summary "Searches users, collections, resources, and courses by search term"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [(into models/collection {:username string?})]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    coll-res (map utils/remove-db-only
                                  (db/read-all-pattern :collections-undeleted
                                                       [:collection-name]
                                                       (str "%" term "%")))
                    res (map #(into % {:username (:username (users/READ (:owner %)))})
                             coll-res)]
                {:status 200
                 :body res}))})

(def search-by-content
  {:summary "Searches users, collections, contents, and courses by search term"
   :permission-level "lab-assistant"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/content]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :contents-undeleted
                                                  [:title :content-type :url :description :tags :file-version]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})


(def search-by-resource
  {:summary "Searches users, collections, resources, and courses by search term"
   :permission-level "instructor"
   :parameters {:header {:session-id uuid?}
                :path {:term string?}}
   :responses {200 {:body [models/resource]}}
   :handler (fn [{{{:keys [term]} :path} :parameters}]
              (let [term (java.net.URLDecoder/decode term)
                    res (map utils/remove-db-only
                             (db/read-all-pattern :resources-undeleted
                                                  [:resource-name :resource-type :requester-email]
                                                  (str "%" term "%")))]
                {:status 200
                 :body res}))})
