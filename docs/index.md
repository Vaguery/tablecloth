# tablecloth

A sometimes-tricky [coding *kata*](https://en.wikipedia.org/wiki/Kata_(programming)).

## my version

This is an exercise, and it would be mean (and possibly stupid) if I didn't try it myself a few times. So I've been working through it (in Clojure, though that's not really important), and [collecting my thoughts](http://vaguery.github.io/tablecloth/my-path.html) in that "Learning in Public" style I prefer.

## Problem definition

There are actually a handful of "small" single-purpose functions here. The practical motivation is that I was constructing a small battery of test functions for a machine learning training system, and realized that the functions themselves held together in a sort of tiny "library"... but that they also exposed a number of very different "facets" of the simple underlying problem description.

Imagine we have a tabletop. Call the left edge of the table `x=0`, and as we move right across the table think of the distance from the left as a value `x`. Call the _height_ of any object above the tabletop a distance `y`.

We also have a collection of _boxes_. Each box is a rectangle, and is defined by three values, its `left` (edge position on the `x` axis), its `width`, and its `height`. Any given box is defined by these three values, and you're welcome to think of them as a tuple, or three attributes of an object, or as a map, or whatever you like.

Imagine that we place all the boxes on the table, in the `x` position stated by each one's `left` specification. Boxes can overlap as needed; we just place one in front of the other.

Then we crouch down and peer across the table. We will see a _skyline_. That is, for every `x` position on the table, there may be no box at all, and so at that `x` position, the skyline has height `0`. Or there might be one or more boxes sitting astraddle position `x`, in which case the skyline height is the height of the tallest sitting over that spot. Taken together, these `y` values for each `x` ≥ 0 define the _skyline function_ for a specified set of boxes.

Here's a sketch of some boxes, and the black line at the top is the skyline formed by them.

![skyline](http://vaguery.github.io/tablecloth/img/tablecloth.png)

You can see in that sketch that the "height" of the top black line is the maximum height of all the boxes that sit straddle any point on the `x` axis. There's a thin vertical black line; looking at that `x` position, you can see the smaller red box in the "foreground" is dominated by the larger wide purple box in the "background", so the skyline `y` value at that point is the top of the purple box.

The challenges in this (or any) _kata_, of course, are not to merely write a function to execute that "maximum height of all the boxes" phrase algorithmically, but rather to write such an algorithm _well_. That is to say: using good technique, and _also_ producing a good result.

There are a few things I'd like to do here as part of the _kata_. In the examples to follow, I will use this set of boxes as an example (they're the numbers I used to make that sketch, too):

~~~ text
box    left    width    height
1       10        8        3
2       12        5        4
3        6        3        2
4       19        4        1
5        0       16        5
~~~

### skyline function

Given a set of boxes (each defined by its `left`, `width` and `height` values), and some `x` value, construct a function that returns the correct `y` value for any `x` ≥ 0. Wherever there is no box sitting, the skyline value should be `0.0`.

If I gave you the example boxes, and asked for the value at `x=8` (the thin vertical line), you would tell me `5`.

### skyline `changed?` predicate

Given a set of boxes, and one more box, return `true` if the skyline function _after the new box is added to the original collection_ is different, or `false` if it is the same. That is, if the new box "bumps up" the skyline anywhere, return `true`.

In the example, suppose I gave you boxes `{1,2,4,5}` and not box `3` (the little red one). If I then gave you box `3`, you would respond `false`. If instead I had given you boxes `{1,2,3,5}` and then box `4` (way over on the right by itself), you would respond `true`.

### skyline normalizer

Given a set of boxes, return a new set of boxes which together form the same skyline, _but do not overlap one another_ in the `x` direction. This will probably involve making a new box for every place where the skyline value changes along the `x` axis.

In the example, if I gave you all five boxes, you would give me four boxes:

1. `{left: 0, width: 16, height: 5}` (box 5)
2. `{left:16, width:  1, height: 4}`
3. `{left:17, width:  1, height: 3}`
4. `{left:19, width:  4, height: 1}` (box 4)

### skyline reducer

Given a set of boxes, return the smallest subset those boxes which produces the same skyline. That is, remove any box which does not contribute to the skyline. For example, in the sketch above, the little red box can be removed without changing the skyline at all, so it should be removed.

In the example, if I gave you all five boxes, you would remove box 3.

### skyline optimizer

Given a set of boxes, return a new set of boxes which together form the same skyline, _and is of minimum size_. That is, the number of boxes you use to construct the new skyline should be the minimum possible number of boxes, of any size. Be sure to take into account situations where two boxes of the same height are adjacent to one another, and feel free to use long, low boxes span several stretches of skyline that happen to be the same height.

Several answers would do in the case of the example I gave you, but there would need to be four boxes. You could simply remove box 3, or return the normalized boxes described above. Either answer has the minimum of four boxes. At least I _think_ that's the minimum....

### skyline counter

Given a set of boxes, return an integer that represents the number of different "altitudes" in the whole skyline function. In other words, how many different horizontal lines comprise it, for all `x` values?

In the example, given five boxes, the answer would be `5`. There are five different heights (including the tabletop, which is `0`) in the entire skyline.

### skyline histogrammer

Given a set of boxes, return a collection (in any order) of `(y, x)` pairs, where `y` is a specific unique height of the skyline function, and the associated `x` is the _total_ skyline distance at that height exactly, over all possible `x`. Not including the table-top `x=0` value, of course. There should be exactly one entry for every non-zero `y` value the skyline function takes.

In the example, given five boxes, the answer would have to include these pairs, in any order:

- `{y: 5, x: 16}`
- `{y: 4, x:  1}`
- `{y: 3, x:  1}`
- `{y: 1, x:  4}`

## Kata

As with any coding _kata_, the point here is to practice your coding skills mindfully. That is, write tests, produce readable code, spend time refactoring and cleaning it up and making it something a colleague or stranger could look at and understand and use.

There is no right answer, and I haven't specified a language to use. I'm planning on working through it in Clojure, because I'm a bit out of practice after a few weeks away. But I might also write it in Ruby or Javascript or Python, and so should you.

## Fitness cases for machine learning

This _kata_ turns out to be an interesting problem for genetic programming and neural networks, as well.

Here are some guidelines for constructing training and test data:

- let all three values take positive values in the range `[0.0, 100.0]`. They should all be floating-point values.
- select cases with 10, 40, 160, and 640 boxes each, to explore scaling
- be sure to include at least one empty training case, a few with overlapping (redundant) boxes, and a few with non-overlapping boxes
- be sure to include situations where several boxes have the same height, and overlap one another
- be sure to include cases where one box "dominates" another; that is, where a smaller box cannot contribute at all to the skyline
- be sure to include cases where all the boxes are the same height, or where there are only a few specific heights for many boxes
