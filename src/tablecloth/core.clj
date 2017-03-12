(ns tablecloth.core)

(defrecord Box [left width height])

(defn box-height
  "given a Box record and a numeric x value, returns the height of that Box measured at that x (or 0, if x lies outside the box)"
  [box x]
  (let [left (:left box)]
    (cond
      (< x left)
        0
      (<= x (+ left (:width box)))
        (:height box)
      :else
        0
      )))


(defn skyline
  "given a collection of Box records and a numeric x value, returns the maximum height of any Box in the collection, measured at that x"
  [boxes x]
  (apply max
     (conj (map #(box-height % x) boxes)
           0)))
