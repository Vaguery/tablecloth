(ns tablecloth.core-test
  (:use midje.sweet)
  (:use [tablecloth.core]))

(fact "I can make a box"
  (:left (->Box 1 2 3)) => 1
  (:width (->Box 1 2 3)) => 2
  (:height (->Box 1 2 3)) => 3
  )

;
(facts "about box-height"
  (fact "returns height, given one box and any x value"
    (box-height (->Box 1 10 2) 2) => 2
    (box-height (->Box 1 10 2) 1) => 2
    (box-height (->Box 1 10 2) 0) => 0
    (box-height (->Box 1 10 2) 10) => 2
    (box-height (->Box 1 10 2) 11) => 2
    (box-height (->Box 1 10 2) 11.00001) => 0
    (box-height (->Box 1 10 2) 9000) => 0
    ))


(fact "I can determine all the heights at one x"
  (let [boxes [(->Box 1 10 2)
               (->Box 2  2 3)
               (->Box 12 3 4)]]
    (map #(box-height % 2.0) boxes) => [2 3 0]
    (map #(box-height % 0.0) boxes) => [0 0 0]
    (map #(box-height % 13.0) boxes) => [0 0 4]
    ))


(fact "skyline function returns tallest height at x"
  (let [boxes [(->Box 1 10 2)
               (->Box 2  2 3)
               (->Box 12 3 4)]]
    (skyline boxes 2.0) => 3
    (skyline boxes 0.0) => 0
    (skyline boxes 13.0) => 4
    ))


(fact "skyline works with an empty collection"
  (skyline [] 5) => 0
  )


(fact "box-ends produces a set of end points"
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)
               (->Box 1 2 3)]]
    (box-ends boxes) => #{0 1 2 3 4}
    (box-ends (take 1 boxes)) => #{0 2}
    (box-ends (take 2 boxes)) => #{0 2 4}
    (box-ends []) => #{}
    ))


(fact "I can filter box-ends by an interval"
  (let [boxes [(->Box 0 2 1)
               (->Box 2.1 2 2)]]
    (into #{}
      (filter
        #(and (< 1 %) (> 3 %))
        (box-ends boxes))) => #{2 2.1}
    ))

(fact "sides-within-box returns the side positions it overlaps in a collection of boxes"
  (let [boxes [(->Box 0 2 1)
               (->Box 2.1 2 2)]]
    (sides-within-box boxes (->Box 1 2 3)) =>
      #{1 2 2.1 3}
    (sides-within-box boxes (->Box 12 2 3)) =>
      #{12 14}
    (sides-within-box boxes (->Box 1 0.1 3)) =>
      #{1.1 1}
    (sides-within-box boxes (->Box 2.1 0.1 3)) =>
      #{2.1 2.2}
    (sides-within-box boxes (->Box 0 33 3)) =>
      #{0 2 2.1 4.1 33}
    ))
