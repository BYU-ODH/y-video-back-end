(ns y-video-back.db.email-logs-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.db.core :as db]
      [y-video-back.db.email-logs :as email-logs]))

(deftest EXISTS?
  (testing "Email log with id does exist"
    (let [test-id "test-id"
          valid-res {:subject "spoof email"}]
      (with-redefs-fn {#'db/READ (fn [table id]
                                     (is (= :email-logs-undeleted table))
                                     (is (= test-id id))
                                     valid-res)}
        #(is (= true (email-logs/EXISTS? test-id))))))
  (testing "Email log with id does not exist"
    (let [test-id "test-id"
          nil-res nil]
      (with-redefs-fn {#'db/READ (fn [table id]
                                     (is (= :email-logs-undeleted table))
                                     (is (= test-id id))
                                     nil-res)}
        #(is (= false (email-logs/EXISTS? test-id)))))))
