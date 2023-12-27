(ns y-video-back.semesters-test
  (:require [y-video-back.semesters :as subj]
            [clojure.test :refer [deftest is testing]]))

(deftest extract-term
  (is (= 5 (subj/extract-term 20195)))
  (is (= 5 (subj/extract-term "20195"))))

(deftest extract-year
  (is (= 2019 (subj/extract-year 20195)))
  (is (= 2019 (subj/extract-year "20195"))))

(deftest wrap-around
  (let [T subj/TNUM]
    (testing "Pos non-wrap"
      (is (= {:value 1 :times 0} (subj/wrap-around T 0)))
      (is (= {:value 3 :times 0} (subj/wrap-around T 1)))
      (is (= {:value 4 :times 0} (subj/wrap-around T 2)))
      (is (= {:value 5 :times 0} (subj/wrap-around T 3))))
    (testing "Pos wrap"
      (is (= {:value 1 :times 1} (subj/wrap-around T 4)))
      (is (= {:value 3 :times 1} (subj/wrap-around T 5)))
      (is (= {:value 4 :times 1} (subj/wrap-around T 6)))
      (is (= {:value 5 :times 1} (subj/wrap-around T 7)))
      (is (= {:value 1 :times 2} (subj/wrap-around T 8))))
    (testing "neg non-wrap"
      (is (= {:value 1 :times 0} (subj/wrap-around T -0)))
      (is (= {:value 5 :times -1} (subj/wrap-around T -1)))
      (is (= {:value 4 :times -1} (subj/wrap-around T -2)))
      (is (= {:value 3 :times -1} (subj/wrap-around T -3))))
    (testing "neg wrap"
      (is (= {:value 1 :times -1} (subj/wrap-around T -4)))
      (is (= {:value 5 :times -2} (subj/wrap-around T -5)))
      (is (= {:value 4 :times -2} (subj/wrap-around T -6)))
      (is (= {:value 3 :times -2} (subj/wrap-around T -7)))
      (is (= {:value 1 :times -2} (subj/wrap-around T -8))))))


(deftest add-yt
  (testing "Adding"
    (testing "add zero"
      (is (= "20191" (subj/add-yt "20191" 0))))
    (testing "add zero to xxxx3"
      (is (= "20193" (subj/add-yt "20193" 0))))
    (testing "add zero to xxxx4"
      (is (= "20194" (subj/add-yt "20194" 0))))
    (testing "add zero to xxxx5"
      (is (= "20195" (subj/add-yt "20195" 0))))
    (testing "add one x1"
      (is (= "20193" (subj/add-yt "20191" 1))))
    (testing "add two x1"
      (is (= "20194" (subj/add-yt "20191" 2))))
    (testing "add three x1"
      (is (= "20195" (subj/add-yt "20191" 3))))
    (testing "add four (year) x1"
      (is (= "20201" (subj/add-yt "20191" 4))))
    (testing "add zero to xxxx3"
      (is (= "20193" (subj/add-yt "20193" 0))))
    (testing "add one x1"
      (is (= "20194" (subj/add-yt "20193" 1))))
    (testing "add two x1"
      (is (= "20195" (subj/add-yt "20193" 2))))
    (testing "add three x1"
      (is (= "20201" (subj/add-yt "20193" 3))))
    (testing "add four (year) x1"
      (is (= "20203" (subj/add-yt "20193" 4)))))
  (testing "Equivalences"
    (is (= (subj/add-yt "20191" 1)
           (subj/add-yt "20193" 0)))
    (is (= (subj/add-yt "20191" 2)
           (subj/add-yt "20194" 0)))
    (is (= (subj/add-yt "20193" 0)
           (subj/add-yt "20194" -1))))
  (testing "Subtracting"
    (testing "minus zero"
      (is (= "20191" (subj/add-yt "20191" -0))))
    (testing "minus one x1"
      (is (= "20185" (subj/add-yt "20191" -1))))
    (testing "minus two x1"
      (is (= "20184" (subj/add-yt "20191" -2))))
    (testing "minus three x1"
      (is (= "20183" (subj/add-yt "20191" -3))))
    (testing "minus four (year) x1"
      (is (= "20181" (subj/add-yt "20191" -4))))))

