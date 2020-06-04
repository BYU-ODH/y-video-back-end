(ns y-video-back.routes.service_handlers.utils)

(defn remove-db-only
  "Compares 2 maps, not counting created, updated, and deleted fields"
  [my_map]
  (dissoc my_map :created :updated :deleted))

(defn add-namespace ; Can probably delete this function, not in use?
  "Converts all keywords to namespace-keywords"
  [namespace m]
  (into {}
    (map (fn [val]
           {
            (keyword
              namespace
              (clojure.string/replace
                (str
                  (get val 0))
                ":"
                ""))
            (get val 1)})
      m)))

(defn to-uuid
  [text_in]
  (java.util.UUID/fromString text_in))

(defn get-id
  [res]
  (str (:id res)))
