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


(fact "box-sides produces a set of side points"
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)
               (->Box 1 2 3)]]
    (box-sides boxes) => #{0 1 2 3 4}
    (box-sides (take 1 boxes)) => #{0 2}
    (box-sides (take 2 boxes)) => #{0 2 4}
    (box-sides []) => #{}
    ))


(fact "I can filter box-sides by an interval"
  (let [boxes [(->Box 0 2 1)
               (->Box 2.1 2 2)]]
    (into #{}
      (filter
        #(and (< 1 %) (> 3 %))
        (box-sides boxes))) => #{2 2.1}
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


(fact "skyline-changed? returns true if, well..."
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)]]
    (skyline-changed? boxes (->Box 1 2 3)) => true
    (skyline-changed? boxes (->Box 0 2 1)) => false
    (skyline-changed? boxes (->Box 100 2 10)) => true
    (skyline-changed? boxes (->Box 0 4 1)) => false
    (skyline-changed? boxes (->Box 0 2 2)) => true
  ))

(fact "skyline-changed? works for an empty collection"
  (skyline-changed? [] (->Box 1 2 3)) => true
  )


(fact "skyline-interpolation produces heights of horizontal spans"
  (let [boxes [(->Box 0 2 1.1)
               (->Box 2 2 2.2)
               (->Box 1 2 3.3)]]
  (box-sides boxes) => #{0 1 2 3 4}
  (skyline-interpolation boxes) =>
    {[0 1] 1.1, [1 2] 3.3, [2 3] 3.3, [3 4] 2.2}
  (skyline-interpolation (take 2 boxes)) =>
    {[0 2] 1.1, [2 4] 2.2}
  (skyline-interpolation (take 1 boxes)) =>
    {[0 2] 1.1}
  (skyline-interpolation (drop 2 boxes)) =>
    {[1 3] 3.3}
  ))


(fact "skyline-interpolation works for an empty collection"
  (skyline-interpolation []) => {}
  )


(fact "skyline-normalize consolidates adjacent boxes of the same height"
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 3 5 2)]) => [(->Box 1 7 2)]
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 3 5 3)]) => [(->Box 1 2 2)
                                          (->Box 3 5 3)]
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 1 5 2)]) => [(->Box 1 5 2)]
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 2 2 2)
                      (->Box 2 17 2)
                      (->Box 9 12 2)]) => [(->Box 1 20 2)]
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 5 2 2)]) => [(->Box 1 2 2)
                                          ; (->Box 3 2 0)
                                          (->Box 5 2 2)]
  )
