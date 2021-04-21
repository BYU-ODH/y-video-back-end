(ns y-video-back.layout
  (:require
   [hiccup.page :as hp]
   [hiccup.element :refer [javascript-tag]]
   [ring.util.http-response :refer [ok] :as ru]
   [selmer.parser :as parser]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
   [ring.util.http-response :refer [content-type ok]]
   [selmer.filters :as filters]))


(parser/set-resource-path!  (clojure.java.io/resource "html"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))


(declare ^:dynamic *app-context*)
(def style-path "/css/")
(def script-path "/js/")
(def assets-path "/assets/")
(def images-path "/images")

(defn context-path [& path]
  (apply str path))

(defn anti-forgery-element []
 [:input {:id "token" :value *anti-forgery-token* :type "hidden"}
  (javascript-tag (str  "var csrfToken = '" *anti-forgery-token* "'"))])

(defn include-byu-deps []
  (hp/include-css "https://cdn.byu.edu/byu-theme-components/latest/byu-theme-components.min.css")
  (hp/include-js  "https://cdn.byu.edu/byu-theme-components/latest/byu-theme-components.min.js"))

(defn top-matter
  "Topmatter including the `head` element and css calls"
  [& [_ #_userinfo]]
  [:head [:title "y-video-back"]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1.0"}]
   (javascript-tag (str "var context = ''"))
   (hp/include-css
    (context-path assets-path "bulma/css/bulma.css")
    (context-path assets-path "font-awesome/css/all.min.css")
    (context-path style-path "style.css"))])

(defn cljs-app-modal []
  [:div#modal])

(defn cljs-app-base []
  [:div#app
   [:div.container
    [:h1.title "Welcome to the swagger API for Y-Video"]
    [:h1.title [:a {:href "api/ping"} "ping"]]
    [:h1.title [:a {:href "api/docs/"} "docs"]]]])

(defn cljs-app-footer []
  [:div#footer
   [:div.footer]])

(defn boiler-plate []
  [:div#boiler-wrapper])
   ;; styles


(defn cljs-includes []
  [:div
   ;; BELOW needs to be altered to serve any appropriate front-end
   (hp/include-js (context-path script-path "app.js?rnd=" (rand-int 1000)))
   [:script {:type "text/javascript"} "goog.require('y-video-back.app')"]])

(defn cljs-app-navbar
  "The navbar"
  []
  [:navbar#nav])

(defn hiccup-render-cljs-base
  "Hiccup rendering (no traditional template)"
  [& [userinfo]]
  (ru/content-type
   (ok
    (hp/html5
     (top-matter userinfo)
     (anti-forgery-element)
     (cljs-app-modal)
     (cljs-app-navbar)
     (cljs-app-base)
     (cljs-app-footer)
     (cljs-includes))) ;; it makes a big difference to make sure the clojurescript is included last, so the DOM is rendered
   "text/html; charset=utf-8"))

(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)
   :image - image to be displayed (optional)
   :caption - caption for image (optional)
   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"
             "session-id" nil}
   :body (hp/html5
          (top-matter)
          (boiler-plate)
          [:div {:style "margin-top: 10px;" :class "columns is-centered has-text-centered"}
           [:div {:class "column is-half"}
            [:div.alert.alert-warning]
            [:h1 {:class "title"} (or (:title error-details) (str "Error " (:status error-details)))]
            [:div [:img {:class "is-centered" :src (str (:image error-details))}]]
            [:div [:h1.title (:caption error-details)]]
            [:div.error-details (:message error-details)]]])})

(defn receipt-page
  ([]
   (ru/content-type
    (ok
     (hp/html5
      [:div#receipt
       [:h1 "You submission was received"]]))
    "text/html; charset=utf-8"))
  ([application]
   (ru/content-type
    (ok
     (hp/html5
      [:div#receipt
       [:h1 "Your submission was received: "]
       [:div.content (str application)]]))
    "text/html; charset=utf-8")))

; This is where the front end pages are rendered

(filters/add-filter! :string? string?)
(filters/add-filter! :get-i (fn [data i]
                              (get (vec data) (Integer/parseInt i))))
(filters/add-filter! :get-k (fn [data i]
                              ((keyword i) data)))

(defn render
  "renders the HTML template located relative to resources/html"
  [request template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :session-id (:session-id request)
          :page template
          :csrf-token *anti-forgery-token*)))
    "text/html; charset=utf-8"))
