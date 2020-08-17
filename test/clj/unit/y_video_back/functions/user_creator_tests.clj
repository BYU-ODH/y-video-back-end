(ns y-video-back.functions.user-creator-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]
      [y-video-back.user-creator :as uc]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (mount.core/start #'y-video-back.handler/app))

(deftest test-user-exists
  (testing "username already in db"
    (let [res (uc/get-session-id (:username test-user-one) false)]
      (is (not (nil? res))))
    (let [res (uc/get-session-id (:username test-user-two) false)]
      (is (not (nil? res))))
    (let [res (uc/get-session-id (:username test-user-one) false)]
      (is (not (nil? res))))))

(deftest test-user-not-exists
  (testing "username already in db"
    (let [res (uc/get-session-id "other-username!" false)]
      (is (not (= (:id test-user-one) res)))
      (let [res-two (uc/get-session-id "other-username!" false)]
        (is (not (nil? res)))))))
