(ns y-video-back.db.user-type-exceptions-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.db.core :as db]
      [y-video-back.db.user-type-exceptions :as user-type-exceptions]))

(deftest EXISTS?
  (testing "user-type-exceptions with id does exist"
    (let [test-id "test-id"
          valid-res {:temp "spoof user-type-exception"}]
      (with-redefs-fn {#'db/READ (fn [table id]
                                     (is (= :user-type-exceptions-undeleted table))
                                     (is (= test-id id))
                                     valid-res)}
        #(is (= true (user-type-exceptions/EXISTS? test-id))))))
  (testing "user-type-exceptions with id does not exist"
    (let [test-id "test-id"
          nil-res nil]
      (with-redefs-fn {#'db/READ (fn [table id]
                                     (is (= :user-type-exceptions-undeleted table))
                                     (is (= test-id id))
                                     nil-res)}
        #(is (= false (user-type-exceptions/EXISTS? test-id)))))))
