(ns y-video-back.db.collections-tests
    (:require
      [clojure.test :refer :all]
      [y-video-back.db.core :as db]
      [y-video-back.db.collections :as collections]))

(deftest READ-PUBLIC
  (testing "Returns nil when no collection with id"
    (let [test-id "test"]
      (with-redefs-fn {#'collections/READ (fn [id]
                                              (is (= test-id id))
                                              nil)}
        #(is (nil? (collections/READ-PUBLIC test-id))))))
  (testing "Returns nil when no public collection with id"
    (let [test-id "test"]
      (with-redefs-fn {#'collections/READ (fn [id]
                                              (is (= test-id id))
                                              {:public false})}
        #(is (nil? (collections/READ-PUBLIC test-id)))))))


(deftest READ-ALL-PUBLIC
  (testing "Sends correct parameters to db function"
    (let [db-res "db-res"]
      (with-redefs-fn {#'db/read-all-where (fn [table field val]
                                             (is (= :collections-undeleted table))
                                             (is (= :public field))
                                             (is (= true val))
                                             db-res)}
        #(is (= db-res (collections/READ-ALL-PUBLIC)))))))
