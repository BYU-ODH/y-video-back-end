(ns y-video-back.db.core-test
  "Entry database testing namespace, testing basic functions and providing functions for testing"
  (:require [y-video-back.db.core :refer [*db*] :as db]
            [y-video-back.db.test-util :as tcore]
            [y-video-back.handler :refer [app]]
            [mount.core]
            [y-video-back.db.test-data :as td]
            [clojure.test :refer [deftest is testing]]))

(tcore/basic-transaction-fixtures
 (mount.core/start #'y-video-back.handler/app))

