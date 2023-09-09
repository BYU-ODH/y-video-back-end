(ns y-video-back.db.auth-tokens-test
  (:require
   [clojure.test :refer [use-fixtures deftest is testing]]
   [mount.core :as mount]
   [y-video-back.db.auth-tokens :as subj]
   [legacy.utils.route-proxy.proxy :as rp]
   [legacy.db.test-util :as tcore]
   [legacy.utils.db-populator :as db-pop]
   [tick.alpha.api :as t])
  )

(tcore/basic-transaction-fixtures
 (mount.core/start)
 (def user-one (db-pop/add-user "admin"))
 (def res (rp/login-current-user "test"))
 (def auth-token (subj/CREATE {:user-id (:id user-one)}))
 (def auth-token-id (:id auth-token)))

(deftest READ-UNEXPIRED
  (testing "No token"
    (is (nil? (subj/READ-UNEXPIRED nil)) "No token given does nothing"))  
  (testing "Good token passes"
    (is (= (:user-id (subj/READ-UNEXPIRED auth-token-id))
           (:id user-one))))
  (testing "Expired token"
    (let [count-tokens-now #(count (subj/READ nil [:id]))
          orig-count (count-tokens-now)]
      (is false "deals with an expired token")
      (is (= (dec orig-count) (count-tokens-now)) "Old token deleted"))
    #_(let [before-timeout nil
            expired-token (update auth-token-id :created)]
        (is (nil? (subj/READ-UNEXPIRED expired-token)))))
  )
