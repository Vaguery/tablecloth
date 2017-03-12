# getting started

This is an account of my thinking (including the mistakes and missteps) I've made in doing my own coding _kata_. As with all my "learning in public" pieces, the point here is to surface the process of work, not so much the "right answer". To be honest, I'm not even sure there's a "right answer" for all the several parts of this exercise....

## Clojure setup

I have a few reasons to choose Clojure, and they're all personal and should have no bearing on your choice to work through this. I've been away from Clojure for a while, and I want to refresh my memory and maybe also learn a bit about Clojure 1.9's `spec` system if that seems useful here. My genetic programming systems are (for the moment) written in Clojure, and as I mentioned in the main problem description, I think this will make a nice benchmark problem for software discovery. And I've recently switched from using an antiquated copy of [Sublime Text 2](https://sublimetext.com/2) to [Atom](https://atom.io), and I figure it might be good to work on something that's not 100% mission-critical first to get my smash-key skills back up to speed in the new world.

So, anyway, Clojure. While I'm _curious_ about `clojure.spec`, I certainly don't know enough to use it _in lieu_ of testing, so I start by setting up a Midje project that looks something like

~~~ text
/tablecloth
  /src
    /tablecloth
      core.clj
  /test
    /tablecloth
      core_test.clj
~~~

and a `project.clj` file that looks a bit like this

~~~ clojure
(defproject tablecloth "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [proto-repl "0.3.1"]]
  :profiles {:dev {:dependencies [[midje "1.8.3"]]}})
~~~

The two Clojure source files I start with are just place-holders, and _even though I've done this a million times before_ I always get paranoid that my testing framework isn't "wired up" correctly, so I always start every project with a stupid test to make sure I can run the tests and see passing and failing results as needed. After all, I intend to run tests essentially every time I hit "return" in the source file, so I'd rather know it's not one of the several changes in library versions or editors or plugins in the editor or any of the other infrastructure changes when I inevitably get _surprised_ later on.

So `/src/tablecloth/core.clj` starts off like this

~~~ clojure
(ns tablecloth.core)

# empty, except for declaring the namespace
~~~

and `test/tablecloth/core_test.clj` something like this

~~~ clojure
(ns tablecloth.core-test
  (:use midje.sweet)
  (:use [tablecloth.core]))

(fact "I can run tests that pass and fail"
  (+ 2 3) => 5
  (+ 2 2) => 5
  )
~~~

Now I can run these tests from the command line, by navigating into the project directory in a shell and typing `lein midje` (because [I've already set up my account's root `profile.clj` appropriately](https://github.com/marick/lein-midje#installation)), and just to make sure I do that and it works as expected:

~~~ text
tmesis:tablecloth bill$ lein midje

FAIL "I can run tests that pass and fail" at (core_test.clj:7)
    Expected: 5
      Actual: 4
FAILURE: 1 check failed.  (But 1 succeeded.)
Subprocess failed
~~~

But I'm also getting up to speed with Atom, so I root around in the various package repositories and discussions, and discover that everybody says I should be running [proto-repl](https://atom.io/packages/proto-repl). I won't bore you with the remarkably simple installation process, but hey it works better than I expect. And to be honest the presence of a REPL _in addition_ to unit tests I write is always a pleasant way to write Clojure code.

Time passes, `proto-repl` is installed, and I discover a lovely little smash-key first thing: When a REPL is running, typing `cmd-opt-A` will "run all tests in the project" and poop out the result of the REPL prompt for me. This is very nice, and while I suspect there may be a time when I want to learn a more focused "run some of the tests" smash-key, it's my regular habit to run all of them every time for as long as I can stand.

And indeed, the REPL reports

~~~ text
FAIL "I can run tests that pass and fail" at (core_test.clj:7)
    Expected: 5
      Actual: 4
~~~

and then when I fix the `2+2=5` assertion, I end up with

~~~ text
Ran 0 tests containing 0 assertions.
0 failures, 0 errors.
"Elapsed time: 70.016425 msecs"
~~~

So now I think I can make code.

**Next:** [the basic skyline function](skyline-function.md)
