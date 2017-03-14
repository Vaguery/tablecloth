**Previously:** [the `skyline-changed?` predicate](skyline-changed.html)

# The skyline normalizer function

The original statement of this aspect of the exercise was:

> Given a set of boxes, return a new set of boxes which together form the same skyline, _but do not overlap one another_ in the `x` direction. This will probably involve making a new box for every place where the skyline value changes along the `x` axis.

And I have to say, that's a pretty straightforward hint I put in there. What can I say? I was writing it off the cuff, and to be honest I hadn't worked through it all the way when I published it.

So here's my thinking: For the same reasons that the `skyline-changed?` predicate only needed to check the _sides_ of boxes, this normalizer will also want to pay attention to the sides of all the boxes in the collection. If you think about the way the skyline actually changes, it only moves up or down at the transition points of edges. But here, unlike the case of the `skyline-changed?` function, here we want to look at the transitions _between_ those sides.

At least that's what it seems to me.

One way I can see to approach it, which leverages these handy tools I've already built, is to take the collection of `Box` records, find all the sides of them, and then use the _midpoints_ of those spans to measure the height of various horizontal segments of the skyline. Then, as we walk from left to right, any time the `skyline-height` changes from one horizontal segment to the next, we know we need to start a new `Box`.

I can also see a few ways to do this that involve a sort of "reduction" approach, something like "chop off any portion of a box that overlaps another box". But I can't quite get my head around that, and so maybe you want to follow up on that half-baked notion in your approach.

### `skyline-interpolation`

I think I'll make a function called `skyline-interpolation`. Given a collection of `Box` record, it will invoke `box-sides` to get the set of sides, and then I'll sort those, and then do some kind of mapping that produces the `skyline-height` at the midpoints of each span.

~~~ clojure
(fact "skyline-interpolation produces heights of horizontal spans"
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)
               (->Box 1 2 3)]]
  (box-sides boxes) => #{0 1 2 3 4}
  (skyline-interpolation boxes) => [1 3 3 2]
  ))
~~~

Just as a reference, I've included `box-sides` in the same test, to remind myself what the spans are. Remember that in this `skyline-interpolation` function I want to have _every_ pair of sides produce one height measurement, so while there are five sides, there will be four heights—and I'll assume that all the heights outside those regions are `0`.

As always, the test fails because there is no `skyline-interpolation` function.

~~~ text
java.lang.RuntimeException: Unable to resolve symbol: skyline-interpolation in this context, compiling:(tablecloth/core_test.clj:105:3)
~~~

In this part, I'm going to work in a weird kind of exploratory way that I find helpful. It's _definitely_ not test-first; it's really more like REPL-based development, since what I'm going to do is _intentionally_ cause this test I've written to emit intermediate results that I can see, and then gradually work my way to the desired outcome.

This is very, vert sketchy, at least for TDD people. In the Clojure world (and probably in other REPL-friendly languages), not so much.

OK? We good?

Based on the sketch I've written out above, I end up here:

~~~ clojure
(defn skyline-interpolation
  [boxes]
  (let [sides     (box-sides boxes)
        height-fn (partial skyline boxes)]
    (partition 2 1 (sort sides))
    ))
~~~

This is me learning some interesting and esoteric facts about the `partition` function in Clojure. Normally it is used in its two-argument form, like `(partition chunk-size collection)`, and it will essentially chop a collection up into `chunk-size` pieces. But it also has a couple of other forms. In this one, I'm invoking it with a `step-size`, sort of `(partition chunk-size step-size collection)`. (There's an optional `padding` argument I've ignored, and it worked out that the result is actually what I want, so no biggie.)

This is what I see when I run the tests:

~~~ text
FAIL "skyline-interpolation produces heights of horizontal spans" at (core_test.clj:105)
    Expected: [1 3 3 2]
      Actual: ((0 1) (1 2) (2 3) (3 4))
       Diffs: in [0] expected 1, was (0 1)
              in [1] expected 3, was (1 2)
              in [2] expected 3, was (2 3)
              in [3] expected 2, was (3 4)
