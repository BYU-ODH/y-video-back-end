(ns usecase.stories.admin
  "General process of administration of the system"
  (:require [clojure.test :refer [deftest is testing]]))

(deftest manage-users
  (testing "Admin can CRUD users"
    (is false))
  (testing "Non-admin CANNOT CRUD users"
    (is false))
  (testing "Admin reset user passwords"
    (is false)))
