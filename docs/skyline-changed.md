**Previously:** [the skyline function](skyline-function.html)

# The skyline-changed? predicate function

Somewhere along the way, I muttered that there would probably be "some changes" necessary in the way I have been modeling the boxes in this problem, and I _think_ (again, writing this essentially in order, as I work through it) that this is where those changes kick in.

Here's my thinking: The `skyline` function I've written now reports the highest point at any _one_ point `x`, given some set of boxes. In this part, the task is to report whether the skyline _changes anywhere_ when a new box is added to an existing collection. So now the task involves—at least—multiple checks of before and after. There's no way to know _where_ the skyline may have changed, except that it would only have changed somewhere within the span of the new box added, right? It might be that the new box is taller than the existing skyline at one end, or at both ends, or _at neither end_, but is still higher in the middle. There are a lot more things (for some definition of "a lot") to pay attention to, is all I'm saying.

### thinking about it

As I said just now, the skyline can't change except in the span of `x` values between the `left` and `right` ends of the new box. The first places it could be different is at those exact `x` positions. And it strikes me, sketching on paper a bit, that the only other places I would need to check would be at any other `left` or `right` ends of existing boxes that are already part of the collection. There would be no changes in the skyline "between" those edges, without there also being an equivalent change at the edge. And since all I'm doing at this point is _detecting_ change, that seems like it would suffice.

So here are some examples of what I'm thinking. They're just simple setups that seem to capture some of the cases.

This one bumps up the skyline at a place where two boxes meet on the same level (I've added a `right` column, even though at the moment by `Box` record doesn't have that as an explicit field):

~~~ text
left    width    height    [right]
existing boxes:
0       2        2         2
2       2        2         4
new box:
1       2        3         3
~~~

This one bumps up the skyline in the gap _between_ two existing boxes:

~~~ text
left    width    height    [right]
existing boxes:
0       2        2         2
3       2        2         5
new box:
0       4        1         4
~~~

This one bumps up the skyline in a previously-empty area:

~~~ text
left    width    height    [right]
existing boxes:
0       2        2         2
new box:
3       1        1         4
~~~

And this one doesn't bump up the skyline:

~~~ text
left    width    height    [right]
existing boxes:
0       2        2         2
2       2        3         4
new box:
1       2        1         3
~~~

There are probably more cases to test, and I may have missed a few "atomic" examples that involve as few boxes as possible. Maybe "one box is entirely inside another"? But the point is that when you draw them out, these examples show that _places_ I will need to check include the ends of the new box, _and_ the ends of boxes that fall between those points.

### collecting box end positions

I have a feeling I will want to collect all the `x` positions of all the `left` and `right` ends of boxes in a collection. I don't need to have multiple copies of them, just one each, and since I'm working in Clojure I can use a `set` collection. I know other languages (like Ruby) have ways of removing duplicates from flat collections, so I'll assume that in other languages that's what you might do (if you decided to proceed the same way).

I start by writing a failing test:

~~~ clojure
(fact "box-ends produces a set of end points"
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)
               (->Box 1 2 3)]]
    (box-ends boxes) => 99
    ))
~~~

Again, this is _intentionally_ failing, not just because there's no `box-ends` function defined, but because I know the answer shouldn't be `99`. I'm just making a place to stand.

I want to collect (`reduce` in Clojure terms) all the `left` and right edges of the boxes into one `set`. So I start with something that looks like this:

~~~ clojure
(defn box-ends
  [boxes]
  (reduce
    (fn [edges box]
      (into edges [(:left box) (right-x box)]))
    #{}
    boxes))
~~~

I've invoked a function that doesn't exist, `right-x`, because I'm tired of writing `(+ (:left box) (:width box))`. I don't want to write a new function _and_ extract something from that function all at once, so before I even run the tests I put it back in place just to make it work, and plan on refactoring it out afterwards. So the first code I try to run is really:

~~~ clojure
(defn box-ends
  [boxes]
  (reduce
    (fn [edges box]
      (into edges [(:left box) (+ (:left box) (:width box))]))
    #{}
    boxes))
