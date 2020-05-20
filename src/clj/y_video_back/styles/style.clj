;; -*- eval: (rainbow-mode) -*-
(ns y-video-back.styles.style
  (:require [garden.def :refer [defstylesheet defstyles defkeyframes]]
            [garden.selectors :as sel]
            [garden.units :as u :refer [px em rem percent]]
            [garden.color :as c :refer [hex->hsl hsl->hex hsl hsla]]))

;;;;;;;;;;;;;;;;;;;;;
;; Style Variables ;;
;;;;;;;;;;;;;;;;;;;;;
(def paper-ink (hex->hsl "#234343"))

(def navbar
  [:#nav
   [:.navbar-item.active {:font-weight 600
                          :border-style "solid"
                          :border-width (px 2)
                          :pointer-events "none"}]])

(def modal
  [:#modal {}])

(def footer
  [:#footer {}])

(defstyles y-video-back
  [:html :body
   {:background "#fff"}
   [:.box {:border-style "solid"
           :border-width (px 1)
           :border-color "#000"}
    [:h2 {:font-weight "600"
          :font-size (em 1.2)}]]
   [:#app {:color paper-ink}
    [:.title {:color paper-ink}]]]
  navbar
  modal
  footer)
