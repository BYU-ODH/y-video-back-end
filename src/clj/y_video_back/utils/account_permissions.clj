(ns y-video-back.utils.account-permissions)

(def to-string-type
  {0 "admin"
   1 "lab-assistant"
   2 "instructor"
   3 "student"})

(def to-int-type
  {"admin" 0
   "lab-assistant" 1
   "instructor" 2
   "student" 3
   0 0
   1 1
   2 2
   3 3})

(def to-string-role
  {0 "instructor"
   1 "ta"
   2 "student"
   3 "auditing"})

(def to-int-role
  {"instructor" 0
   "ta" 1
   "student" 2
   "auditing" 3
   0 0
   1 1
   2 2
   3 3})
