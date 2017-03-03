# tablecloth

A sometimes-tricky coding kata.

## Problem definition

Imagine we have a tabletop, which we will represent as the Cartesian plane for `x` >= 0.

We also have a collection of _boxes_. Each box is a rectangle, and is defined by three values, its `left` (edge position on the `x` axis), its `width`, and its `height`. Any given box is defined by these three values, and you're welcome to think of them as a tuple, or three attributes of an object, or as a map, or whatever you like.

Imagine that we place the boxes on the table, in the `x` position stated by each one's `left` specification, and then we crouch down and peer across the table. We will see a _skyline_. That is, for every `x` position on the table, there are one or more _tallest_ boxes. If we trace along the top of the tallest box for all values of `x`, there is one particular `y` value (which may be 0.0, where no box is sitting). Taken together, these `y` values for each `x` ≥ 0 define the _skyline function_ for a specified set of boxes.

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

## How to run the tests

The project uses [Midje](https://github.com/marick/Midje/).

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.
