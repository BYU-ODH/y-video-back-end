(ns y-video-back.app
  (:require [y-video-back.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
