(ns y-video-back.routes.search-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model_generator :as g]
      [y-video-back.utils.route_proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.annotations :as annotations]
      [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.content-files-assoc :as content_files_assoc]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user_collections_assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE {:email "im.a.what@gmail.com"
                                                        :last-login "11 years ago"
                                                        :account-name "Harry Potter"
                                                        :account-type 0
                                                        :username "1-flyboy"})))
  (def test-user-two (ut/under-to-hyphen (users/CREATE {:email "another-weasley@gmail.com"
                                                        :last-login "yesterday"
                                                        :account-name "Ron Weasley"
                                                        :account-type 1
                                                        :username "King-1-Weasley"})))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE {:email "insufferable_know_it_all@byu.edu"
                                                        :last-login "today"
                                                        :account-name "Hermione Granger"
                                                        :account-type 0
                                                        :username "future-minister-1"})))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE {:collection-name "Quidditch Books"
                                                              :published true
                                                              :archived false})))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE {:collection-name "Self-help Books"
                                                              :published true
                                                              :archived true})))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE {:collection-name "Non-fiction Books"
                                                              :published false
                                                              :archived true})))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE {:content-name "Quidditch Through the Ages"
                                                           :content-type "book 1"
                                                           :requester-email "im.a.what@gmail.com"
                                                           :thumbnail "thumbs"
                                                           :copyrighted true
                                                           :physical-copy-exists false
                                                           :full-video true
                                                           :published false
                                                           :allow-definitions true
                                                           :allow-notes false
                                                           :allow-captions false
                                                           :date-validated "a while ago"
                                                           :views 0
                                                           :metadata "so meta"})))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE {:content-name "Twelve Fail-Safe Ways to Charm Witches"
                                                           :content-type "book 2"
                                                           :requester-email "another-weasley@gmail.com"
                                                           :thumbnail "thumbs"
                                                           :copyrighted false
                                                           :physical-copy-exists true
                                                           :full-video false
                                                           :published true
                                                           :allow-definitions false
                                                           :allow-notes false
                                                           :allow-captions true
                                                           :date-validated "recently"
                                                           :views 0
                                                           :metadata "so meta"})))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE {:content-name "Hogwarts: A History"
                                                           :content-type "book 2"
                                                           :requester-email "insufferable_know_it_all@byu.edu"
                                                           :thumbnail "thumbs"
                                                           :copyrighted false
                                                           :physical-copy-exists false
                                                           :full-video false
                                                           :published false
                                                           :allow-definitions false
                                                           :allow-notes false
                                                           :allow-captions false
                                                           :date-validated "every day"
                                                           :views 0
                                                           :metadata "so meta"})))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE {:department "Transfiguration"
                                                          :catalog-number "ClockAndMap 101"
                                                          :section-number "001"})))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE {:department "Potions"
                                                          :catalog-number "UWillFail 101"
                                                          :section-number "001"})))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE {:department "Charms"
                                                          :catalog-number "CouldveBeenDualing 101"
                                                          :section-number "001"})))
  (mount.core/start #'y-video-back.handler/app))

(defn test-search-table
  [table-key query_term expected_users]
  (let [res (rp/search query_term)]
    (is (= 200 (:status res)))
    (is (= (map #(update (ut/remove-db-only %) :id str) expected_users)
           (table-key (m/decode-response-body res))))))

(deftest test-search-users
  (testing "all users email"
    (test-search-table :users
                       "@"
                       [test-user-one test-user-two test-user-thr]))
  (testing "one user email"
    (test-search-table :users
                       "im.a.what"
                       [test-user-one]))
  (testing "no users email"
    (test-search-table :users
                       "@yahoo.com"
                       []))
  (testing "all users name"
    (test-search-table :users
                       "o"
                       [test-user-one test-user-two test-user-thr]))
  (testing "one user name"
    (test-search-table :users
                       "Ron W"
                       [test-user-two]))
  (testing "no users name"
    (test-search-table :users
                       "Malfoy"
                       []))
  (testing "all users username"
    (test-search-table :users
                       "1"
                       [test-user-one test-user-two test-user-thr]))
  (testing "one user username"
    (test-search-table :users
                       "minister"
                       [test-user-thr]))
  (testing "no users username"
    (test-search-table :users
                       "buckbeak"
                       [])))
(deftest test-search-collections
  (testing "all colls name"
    (test-search-table :collections
                       "Books"
                       [test-coll-one test-coll-two test-coll-thr]))
  (testing "one coll name"
    (test-search-table :collections
                       "fiction"
                       [test-coll-thr]))
  (testing "no colls name"
    (test-search-table :collections
                       "Movies"
                       [])))
(deftest test-search-contents
  (testing "all conts name"
    (test-search-table :contents
                       "i"
                       [test-cont-one test-cont-two test-cont-thr]))
  (testing "one cont name"
    (test-search-table :contents
                       "elve"
                       [test-cont-two]))
  (testing "no conts name"
    (test-search-table :contents
                       "Fantastic Beasts"
                       []))
  (testing "all conts type"
    (test-search-table :contents
                       "book"
                       [test-cont-one test-cont-two test-cont-thr]))
  (testing "one cont type"
    (test-search-table :contents
                       "1"
                       [test-cont-one]))
  (testing "no conts type"
    (test-search-table :contents
                       " movie "
                       []))
  (testing "all conts requester-email"
    (test-search-table :contents
                       "l"
                       [test-cont-one test-cont-two test-cont-thr]))
  (testing "one cont requester-email"
    (test-search-table :contents
                       "what"
                       [test-cont-one]))
  (testing "no conts requester-email"
    (test-search-table :contents
                       "@gmail@com"
                       [])))