~~~

Remarkably that runs, and produces this response from Midje:

~~~ text
FAIL "box-ends produces a set of end points" at (core_test.clj:53)
    Expected: 99
      Actual: #{0 1 2 3 4}
~~~

Which is... well, it's _right_. That's the set containing all the `:left` and right edges of all the three boxes.

I add a few more tests to build a little more confidence; I've learned that any unexpected symmetry (like all the numbers from `0` to `4`) is to be mistrusted in a test result, so:

~~~ clojure
(fact "box-ends produces a set of end points"
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)
               (->Box 1 2 3)]]
    (box-ends boxes) => #{0 1 2 3 4}
    (box-ends (take 1 boxes)) => #{0 2}
    (box-ends (take 2 boxes)) => #{0 2 4}
    (box-ends []) => #{}
    ))
~~~

And those _also_ pass. So apparently by complete accident I've written a bunch of working code. That's a strange feeling.

Time to refactor, though.

First, I want to extract that `(+ (:left box) (:width box))` thing. I suppose I could rework the definition of the `Box` record so there was a `:right` field, and make a constructor that handled the math when the `Box` instance was created. But so far I don't have a sense of pressure about that, and I'm skittish about calculated fields in record instances for some reason.

If you want the details of why I'm skittish: It's because Clojure records can have new values "set". I put the word "set" in quotes because a record is nominally an immutable item, and what you're really doing when you assign a new value is creating a new record. And then you need to find a way to capture the "new" invocation and insert your "calculate the `:right` value" code into that somehow... and so on. Easier for now to just call a function and do a bit of math whenever you want that value for a given `Box`.

Here's what I write:

~~~ clojure
(defn right
  [box]
  (+ (:left box) (:width box)))
~~~

Then I can simplify the `box-ends` function to be

~~~ clojure
(defn box-ends
  [boxes]
  (reduce
    (fn [edges box]
      (into edges [(:left box) (right box)]))
    #{}
    boxes))
~~~

I also notice that I can use `right` in the `box-height` function, so I simplfy that as well while I'm at it. And all the tests pass.

(Some folks might have asked me whether I should have tested `right` itself. I guess, yes? But I'm immediately using it in two different places, both of which are heavily tested already. Despite my reputation as a stickler, I don't see the need when I'm comfortable with the syntax. As I am in Clojure. If I were working in some weird language I was _learning_, then yes, I would almost certainly have pissed off my pairing partner by insisting there be tests of `right`.)

### just the important box ends

So now I can take a collection of boxes and get the `x` positions of all their sides. What do I do with that information? The task is to see if things are different in the skyline before and after adding a new box. My sketches above convince me that the only place change _can_ occur is between the ends of the new box, and so the `box-ends` that fall in that range are the only ones I need to check `skyline-function`.

I want to sketch in code again, like I did last time when I went from "all the heights of the boxes at `x`" to "the max height of the boxes at `x`".

~~~ clojure
(fact "I can filter box-ends by an interval"
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)]]
    (filter
      #(and (< 1 %) (> 3 %))
      (box-ends boxes)) => #{0 2 4}
    ))
~~~

This helps me find a direction in which to face, as it were. I'm imagining here that the `1` and `3` are the sides of some "new box". I'm a little put off by the fact that the `2` sides "overlap" for the original boxes, so let me make a single change:

It also doesn't _quite_ pass as written, since the checker is assuming a `set` is returned, and the `filter` returns a [lazy] sequence. I make the two changes and get this passing test:

~~~ clojure
(fact "I can filter box-ends by an interval"
  (let [boxes [(->Box 0 2 1)
               (->Box 2.1 2 2)]]
    (into #{}
      (filter
        #(and (< 1 %) (> 3 %))
        (box-ends boxes))) => #{2 2.1}
    ))
~~~

Now the _function_ I want to write that does this will also want to include the ends of the "new box" in the result. Writing this little exploration makes me think the function name wants to be called something like `sides-within-box`.

### `sides-within-box`

I start with a failing test again

