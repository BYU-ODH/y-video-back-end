(def x 2)

(print
  (reduce
    +
    (map
      #(/
        (reduce
          *
          (repeat x %1))
        %1)
        (range 1 11))))
