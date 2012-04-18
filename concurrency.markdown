---
layout: page
title: BlueEyes' Concurrency Model
---

The main construct for concurrency in BlueEyes is the `Future`. A `Future[A]` represents a computation that will finish at some point in ... the future, delivering a value of type `A`. Futures are thus a natural way to build asynchronous programs.

BlueEyes uses [Akka's Future implementation](http://doc.akka.io/docs/akka/2.0.1/scala/futures.html). They are used pervasively in BlueEyes, so we must become familiar with the Futures API and patterns to write BlueEyes code.

## Execution Context

Akka requires an (implicit) `ExecutionContext` to determine the thread pool (and other settings) that is used to run a Future. Correctly configuring an ExecutionContext can be an important part of optimising you code, but for many cases some simple defaults suffice. To get a reasonable default context, simply mix-in `blueeyes.bkka.AkkaDefaults`.


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

This might be slightly less efficient that using `map` or `flatMap` as appropriate, but it has the huge benefit of being simple and consistent. This code is simple to write, simple to read, and simple to debug. I recommend you adopt it in your programs.

## Creating Futures

Futures actually consist of two parts: a Future, and a Promise. A Future is a "read handle", meaning it is the wrapper around a value that allows us to read that value when it becomes available. The Promise is the "write handle", the part that allows us to set the value of the Future. Note you can only set the value of a Future once. Setting the value of a Future is called completing the Promise.

The easiest way to create a Future is to simply call the apply function on the Future object with the computation that will produce the value:

{% highlight scala %}
Future { someComputation() }
{% endhighlight %}

If you have already computed the value of a Future at the point where you want to return a Future of that value, the most efficient way to create this Future is to write

{% highlight scala %}
Promise.successful { <theValue> }
{% endhighlight %}

This is slightly more efficient than writing

{% highlight scala %}
Future { <theValue> }
{% endhighlight %}

Completing promises.

### Serial and Parallel Composition

Serial composition in Futures is simply `map` or `flatMap` as appropriate. Parallel composition is a bit more involved. Here are some of the possibilities:

If you have a sequence of Futures, you can convert them to the Future of a sequence using the `sequence` function on the `Future` object.

Example here

`traverse`

Choosing the first Future to complete

Timeouts



## Error Handling

Exceptions are not good for handling errors in Futures. There are several reasons:

1. When an exception is raised within a Future there is no sensible place to deliver the exception too -- the thread that created the Future is likely not the thread running the Future. Thus a Future enters a cancelled state, and no further computation occurs within that Future. Unless at some point you block on the Future the exception will never be raised. Instead code will just silently stop working. This makes debugging difficult.

2. When using Futures it is common to use Actors to, for example, interface with synchronous code. We interact with Actors by passing messages, not calling procedures, so an Actor does not share a stack with it's caller. This means that if we do get an exception from a Future, and that exception was raised within an Actor, the stack trace is likely to useless at it will lack almost all context.

3. Exceptions are not represented in the type system, and thus the type system won't help us perform correct error handling. One of Scala's main strengths is its expressive type system and it silly to lose the benefits of the type system for this important case.

Given the above, I recommend you encode errors as a data type. The `Either` type in the standard library is appropriate, but Scalaz's `Validation` type is better as it makes a clearer separation between success and failure. Many people are afraid of using Scalaz. I urge you not to be; Validations are very simple to use. Here is a quick tutorial:

- A Validation is parameterised by two types, the type of failures and the type of successes: `Validation[Failure, Success]`.
- There are two corresponding case classes, `Success` and `Failure`.
- A Validation is a monad. It has `map`, `flatMap` and so on. If the Validation is a Success processing will continue, otherwise no further processing will take place.
- Use the `fold` method to convert a Validation to value (usually a HttpResponse) when you've finished processing:

{% highlight scala %}
aValidation.fold(
  failure = f => "Boo, we failed! The reason is "+f.toString,
  success = s => "Yay, we succeeded with result "+s.toString
)
{% endhighlight %}

- The `.success` and `.fail` implicits convert any value to a Success or Failure respectively. Recall that a Validation is parameterised by two types. One of these types can be taken from the value being converting, but type inference will often fail to correctly infer the other type. Here's an example:

{% highlight scala %}
// Construct instances of Validation[String, Int]
42.success[String] // 42 is a success but we must specify the failure type
"Oh noes!".fail[Int] // We infer the failure type but not the success type
{% endhighlight %}

- To convert an Option to a Validation, use the `toSuccess` implicit. You must pass the failure value to this function:

{% highlight scala %}
// Construct instaces of Validation[String, Int]
Some(42) toSuccess "Oh noes!" // => Success(42)
None toSuccess "Oh noes!" // => Failure("Oh noes!")
{% endhighlight %}

To use Scalaz you currently need to add a dependency on `7.0-SNAPSHOT`. The appropriate line for your SBT build is

{% highlight scala %}
"org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT"
{% endhighlight %}

The correct import statements are a bit goofy. To get `Validation` you need just

{% highlight scala %}
import scalaz.Validation // You can also import Success and Failure if you want
{% endhighlight %}

To get the `.success` and `.fail` implicits you need

{% highlight scala %}
import scalaz.syntax.validation._
{% endhighlight %}

Finally to get the `.toSuccess` implicit on `Option` you need

{% highlight scala %}
import scalaz.std.option.optionSyntax._
{% endhighlight %}

When using Validations you must decide on type to represent failures. A string may seem attractive but our experience has shown more structure is useful. In particular, you want to record enough information to convert the failure to an appropriate HttpResponse. To solve this problem we have created a type called `Problem`. Our code is available in a project called [Bigtop](https://github.com/bigtop/bigtop). It is open sourced under the Apache License, so you can freely use it in your code.

If you use Validations you will often end up with a type like `Future[Validation[F,S]]`. This will have the unhappy consequence that you need to unwrap the Future to get the Validation and then unwrap the Validation to get the value:

{% highlight scala %}
Future { 42.success } map { validation => validation.map { value => doSomething(value) }}
{% endhighlight %}

Writing this code repeatedly will make you sad. I recommend you flatten this hierarchy down to a single type called, say, `FutureValidation`, and implement appropriate methods on it. An implicit to convert to this type will make you life much easier. You don't even have to write FutureValidation, as it already exists as part of [Bigtop](https://github.com/bigtop/bigtop). In Bigtop, the `.fv` implicit converts a Validation or Future[Validation] to a FutureValidation. Once you have this you can write simple code in the for comprehension style. For example, here is some real code taken from Bigtop that shows this style. Note it uses some extensions to BlueEyes' JSON parsing code, also in Bigtop, that returns Validations:

{% highlight scala %}
for {
  json     <- req.json
  username <- json.mandatory[String]("username").fv
  password <- json.mandatory[String]("password").fv
  result   <- action.create(username, password)
} yield result
{% endhighlight %}

This code has all the advantages of the for comprehension style for futures with the added benefit of handling errors sensibly.

Exceptions may still be raised by code outside your control, so I recommend that at the end of your processing chain, just before creating a response, you call the `recover` method to catch any exceptions and convert them to a Failure.
