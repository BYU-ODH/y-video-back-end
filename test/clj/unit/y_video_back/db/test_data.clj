(ns y-video-back.db.test-data
  "Entry database testing namespace, testing basic functions and providing functions for testing"
  (:require [y-video-back.db.test-util :as tu]
            [y-video-back.handler :refer[app]]
            [mount.core]))

(tu/basic-transaction-fixtures
 (mount.core/start #'y-video-back.handler/app))

(def capability-map
  {:id 999999,
   :name "add test",
   :deleted nil,
   :info nil,
   :created #inst "2019-08-30T18:20:32.360-00:00",
   :updated #inst "2019-08-30T18:20:32.360-00:00",
   :updated-by 12})

(def capability-link-map {:id 999999, :capability-id 999999, :user-id 999999})

(def category-map
  {:id 999999,
   :name "Test core.clj ",
   :description "delete me",
   :deleted nil,
   :info nil,
   :created #inst "2019-08-30T18:20:05.570-00:00",
   :updated #inst "2019-08-30T18:20:05.570-00:00",
   :updated-by 12})

(def category-link-map
  {:id 999999, :job-id 999999, :category-id 999999})

(def client-map
  {:deleted nil,
   :department-id 999999,
   :email "test",
   :last-name "test",
   :building "test",
   :phone "",
   :updated #inst "2019-09-03T16:46:19.089-00:00",
   :created #inst "2019-09-03T16:46:19.089-00:00",
   :first-name "test",
   :title "test",
   :updated-by nil,
   :id 999999,
   :info nil,
   :room "0000",
   :memo ""})

(def department-map
  {:deleted nil,
   :short-name "Test",
   :password "",
   :updated #inst "2019-09-03T16:46:19.037-00:00",
   :name "Test",
   :created #inst "2019-09-03T16:46:19.037-00:00",
   :login "",
   :updated-by nil,
   :id 999999,
   :info nil})

(def device-map
  {:access-device nil,
   :deleted nil,
   :serial-number "",
   :make "Dell",
   :client-id 999999,
   :service-code "",
   :updated #inst "2019-09-03T16:46:19.252-00:00",
   :purchase-requisition "124320",
   :created #inst "2019-09-03T16:46:19.252-00:00",
   :updated-by nil,
   :id 999999,
   :purchase-order "999999",
   :info nil,
   :surplused #inst "2001-01-01T07:00:00.000-00:00",
   :available true,
   :purchase-date #inst "2001-05-01T06:00:00.000-00:00",
   :icn "999999",
   :memo "",
   :model "test"})

(def email-addresses-map
  {:id 999999,
   :email-address "scott.griffin1700@byu.edu",
   :deleted nil,
   :info nil,
   :created #inst "2019-09-03T16:47:00.184-00:00",
   :updated #inst "2019-09-03T16:47:00.184-00:00",
   :updated-by nil})

(def email-links-map
  {:id 999999,
   :client-id 999999,
   :created #inst "2019-09-03T16:47:00.300-00:00",
   :email-addresses-id 999999,
   :updated #inst "2019-09-03T16:47:00.300-00:00",
   :updated-by nil,
   :deleted nil})

(def email-logs
  {:deleted nil,
   :updated #inst "2019-07-30T15:55:45.000-00:00",
   :created #inst "2019-07-30T15:55:45.000-00:00",
   :recipients ["scott.griffin@byu.edu" "Scott.griffin@byu.net"],
   :updated-by 12,
   :sent-date #inst "2019-07-30T15:55:45.000-00:00",
   :id 999999,
   :sender "scott.griffin1700@gmail.com",
   :info nil,
   :subject "email testing",
   :message "This is the initial test of sending emails."})

(def email-templates
  {:id 999999,
   :subject "Device Due Date Reminder",
   :email-content
   "(DAY OF WEEK, MONTH, DAY, YEAR)\n\nOFFICE OF DIGITAL HUMANITIES\n\n(Client First-name) (Client Last-name)\n(Client Room Number)\n(Building)\n(Phone Number)\n\n\n\n\nDEVICE DUE DATE REMINDER:\nOffice of Digital Humanites Device Circulation\ncsr-email@byu.edu\n\nPlease return or renew the following items by the listed due date. Renew online\n(https://odh-devices.byu.edu/account/) or at the ODH Device Circulation desk. (801-422-9999)\n\nBe aware some items cannot be renewed.\n\nThank you.\n\nODH Device Circulation\n\n1  due: mm/dd/yyyy,hh:mm am/pm\nMake: ()\nModel: ()\nICN: ()\nBarcode: ()\nSerial-Number: ()\nMemo: ()",
   :deleted nil,
   :info nil,
   :created #inst "2019-09-03T16:47:00.239-00:00",
   :updated #inst "2019-09-03T16:47:00.239-00:00",
   :updated-by nil})

(def history-map
  {:deleted nil,
   :job-id 999999,
   :updated #inst "2019-09-03T16:46:51.438-00:00",
   :created #inst "2019-09-03T16:46:51.438-00:00",
   :user-id 999999,
   :title "hotel",
   :id 999999,
   :info nil,
   :time-stamp #inst "2004-07-26T21:33:44.000-00:00",
   :memo "his hotel denies his access"})

(def job-map
  {:deleted nil,
   :exclusive false,
   :web-team false,
   :client-id 999999,
   :device-id 999999,
   :updated #inst "2019-09-03T16:46:20.555-00:00",
   :created #inst "2019-09-03T16:46:20.555-00:00",
   :user-id 999999,
   :resolved true,
   :title "Extensity",
   :updated-by nil,
   :priority "normal",
   :id 999999,
   :info nil,
   :kbmember false,
   :time-stamp #inst "2004-07-26T21:18:57.000-00:00",
   :memo "needs help using mels system for extensity",
   :complete-stamp #inst "2004-07-29T18:49:56.000-00:00"})

(def memo-map
  {:deleted nil,
   :from-id 999999,
   :unread true,
   :updated #inst "2019-09-03T16:46:36.361-00:00",
   :created #inst "2019-09-03T16:46:36.361-00:00",
   :user-id 999999,
   :updated-by 999999,
   :priority "Low",
   :id 999999,
   :info nil,
   :reply-to "Devin Asay <devin_asay@BYU.EDU>",
   :time-stamp #inst "2007-05-11T14:46:53.000-00:00",
   :subject "College primary web server back online",
   :memo
   "The primary web server has been returned to service."})

(def permission-map
  {:id 999999,
   :name "Device",
   :description "",
   :deleted nil,
   :info nil,
   :created #inst "2019-09-03T16:46:51.274-00:00",
   :updated-by 999999,
   :updated #inst "2019-09-03T16:46:51.274-00:00"})

(def user-map
  {:deleted nil,
   :email "test@byu.edu",
   :last-name "testing",
   :retired true,
   :phone "000000",
   :updated #inst "2019-09-03T16:46:18.480-00:00",
   :name "test",
   :created #inst "2019-09-03T16:46:18.480-00:00",
   :first-name "Tester",
   :updated-by nil,
   :id 999999,
   :info nil,
   :access-level "User",
   :memo "Tester",
   :user-group-id 999999})

(def user-groups-map
  {:id 999999,
   :name "Administrators",
   :description "",
   :deleted nil,
   :info nil,
   :created #inst "2019-09-03T16:46:18.439-00:00",
   :updated #inst "2019-09-03T16:46:18.439-00:00"})

(def work-map
  {:id 999999,
   :clock-in #inst "2004-06-08T20:00:40.000-00:00",
   :clock-out #inst "2004-06-08T22:23:55.000-00:00",
   :memo "test driven",
   :deleted nil,
   :info nil,
   :created #inst "2019-09-03T16:46:51.398-00:00",
   :updated #inst "2019-09-03T16:46:51.398-00:00"})