~~~

In fact, this is more or less what I wanted. To be frank I was expecting the `0` value to show up on the right-hand end of the list, but to be honest it's not a big issue for me. I can work with this too.

It's not the _answer_, though, so I make one further change... plus a teeny refactoring.

~~~ clojure
(defn midpoint
  [x1 x2]
  (/ (+ x1 x2) 2))

(defn skyline-interpolation
  [boxes]
  (let [sides     (box-sides boxes)
        height-fn (partial skyline boxes)]
    (map
      #(apply midpoint %)
      (partition 2 1 (sort sides)))
      ))
~~~

For the sake of readability, I've made a `midpoint` helper, which just returns the arithmetic average of two numbers. Now the `skyline-interpolation` function applies `midpoint` to each pair of sides produced by `partition`. As a result, I see a new error:

~~~ text
FAIL "skyline-interpolation produces heights of horizontal spans" at (core_test.clj:105)
    Expected: [1 3 3 2]
      Actual: (1/2 3/2 5/2 7/2)
       Diffs: in [0] expected 1, was 1/2
              in [1] expected 3, was 3/2
              in [2] expected 3, was 5/2
              in [3] expected 2, was 7/2
~~~

Those four fractions are the midpoints of the horizontal spans: the `x` values where I want to measure the `skyline`.

~~~ clojure
(defn skyline-interpolation
  [boxes]
  (let [sides     (box-sides boxes)
        height-fn (partial skyline boxes)]
    (map
      height-fn
      (map
        #(apply midpoint %)
        (partition 2 1 (sort sides)))
        )))
~~~

And that works!

Although, to be honest, now that I see it, I'm not 100% sure it's what I wanted at all.

In any case, just to make sure, I add a bunch more test cases. I also make a _tiny_ change to the heights, giving them a fractional push up so they are more clearly what they claim to be. (This is one of the problems when you only use `1` and `2` and `3` for building fixtures....)

~~~ clojure
(fact "skyline-interpolation produces heights of horizontal spans"
  (let [boxes [(->Box 0 2 1.1)
               (->Box 2 2 2.2)
               (->Box 1 2 3.3)]]
  (box-sides boxes) => #{0 1 2 3 4}
  (skyline-interpolation boxes) => [1.1 3.3 3.3 2.2]
  (skyline-interpolation (take 2 boxes)) => [1.1 2.2]
  (skyline-interpolation (take 1 boxes)) => [1.1]
  (skyline-interpolation (drop 2 boxes)) => [3.3]
  ))
~~~

So what I mean by "not 100% sure it's what I wanted at all" is that I'm wondering how to move from this to the proposed process of building a _new_ collection of boxes. For example, take the result `(skyline-interpolation boxes) => [1.1 3.3 3.3 2.2]`. What I want is some kind of association between the `sides` I've gathered together, and the heights of the respective horizontal "tops". Without re-calculating the `sides` again in the next function down the line.

Hmmm... "association", huh?

How about this: instead of returning _just_ the heights, I return a `map` where the keys are the pairs of sides themselves, and the values are the measured heights?

~~~ clojure
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
  ~~~

  That, of course, breaks in many ways. But none are surprising.

~~~ clojure
(defn skyline-interpolation
  [boxes]
  (let [sides     (box-sides boxes)
        chunks    (partition 2 1 (sort sides))
        height-fn (partial skyline boxes)]
    (zipmap
      chunks
      (map
        height-fn
        (map #(apply midpoint %) chunks
        )))))
~~~

