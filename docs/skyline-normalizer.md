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

*To Be Continued*
