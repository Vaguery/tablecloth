# tablecloth

A sometimes-tricky coding kata.

## Problem definition

Imagine we have a tabletop, which we will represent as the Cartesian plane for `x` >= 0.

We also have a collection of _boxes_. Each box is a rectangle, and is defined by three values, its `left` (edge position on the `x` axis), its `width`, and its `height`. Any given box is defined by these three values, and you're welcome to think of them as a tuple, or three attributes of an object, or as a map, or whatever you like.

Imagine that we place all the boxes on the table, in the `x` position stated by each one's `left` specification. Boxes can overlap as needed; we just place one in front of the other.

Then we crouch down and peer across the table. We will see a _skyline_. That is, for every `x` position on the table, there are one or more _tallest_ boxes. If we trace along the top of the tallest box for all values of `x`, there is one particular `y` value (which may be 0.0, where no box is sitting). Taken together, these `y` values for each `x` ≥ 0 define the _skyline function_ for a specified set of boxes.

Here's a sketch of some boxes, and the black line at the top is the skyline formed by them.

![skyline](http://vaguery.github.io/tablecloth/img/tablecloth.png)

You can see in that sketch that the "height" of the top black line is the maximum height of all the boxes that sit straddle any point on the `x` axis. The challenges, of course, are not to merely write a function to execute that "maximum height of all the boxes" phrase algorithmically, but rather to write such an algorithm _well_. That is to say: using good technique, and _also_ producing a good result.

There are a few things I'd like to do here as part of the _kata_:

### skyline function

Given a set of boxes (each defined by its `left`, `width` and `height` values), and some `x` value, construct a function that returns the correct `y` value for any `x` ≥ 0.

### skyline `changed?` predicate

Given a set of boxes, and one more box, return `true` if the skyline function _after the new box is added to the original collection_ is different, or `false` if it is the same. That is, if the new box "bumps up" the skyline anywhere, return `true`.

### skyline normalizer

Given a set of boxes, return a new set of boxes which together form the same skyline, _but do not overlap one another_ in the `x` direction. This will probably involve making a new box for every place where the skyline value changes along the `x` axis.

### skyline reducer

Given a set of boxes, return the smallest subset of the same boxes which produces the same skyline. That is, remove any box which does not contribute to the skyline.

### skyline optimizer

Given a set of boxes, return a new set of boxes which together form the same skyline, _and is of minimum size_. That is, the number of boxes you use to construct the new skyline should be the minimum possible number of boxes, of any size.

## Kata

As with any coding _kata_, the point here is to practice your coding skills mindfully. That is, write tests, produce readable code, spend time refactoring and cleaning it up and making it something a colleague or stranger could look at and understand and use.

There is no right answer, and I haven't specified a language to use. I'm planning on working through it in Clojure, because I'm a bit out of practice after a few weeks away. But I might also write it in Ruby or Javascript or Python, and so should you.

## Fitness cases for machine learning

This _kata_ turns out to be an interesting problem for genetic programming and neural networks, as well.

Here are some guidelines for constructing training and test data:

- let all three values take positive values in the range `[0.0, 100.0]`. They should all be floating-point values.
- select cases with 10, 40, 160, and 640 boxes each, to explore scaling
- be sure to include at least one empty training case, a few with overlapping (redundant) boxes, and a few with non-overlapping boxes
