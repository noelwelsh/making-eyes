---
layout: page
title: BlueEyes' Concurrency Model
---

The main construct for concurrency in BlueEyes is the `Future`. A `Future[A]` represents a computation that will finish at some point in ... the future, delivering a value of type `A`. Futures thus represent a natural way of building programs that process input asynchronously.

BlueEyes uses [Akka's Future implementation](http://doc.akka.io/docs/akka/2.0.1/scala/futures.html). They are used pervasively in BlueEyes, so we must become familiar with the Future API to write BlueEyes code.


## Using Futures

The key method on a Future is `map`. Given a `Future[A]`, `map` takes a function `A => B` returning a `Future[B]`. The computation will be only be run when the value of the Future is available. In this way we can build up a long chain of computations that only execute (and thus consume CPU) when the value to kick off the computation is available. For example:

{% highlight scala %}
val f = Future { 1 } // Create a future of an Int
f map { v => v + 2 } map { v => v * 3 } // Chain some computations to the Future
{% endhighlight %}

Futures also support `flatMap`, `filter`, and so on, which should be familiar. This means Future is a monad, and thus we can use it within for comprehensions.

### For Comprehensions

When it comes to actually using Futures, for all but the most trivial uses I always use for comprehensions. For example:

{% highlight scala %}
for {
  v1 <- Future { 1 }
  v2 <- Future { v1 + 2 }
} yield v2 * 3
{% endhighlight %}

This might be slightly less efficient that using `map` or `flatMap` as appropriate, but it is the huge benefit of being simple and consistent.


## Creating Futures

Creating futures `Promise`

## Actors