~~~ clojure
(fact "sides-within-box returns the side positions it overlaps in a collection of boxes"
  (let [boxes [(->Box 0 2 1)
               (->Box 2.1 2 2)]]
    (sides-within-box  (->Box 1 2 3) boxes) => 99
    ))
~~~

That's the same situation as the previous test I wrote for hand-filtering, and in a minute I want to add back in the sides of the "filtering" box to the result `set`. For now, the result is a patently absurd one (`99`), because things are again slightly complicated. When this fails informatively, I'll change the expected result to _mean_ "given the two original boxes, and a new box that spans `x` values between `1` and `3` inclusive, I want a `set` that includes `1` and `3` and also any sides of the collected boxes that fall in that interval".

Needless to say, there is no code yet that implements `sides-within-box`, so I get

~~~ text
java.lang.RuntimeException: Unable to resolve symbol: sides-within-box in this context, compiling:(tablecloth/core_test.clj:72:5)
~~~

I add what seems to be a straightforward copy of what I did in the previous example, and I also throw in a local variable `all` that includes the ends of the new box for good measure:

~~~ clojure
(defn sides-within-box
  [boxes box]
  (let [l (:left box)
        r (right box)
        all (into (box-ends boxes) [l r])]
    (filter #(and (<= l %) (>= r %)) all)
    ))
~~~

And that crashes like a bad crashy thing.

~~~ text
FAIL "sides-within-box returns the side positions it overlaps in a collection of boxes" at (core_test.clj:72)
    Expected: 99
      Actual: java.lang.NullPointerException
              tablecloth.core$right.invokeStatic(core.clj:7)
              tablecloth.core$right.invoke(core.clj:5)
~~~

I hate seeing `NullPointerException` when I'm writing Clojure code. It means I've stumbled into the _many_ places where Clojure is like `/shrug` and just says impenetrable stuff about hidden implementation details.

I wrangle for a while with the function, trying to discover where inside it I've made a mistake. I delete the code in the body, and it still crashes. I delete the code in the `let` statement, and it _still_ crashes.

Can you see where I made my mistake?

OK. _I reversed the arguments_. In the test, I have called `(sides-within-box (->Box 1 2 3) boxes)`, and in the function I have defined it as `(defn sides-within-box [boxes box] ...)`. This, along with its nasty cousin that crops up when you try to treat an integer as a collection, is why Clojure can be so super irritating some times.

When I realize this, and swap the arguments in the test, I get a more moderated and informative response from Midje:

~~~ text
FAIL "sides-within-box returns the side positions it overlaps in a collection of boxes" at (core_test.clj:72)
    Expected: 99
      Actual: (2.1 1 3 2)
~~~

Which is... almost the expected answer. Whew. I have forgotten to cast the result as a `set`, but I can handle that. Finally, my test is

~~~ clojure
(fact "sides-within-box returns the side positions it overlaps in a collection of boxes"
  (let [boxes [(->Box 0 2 1)
               (->Box 2.1 2 2)]]
    (sides-within-box boxes (->Box 1 2 3)) => #{1 2 2.1 3}
    ))
~~~

and the code that makes this pass is

~~~ clojure
(defn sides-within-box
  [boxes box]
  (let [l (:left box)
        r (right box)
        all (into (box-ends boxes) [l r])]
    (into #{}
      (filter #(and (<= l %) (>= r %)) all))
    ))
~~~

I want to write a few more tests, for edge cases and stuff, so I kind of go to town and write a whole bunch of cases just for a sense of security:

~~~ clojure
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
~~~

Those all pass.

### `skyline-changed?`

I think I've got what I need to work through `skyline-changed?` now. I can gather all the `x` positions where the skyline _might_ have changed.

I can write at least one test already:

~~~ clojure
(fact "skyline-changed? returns true if, well..."
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)]]
    (skyline-changed? boxes (->Box 1 2 3)) => true
  ))
~~~

That is, it will change when a big tall box appears in the mix.

Of course, there is no such function (again), so I write one.

