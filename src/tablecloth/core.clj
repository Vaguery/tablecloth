(ns tablecloth.core)

(defrecord Box [left width height])

(defn box-height
  [box x]
  (let [l (:left box)
        w (:width box)
        h (:height box)]
    (cond
      (< x l) 0
      (<= x (+ l w)) h
      :else 0)))

(defn skyline
  [boxes x]
  (apply max (map #(box-height % x) boxes)))
