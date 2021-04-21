(ns y-video-back.routes.service-handlers.handlers.email-handlers
  (:require
   [y-video-back.email.mail :as mail]
   [reitit.ring.middleware.multipart :as multipart]
   [y-video-back.routes.service-handlers.utils.role-utils :as role-utils]))
(def send-email-with-attachment
  {:summary "Sends email with attachment to admin email address(es)"
   :permission-level "student"
   :parameters {:header {:session-id uuid?}
                :multipart {:sender-email string?
                            :subject string?
                            :message string?
                            :attachment multipart/temp-file-part}}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [sender-email subject message attachment]} :multipart {:keys [session-id]} :header} :parameters}]
                (mail/send-email {:sender sender-email :subject subject
                                  :body [{:type "text/html"
                                          :content message}
                                         {:type :attachment
                                          :content (:tempfile attachment)
                                          :file-name (:filename attachment)}]
                                  :message message  ; For use in logging
                                  :user-id (role-utils/token-to-user-id-all session-id)}))})  ; For use in logging

(def send-email
  {:summary "Sends email to admin email address(es)"
   :permission-level "student"
   :parameters {:header {:session-id uuid?}
                :body {:sender-email string?
                       :subject string?
                       :message string?}}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [sender-email subject message]} :body {:keys [session-id]} :header} :parameters}]
                (mail/send-email {:sender sender-email :subject subject
                                  :body [{:type "text/html"
                                          :content message}]
                                  :message message  ; For use in logging
                                  :user-id (role-utils/token-to-user-id-all session-id)}))})  ; For use in logging