~~~ clojure
(defn skyline-changed?
  [boxes box]
  (let [exes (sides-within-box boxes box)
        old-sky (partial skyline boxes)
        new-sky (partial skyline (conj boxes box))]
    (some #(not= (old-sky %) (new-sky %)) exes)
  ))
~~~

I am eliding a bunch of stupid typos here, and a whole lot of looking-things-up-at-clojuredocs.org. I hope you will forgive me.

What this does is actually pretty straightforward. In the `let` box I am determining all the salient `x` values (saved as `exes`), and constructing two `partial` functions that will tell me "the skyline" of the collection of boxes without the new one, and the skyline with the new one added in. Then in the body I'm saying, essentially, "For all these `exes` values, is there any place where the `skyline-function` is different before and after?"

That's what `some` does. Sortof. It's a but more complicated than that, because it _actually_ returns a value that isn't `false` if it doesn't find something (which, if you know Clojure, is signaled by the fact that it isn't called `some?`).

At any rate, this makes the test pass. So time for more tests! This second claim is intended to fail (the expected outcome is `false`, not `true`):

~~~ clojure
(fact "skyline-changed? returns true if, well..."
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)]]
    (skyline-changed? boxes (->Box 1 2 3)) => true
    (skyline-changed? boxes (->Box 0 2 1)) => true
  ))
~~~

And it does fail, but that's because `some` returns `nil` when it doesn't find anything, not `false`. Like I said above, in fact. This provokes a minor change in my `skyline-changed?` function, where I cast the result of `some` to a `boolean` value.

~~~ clojure
(defn skyline-changed?
  [boxes box]
  (let [exes (sides-within-box boxes box)
        old-sky (partial skyline boxes)
        new-sky (partial skyline (conj boxes box))]
    (boolean
      (some #(not= (old-sky %) (new-sky %)) exes))
  ))
~~~

That works when I fix the test to be correct (oops), and it continues to work when I add a bunch more cases:

~~~ clojure
(fact "skyline-changed? returns true if, well..."
  (let [boxes [(->Box 0 2 1)
               (->Box 2 2 2)]]
    (skyline-changed? boxes (->Box 1 2 3)) => true
    (skyline-changed? boxes (->Box 0 2 1)) => false
    (skyline-changed? boxes (->Box 100 2 10)) => true
    (skyline-changed? boxes (->Box 0 4 1)) => false
    (skyline-changed? boxes (->Box 0 2 2)) => true
  ))
~~~

I suppose I should also check to see if an empty collection registers being changed:

~~~ clojure
(fact "skyline-changed? works for an empty collection"
  (skyline-changed? [] (->Box 1 2 3)) => true
)
~~~

And it does!

### cleanup

I should write some docstrings and clean things up a bit.

While I'm doing the docstrings, I realize one thing with broader implications: I've slipped from using "end" to "side" when I refer to the left and right edges of a `Box` record. I make a few adjustments throughout the codebase to reconcile those different usages.

~~~ clojure
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
~~~

## other paths

I want to point out that I could have done this a few other ways. To be frank, I didn't _need_ to extract just the section of the `x` axis where the new box sits.

That smacks of "premature optimization" on my part, but my defense is that it's the only way I actually thought about the problem once I'd sketched it. Clojure is remarkably efficient at these things, so I doubt I would have had "trouble" with over-sized collections or memory if I'd skipped that whole part. Because the number of "checkpoints" is approximately linear with the number of boxes in the collection. `/shrug`

I'm sure there are many other ways to approach it. For instance, if I were working in an object-oriented language, I'd have been tempted to give methods to the `Box` and `Boxes` objects, and maybe the call structures would be very different. Admittedly, even in Ruby I lean towards `Array#collect` and `Array#inject` methods, so my particular approach would be similar on a fine scale I bet.

## next time

I suppose it will be time for `skyline-normalizer` next time. That's the one that takes a collection of `Box` records, and gives you a new collection where none of the boxes overlap in the `x` direction. Well, except the ends I guess.

**Next:** [normalizing a given skyline](skyline-normalizer.html)
