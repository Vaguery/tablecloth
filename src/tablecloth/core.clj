(ns tablecloth.core)

(defrecord Box [left width height])

(defn right
  "given a `Box` record, returns the `x` value of its right side."
  [box]
  (+ (:left box) (:width box)))


(defn box-height
  "given a `Box` record and a numeric x value, returns the height of that Box measured at that x (or 0, if x lies outside the box)"
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
  "given a collection of `Box` records and a numeric `x` value, returns the maximum height of any `Box` in the collection, measured at that `x`"
  [boxes x]
  (apply
    max
    (conj
      (map #(box-height % x) boxes)
      0
      )))


(defn box-sides
  "given a collection of `Box` records, it returns a `set` containing all the `x` positions of the left and right sides of every box"
  [boxes]
  (reduce
    (fn [sides box]
      (into sides [(:left box) (right box)]))
    #{}
    boxes
    ))


(defn sides-within-box
  "given a collection of `Box` records and a new `Box`, it returns a `set` containing the `x` values of the new `Box`, plus all left or right sides of the collection that fall between those limits"
  [boxes box]
  (let [l (:left box)
        r (right box)
        all (into (box-sides boxes) [l r])]
    (into #{}
      (filter #(and (<= l %) (>= r %)) all))
      ))


(defn skyline-changed?
  "given a collection of `Box` records and a new `Box`, it returns `true` if the `skyline-function` for the collection-plus-the-new-box differs anywhere from the original skyline`"
  [boxes box]
  (let [exes    (sides-within-box boxes box)
        old-sky (partial skyline boxes)
        new-sky (partial skyline (conj boxes box))]
    (boolean
      (some
        #(not= (old-sky %) (new-sky %))
        exes))
        ))
