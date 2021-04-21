(ns y-video-back.utils.utils)

(defn to-uuid
  [text-in]
  (java.util.UUID/fromString text-in))

(def nil-uuid
  (to-uuid "00000000-0000-0000-0000-000000000000"))