That works, although I realize when I peer at it that there's a "trick" in it. See, Clojure doesn't discriminate between `vector` and `list` collections when checking equality. `(= [1 2] '(1 2))` evaluates to `true`, in other words. So you'll notice that my earlier partial solution that only invoked `partition` produced `'((0 1) (1 2) (2 3) (3 4))`. That's a Clojure `list`, and all the pairs inside it are `list` collections too. But my keys, as specified in this last test, they're `vector` collections.

Will that matter?

I don't know, to be honest. We'll see. For now, I think I can let it slide. I think this intermediate structure, the `map` produced by `skyline-interpolation`, will work for me.

There's one more thing to check, since you'll recall it was something I was upset at forgetting last time.

~~~ clojure
(fact "skyline-interpolation works for an empty collection"
  (skyline-interpolation []) => {}
  )
~~~

That is, the `map` I get from a collection of _no_ `Box` records is an empty one. And it works, too!

### normalized boxes

Now that I have this handy all-in-one data structure, I am imagining I can just walk down its length and produce some kind of new `Box` structure.

We'll see.

Time for some tests:

~~~ clojure
(fact "skyline-normalize consolidates adjacent boxes of the same height"
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 3 5 2)]) => [(->Box 1 4 2)]
  )
~~~

As always, this fails because there's no `skyline-normalize` function yet. This time, my idea is we are "walking" through this map (in some sorted order, which I should worry about), and collecting and building `Box` records only when the heights change from step to step.

That calls for a `loop`, but I'm going to start in that same exploratory way I used above.

~~~ clojure
(defn skyline-normalize
  [boxes]
  (let [chunks (sort (skyline-interpolation boxes))]
    chunks
  ))
~~~

This is simply trying to `sort` the `map` we get from `skyline-interpolation`. I've done that before, and it _should_ work, but...

~~~ text
FAIL "skyline-normalize consolidates adjacent boxes of the same height" at (core_test.clj:122)
    Expected: [{:height 2, :left 1, :width 4}]
      Actual: java.lang.ClassCastException: clojure.lang.LazySeq cannot be cast to java.lang.Comparable
~~~

Heh. Remember when I said that Clojure doesn't distinguish very well between `list` and `vector` items for equality checks? This is one of those times when it becomes important. `(sort {[3 2] 3 [2 3] 2 [1 2] 1})` produces `'([[1 2] 1] [[2 3] 2] [[3 2] 3])` with no complaint, sorting the `vector` keys correctly. But `(sort {'(3 2) 3 '(2 3) 2 '(1 2) 1})`, where the keys of the `map` are `list` items? No such luck.

~~~ text
ClassCastException clojure.lang.PersistentList cannot be cast to java.lang.Comparable  clojure.lang.Util.compare (Util.java:153)
~~~

If I'm going to want to do this, then the keys produced in `skyline-interpolation` need to be `vector` not `list` pairs. I make a slightly worrying _ad hoc_ change like this:

~~~ clojure
(defn skyline-interpolation
  [boxes]
  (let [sides         (box-sides boxes)
        chunks        (partition 2 1 (sort sides))
        vector-chunks (map #(into [] %) chunks)
        height-fn     (partial skyline boxes)]
    (zipmap
      vector-chunks
      (map
        height-fn
        (map #(apply midpoint %) chunks
        )))))
~~~

That is, I explicitly make `vector-chunks` and use that for the keys. The error disappears, though that function is becoming a little bit=top-heavy for my taste. Mayhap I will extract a function from it shortly....

But my error goes away, and the test now fails because I don't actually have the expected answer yet:

~~~ text
FAIL "skyline-normalize consolidates adjacent boxes of the same height" at (core_test.clj:122)
    Expected: [{:height 2, :left 1, :width 4}]
      Actual: ([[1 3] 2] [[3 8] 2])
       Diffs: expected length of sequence is 1, actual length is 2.
                actual has 1 element in excess: ([[3 8] 2])
              in [0] expected #tablecloth.core.Box{:left 1, :width 4, :height 2}, was [[1 3] 2]
~~~

Time to loop.

I confess: Clojure's tail recursion algorithms have always stressed me out. I have a tendency to build an inelegant crappy spaghetti `loop`, and I inevitably try to have two or more `recur` blocks floating around, and the compiler and I inevitably get in an argument.

So maybe if I _think_ for a minute, they way the big Clojure boys seem to do, I can piece this together? Because my frustration is almost certainly caused by my desire (insistence) on doing this incrementally, and Clojure's let's say "reticence" with letting me talk about a "part" of a recursive loop.

I have a sorted collection now, of pairs like this: `[[:left :right] :height]`. What I'm interested in doing is (1) as I walk (including the first step), whenever the `:height` value changes, I want to append a new `Box` to my collection. Whenever I take a step and the `:height` _doesn't_ change, I want to adjust the `:width` of the last box in my collection. And whenever I run out of pairs, I want to return the collection of `Box` items.

Yeah, it seems so simple when I write it out. :/

Here's a first pass. All this is doing is walking through the `skyline-interpolation` `map` pair-by-pair, and moving the "height" into a `vector` called `new-boxes`. I'm not trying to deal with the conditional "if the height changes" stuff yet, just making sure I have the Clojure `loop`/`recur` infrastructure set up correctly. Well... "correctly".

~~~ clojure
(defn skyline-normalize
  [boxes]
  (let [steps (sort (skyline-interpolation boxes))]
    (loop [step      (first steps)
           remaining (rest steps)
           new-boxes []]
      (if (nil? step)
        new-boxes
        (let [old-height (:height (last new-boxes))
              new-height (second step)]
          (recur  (first remaining)
                  (rest remaining)
                  (conj new-boxes new-height)))
                  ))))
~~~

This fails informatively:

~~~ text
FAIL "skyline-normalize consolidates adjacent boxes of the same height" at (core_test.clj:122)
    Expected: [{:height 2, :left 1, :width 4}]
      Actual: [2 2]
       Diffs: expected length of sequence is 1, actual length is 2.
                actual has 1 element in excess: (2)
              in [0] expected #tablecloth.core.Box{:left 1, :width 4, :height 2}, was 2
~~~

Now I think I can put a little condition in there.

Heh. "Little".

~~~ clojure
(defn skyline-normalize
  [boxes]
  (let [steps (sort (skyline-interpolation boxes))]
    (loop [step      (first steps)
           remaining (rest steps)
           new-boxes []]
      (if (nil? step)
        new-boxes
        (let [last-box (last new-boxes)
              last-width (:width last-box)
              old-height (:height last-box)
              new-height (second step)
              new-width (- (second (first step)) (ffirst step)) ]
          (recur  (first remaining)
                  (rest remaining)
                  (if (= old-height new-height)
                    (conj
                      (butlast new-boxes)
                      (assoc last-box :width (+ (:width last-box) new-width)))
                    (conj
                      new-boxes
                      (->Box (ffirst step) new-width new-height)))
                      ))))))
~~~

OK, this is one hell of a "little" condition, but it was, to be honest, me working out the decision tree right there in plain sight. If the heights are different, I want to make a new `Box`. If the heights are the same, I need to _revise_ the last `Box` I saved to make it wider. So all that business near the end about "`(assoc last-box :width (+ (:width last-box) new-width))`" is about updating the width of the "growing" box.

And... well, see, the weird thing is... it _works_. Just as a reminder, the failing test I was using as a "probe test" was

~~~ clojure
(fact "skyline-normalize consolidates adjacent boxes of the same height"
  (skyline-normalize [(->Box 1 2 2)
                      (->Box 3 5 2)]) => [(->Box 1 4 2)]
  )
~~~

And that failed when I ran this code with the message

~~~ text
FAIL "skyline-normalize consolidates adjacent boxes of the same height" at (core_test.clj:122)
    Expected: [{:height 2, :left 1, :width 4}]
      Actual: ({:height 2, :left 1, :width 7})
       Diffs: in [0 :width] expected 4, was 7
~~~

Which, I admit, was flummoxing. I had totally forgotten that I'd started with a failing test: the _correct_ answer is `(->Box 1 7 2)` now.

I fix that, and totally it is time to add a bunch more tests. Because there's no way this _works_ works.

Whew. Finally after adding several checks that miraculously seem to pass, I finally stumble on an edge case that causes troubles.

~~~ clojure
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
~~~

See, in that last one, there is a _gap_ between the two boxes. They don't overlap, and my algorithm "notices" the "height `0`" section and makes a heightless box as a "spacer".

**To Be Continued**
