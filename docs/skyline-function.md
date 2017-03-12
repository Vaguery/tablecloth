**Previously:** [getting started](getting-started.md)

# The skyline function

Now what I've specified in the "skyline function" part of this task is that a "box" is defined by three numerical values: its `left` edge position on the x axis, its `width`, and its `height`.

It's probably my old Object-Oriented habits, but personally I prefer labeled attributes rather than using a raw tuple for these things. That is to say, I _could_ define a `box` by convention as some ordered collection of numbers, like (in CLojure) a `vector` `[2.0 3.0 4.0]` or `list` like `'(2.0 3.0 4.0)`.

But when I'm writing code that does things like calculating the position of the `right` edge of a box (which I am almost certain I'll want to do, and soon), I don't really want to calculate `right` this way:

~~~ clojure
(+ (first box) (second box))
~~~

but would _much_ rather do it this way:

~~~ clojure
(+ (:left box) (:width box))
~~~

So I'll make a `Box` `record` to use from here on.

### testing constructors

I start with a check:

~~~ midje
(fact "I can make a box"
  (:left (->Box 1 2 3)) => 1
  (:width (->Box 1 2 3)) => 2
  (:height (->Box 1 2 3)) => 3
  )
~~~

Notice that there is no `Box` record defined yet, so I'm writing this test expecting it to fail.

And indeed that's just what happens.

~~~ text
java.lang.RuntimeException: Unable to resolve symbol: ->Box in this context, compiling:(tablecloth/core_test.clj:6:10)
~~~

That's easy to fix over in `tablecloth.core`

~~~ clojure
(ns tablecloth.core)

(defrecord Box [left width height])
~~~

Not complicated or surprising.

### heights

Now the _actual_ task here is to report a maximum `height` value at some given `x` position, in the context of a collection of `Box` items.

The collection could be empty, but frankly that's a boring case so I put it off until later.

It seems that what I want to do will involve collecting a bunch of different `height` "contributions" at some position `x`, one for all the zero or more boxes that happen to be sitting over position `x`, and then return the maximum of those contributions. Feels like I will want to be able to return something called `box-height`, which is the "height contribution" of some single box at some particular `x` position. Think of it as being the "single-box skyline", right? It's `0.0` everywhere around the `Box`, and that box's `:height` only where the box actually sits.

Good enough for a test:

~~~ clojure
(facts "about box-height"
  (fact "I can detect the box when the x falls within it"
    (box-height (->Box 1 10 2) 2) => 2
    ))
~~~

That fails (predictably) because there is no function called `box-height` yet.

~~~ clojure
(defn box-height
  [box x]
  (let [l (:left box)
        w (:width box)
        r (+ l w)
        h (:height box)]
    (cond
      (< x l) 0
      (<= x r) h
      :else 0
      )))
~~~

So the idea here is simple enough. If `x` is less than the `:left` of the box, the result is `0`. If that's not true, but `x` is less than or equal to the right side of the box, then we return the `:height`. Otherwise we're over to the box's right, and we return `0`.

Did I _need_ all that conditional logic? No, not at all. But I'm not using "strict" test-driven design here, I'm just writing tests first. If I were using _real_ TDD, then I would have made this test pass (the one that checks at `x=2` that the height is `2`) _by always returning 2_. The _next_ test I'd written, which would have no doubt checked some other `x` value, _that's_ when I would have added more logic.

This is a big source of confusion for many developers, some of whom even present (wrong) information at conferences: "TDD" does not simply mean "writing a failing unit test before you write the code that makes it pass". Test-driven _design_ means you only add code that makes a _failing test pass_.

Anyway, I don't usually use strict TDD. I just want tests to tell me I haven't broken anything, and to slow down my fevered head to the point where I don't lose track of what's working and what isn't.

So I'm going to add a bunch more tests, and I _expect_ all of them to actually pass because I did the "whole function" already:

~~~ clojure
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
~~~

And indeed, this is the case.

### overlapping boxes

Now that I can determine the "profile" of one `Box` accurately, I'd expect I will want to take a collection of `Box` items and measure their individual `box-height` values all at once. This is just mapping the `box-height` function over the collection of boxes, but I just want to get a sense of how this might work, so I write a little "sketch" in my test file.

~~~ clojure
(fact "I can determine all the heights at one x"
  (let [boxes [(->Box 1 10 2)
               (->Box 2  2 3)
               (->Box 12 3 4)]]
    (map #(box-height % 2.0) boxes) => [2 3 0]
    (map #(box-height % 0.0) boxes) => [0 0 0]
    (map #(box-height % 13.0) boxes) => [0 0 4]
    ))
~~~

So just to help get my head around where I _think_ I want to go, I've picked three boxes where the first two overlap, and the third is off on its own. Picking an `x` that falls in the region where the first two overlap should give me two non-zero `box-height` values, picking one that doesn't fall under any of them should give me three `0` values, and picking one that's over where the third box sits should give me its height only. And that is the case.

This test passes immediately, because I've just been _exercising_ the code I've already written. It's not so much a unit test as a sort of implicit acceptance test of it. Something like "I can probably write my `sklyine` function this way, invoking the function and producing this intermediate result."

### skyline

That "sketch" I just made might seem extraneous to all you powerful thinkers and ninja programmers, but I like the way it helps me frame this next one:

~~~ clojure
(fact "skyline function returns tallest height at x"
  (let [boxes [(->Box 1 10 2)
               (->Box 2  2 3)
               (->Box 12 3 4)]]
    (skyline boxes 2.0) => 3
    (skyline boxes 0.0) => 0
    (skyline boxes 13.0) => 4
    ))
~~~

See how this echoes the previous test? All I've really done is made the previous explicit calculation of _all_ the `box-height` values, and manually taken the `max` of those.

I could have done something else in this test, I suppose, to indicate my intention. For instance, instead of `(skyline boxes 13.0) => 4` I might have said `(skyline boxes 13.0) => (max [0 0 4])`, but to be honest that feels _less_ clear than doing it this way. Your experience may vary.

Anyway, that test fails, because there is no `skyline` function yet. So:

~~~ clojure
(defn skyline
  [boxes x]
  (apply max (map #(box-height % x) boxes)))
~~~

That's tidy enough to make me happy. It does only one thing (a plus), and it's not even very opaque or nested.

### refactoring and such

Here's my complete Clojure source file, so far:

~~~ clojure
(ns tablecloth.core)

(defrecord Box [left width height])

(defn box-height
  [box x]
  (let [l (:left box)
        w (:width box)
        r (+ l w)
        h (:height box)]
    (cond
      (< x l) 0
      (<= x r) h
      :else 0
      )))

(defn skyline
  [boxes x]
  (apply max (map #(box-height % x) boxes)))
~~~

I haven't got any doc-strings, which I should probably fix. And that `box-height` function feels a bit spread out. I wonder if I could refactor it a bit?

I spend some time fiddling with the conditional part of `box-height`. I suppose in some sense an `if` would be "tidier", especially since the "left of box" and "right of box" parts both return a value of 0. But when I write that, it involves a freaky clause like `(if (or (< x l) (> x r)))` and you know what? That's not communicative to me. This, even though it's a bit redundant, feels more viscerally _like_ what I'm envisioning: a scan from left to right.

So I leave it. Since I admonished players to try to practice good practices, here's where my code ends up:

~~~ clojure
(ns tablecloth.core)

(defrecord Box [left width height])

(defn box-height
  "given a Box record and a numeric x value, returns the height of that Box measured at that x (or 0, if x lies outside the box)"
  [box x]
  (let [l (:left box)
        w (:width box)
        r (+ l w)
        h (:height box)]
    (cond
      (< x l) 0
      (<= x r) h
      :else 0
      )))

(defn skyline
  "given a collection of Box records and a numeric x value, returns the maximum height of any Box in the collection, measured at that x"
  [boxes x]
  (apply max (map #(box-height % x) boxes)))
~~~

I don't see a _need_ for type hints or anything fancy like that, yet. Maybe there will be, somewhere down the road? I somehow doubt it, though.

Next time I'll work on the `skyline-changed?` predicate. Thinking a bit about that, I start to suspect there may be a little pressure building to make the `Box` record a little richer. We'll see.
