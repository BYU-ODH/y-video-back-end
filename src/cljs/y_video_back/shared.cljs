(ns y-video-back.shared
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rfc]
            [goog.string :as gstring]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;
;; Javascript Interop ;;
;;;;;;;;;;;;;;;;;;;;;;;;
(defn jslog [s]
  (.log js/console s))

;;;;;;;;;;;;;;;;;;
;; MISC Helpers ;;
;;;;;;;;;;;;;;;;;;

(defn sjoin [seq] (str/join " " seq))

(defn get-value-from-change [e]
  (.. e -target -value))

(defn assoc-lazy [seq n val]
  (let [post (inc n)]
    (concat (take n seq) (cons val (drop post seq)))))

(defn go-to [url]
  (set! (.-location js/window) url))

(defn delete-nth [seq n]
  (concat (take (dec n) seq) (drop n seq)))

(def alphanum-sort-all
  (let [collator (Intl.Collator.
                  js/undefined #js {:numeric true :sensitivity "base"})]
    (fn [coll & [reverse?]]
      (let [r (if reverse? reverse identity)]
        (into [] (r
                  (.sort (clj->js coll)
                         (.-compare collator))))))))

(defn alphanum-compare
  [a b & [reverse?]]
  (let [r (if reverse? -1 1)]
    (* r (.localeCompare (clj->js a)
                         b js/undefined #js
                         {:numeric true :sensitivity "base"}))))

(defn err-boundary
  [& children]
  (let [err-state (r/atom nil)]
    (r/create-class
      {:display-name "ErrBoundary"
       :component-did-catch (fn [err info]
                              (reset! err-state [err info]))
       :reagent-render (fn [& children]
                         (if (nil? @err-state)
                           (into [:<>] children)
                           (let [[_ info] @err-state]
                             [:pre [:code (pr-str info)]])))})))

(defn admin? ;; TODO
  "Determine if the current session's user is an admin"
  []
  true)

(defn routify-keyword
  "Given an un-namespaced keyword or string, namespace it into a routes keyword (probably for matching purposes)"
  [kw]
  (keyword "y-video-back.routes" kw))

(defn with-precision
  "Round numbers to a decimal point"
  [precision n]
  (gstring/format (str "%." precision "f")
                 n))

;(with-precision 2 1.020039)
