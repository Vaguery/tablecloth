(ns tablecloth.core)

(defrecord Box [left width height])

(defn right
  [box]
  (+ (:left box) (:width box)))

(defn box-height
  "given a Box record and a numeric x value, returns the height of that Box measured at that x (or 0, if x lies outside the box)"
  [box x]
  (let [left (:left box)]
    (cond
      (< x left)
        0
      (<= x (right box))
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


(defn box-ends
  [boxes]
  (reduce
    (fn [edges box]
      (into edges [(:left box) (right box)]))
    #{}
    boxes))


(defn sides-within-box
  [boxes box]
  (let [l (:left box)
        r (right box)
        all (into (box-ends boxes) [l r])]
    (into #{}
      (filter #(and (<= l %) (>= r %)) all))
    ))
