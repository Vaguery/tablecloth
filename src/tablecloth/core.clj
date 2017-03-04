(ns tablecloth.core)

(defrecord Box [left width height])

(defn box-height
  [box x]
  (let [l (:left box)
        w (:width box)
        r (+ l w)
        h (:height box)]
    (if
      (and (<= x r) (>= x l)) h
      0
      )))


(defn skyline
  [boxes x]
  (apply max (map #(box-height % x) boxes)))
