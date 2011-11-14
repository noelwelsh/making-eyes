---
layout: page
title: A Quick Introduction to BlueEyes
---

The *Service* is the core concept in BlueEyes. At it's heart a service is a set of related *request handlers*. A request handler is just a function, bound to a URL, from a HTTP request to a HTTP responsse.

Typically a service needs some configuration parameters (for example, the database to use), and will need to create (and later destroy) some resources, such as a connection to the database. In BlueEyes the configuration parameters are called the service's *context*, and the request handlers plus their startup and shutdown functions are called a *service descriptor*.

Let's look at how to build a very simple service in BlueEyes. We're going to build a simple calculator web service. It will respond to URLs like `/add/<number>/<number>` and `/multiply/<number>/<number>`. 

## Building a Service

The simplest way to construct a REST service is to extend `blueeyes.BlueEyesServiceBuilder` and call the `service` function:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait CalculatorService extends BlueEyesServiceBuilder {
  val calculatorService = service("name", "version") {
    ... // service descriptor goes here
  }
}
{% endhighlight %}

### Service

The `service` function takes a `name`, `version`, and a function from a context to a service descriptor. The `name` and the `version` are strings. The name can be anything you want, while the version number should be a version number like `"1.0.0"`:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait CalculatorService extends BlueEyesServiceBuilder {
  val calculatorService = service("calculatorService", "1.0.0") {
    context => // service descriptor
  }
}
{% endhighlight %}


### The Service Descriptor

The service descriptor is created by chaining together functions to handle `startup`, `request`s, and `shutdown`:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait CalculatorService extends BlueEyesServiceBuilder {
  val calculatorService = service("calculatorService", "1.0.0") {
    context => 
      startup {
        // Create resources
      } ->
      request {
        // Handle requests
      } ->
      shutdown {
        // Destroy resources
      }
  }
}
{% endhighlight %}


### Startup and Shutdown

We pass to `startup` a function that will create any resources we need. This function must return a `Future`. If you're not familiar with futures, they're an abstraction for handling concurrency. This is the first hint at how pervasively BlueEyes has adopted a high performance approach. We'll talk about futures in detail later. For now, all you need to know is that there is an implicit `.future` to convert any value to a future of that value.

The simplest startup function does nothing:

{% highlight scala %}
startup {
  ().future
}
{% endhighlight %}

The `shutdown` function is similar, except it takes a function from a configuration (of the same type as the value return by the startup function) to a future. Since we haven't done anything in our startup we don't need to do anything in our shutdown:

{% highlight scala %}
shutdown { config =>
  ().future
}
{% endhighlight %}

Our code currently looks like:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait CalculatorService extends BlueEyesServiceBuilder {
  val calculatorService = service("calculatorService", "1.0.0") {
    context => 
      startup {
        ().future
      } ->
      request {
        // Handle requests
      } ->
      shutdown { config =>
        ().future
      }
  }
}
{% endhighlight %}

Now we just need to write some request handlers and we're done!

### Request

Like `startup` and `shutdown`, we pass to a `request` a function, this time from our configuration (as returned by `startup`) to our request handlers. Request handlers are *partial functions* from `HttpRequest` to a future of `HttpResponse`.  Being partial functions, a request handler can decide whether or not it handles a given URL. BlueEyes provides a number of combinators from which we can build request handlers. For example, the `path` combinator can be used to match the path of a URL:

{% highlight scala %}
request { config =>
  path("/foo/bar") {
    // Do something
  }
}
{% endhighlight %}

These combinators all compose. That is, they expect their final argument to also be a partial function. Thus we could write the `path` above as

{% highlight scala %}
request { config =>
  path("/foo") {
    path("/bar") {
      // Do something
    }
  }
}
{% endhighlight %}

In our case we want to match the paths `/add` and `/mulitply`. Furthermore, we expect each path to be followed by two numbers, which we want to extract. In the above example we passed a string to `path`. The `path` combinator allows more flexible input. If we want to extract part of a path we can name those parts with symbols. We combine parts with the '/' method. The extracted values are then made available in the `parameters` map. For example, for `add` we could write:

{% highlight scala %}
path("/add" / 'number1 / 'number2) { case request =>
  val number1 = request.parameters.get('number1)
  val number2 = request.parameters.get('number2)
  // Do something
}
{% endhighlight %}

Since extracting parameters is a fundamental operation, BlueEyes provides a `parameter` combinator, which we could use in place of the code above:

{% highlight scala %}
path("/add" / 'number1 / 'number2) { 
  parameter('number1) { number1 =>
    parameter('number2) { number2 =>
     // Do something
   }
 }
}
{% endhighlight %}

We're now ready to write the `add` request handler. We simply need to convert `number1` and `number2` to numbers (let's use Doubles). If this is successful we'll return the sum, otherwise we'll return an appropriate error. Here's the code:

{% highlight scala %}
path("/add" / 'number1 / 'number2) { 
  parameter('number1) { number1 =>
    parameter('number2) { number2 =>
      try {
        val n1 = number1.toDouble
        val n2 = number2.toDouble
        val sum = n1 + n2
        
        HttpResponse[ByteChunk](content = Some(sum.toString)).future
      } catch {
          case e: NumberFormatException => HttpResponse[ByteChunk](status = HttpStatus(BadRequest)).future
      }
   }
 }
}
{% endhighlight %}

A few things to note about the code above. Firstly, the `HttpResponse` constructor allows us to specify the status code, headers, content, and HTTP version, but has sensible default arguments. If we write just `HttpResponse[ByteChunk]()` we get an empty response with a 200 (OK) status.

Notice also the type, `ByteChunk`, we parameterise `HttpResponse` by. This specifies how the content, in our case the sum, is encoded. The basic content type is `ByteChunk`. As the name suggests this represents chunks of bytes, which are essentially a lazy list of byte arrays. BlueEyes understands other content types, such as `JValue` which is used to represent JSON data. 

Note that the content we set in the `HttpResponse` is a `String`, not a `ByteChunk`. All data must eventually be converted to `ByteChunk`s, so BlueEyes has a number of *bijections* for converting data. By mixing `blueeyes.core.data.BijectionsChunkString` into our service, we make available an implicit bijection between `String` and `ByteChunk`. Thus we can set the content to a `String` and the bijection will implicitly convert it to a `ByteChunk`. That is, we alter the definition of `CalculatorService` to be:

{% highlight scala %}
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

trait CalculatorService extends BlueEyesServiceBuilder with BijectionsChunkString {
  ...
}
{% endhighlight %}

Finally notice we called the `future` method on our responses. Remember request handlers return futures of responses, so it is necessary to perform this conversion.

#### Chaining Together Request Handlers

We've implemented one of the two request handlers. The code for `multiply` is a straightforward adaptation of `add`. However we now need to chain together these two handlers. Since request handlers are just partial functions we can use the `orElse` method defined on partial functions. BlueEyes also provides a `~` method that does the same thing. It makes code a bit easier to read, and it's the idiomatic option within BlueEyes. With this addition we can write our request handlers as

{% highlight scala %}
path("/add" / 'number1 / 'number2) { 
  ... // rest of add code goes here
} ~
path("/multiply" / 'number1 / 'number2) {
  parameter('number1) { number1 =>
    parameter('number2) { number2 =>
      try {
        val n1 = number1.toDouble
        val n2 = number2.toDouble
        val product = n1 * n2
        
        HttpResponse[ByteChunk](content = Some(product.toString)).future
      } catch {
          case e: NumberFormatException => HttpResponse[ByteChunk](status = HttpStatus(BadRequest)).future
      }
   }
 }
}
{% endhighlight %}

### The Complete Service

The complete code for the calculator service is:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

trait CalculatorService extends BlueEyesServiceBuilder with BijectionsChunkString {
  val calculatorService = service("calculatorService", "1.0.0") {
    context => 
      startup {
        ().future
      } ->
      request {
        path("/add" / 'number1 / 'number2) { 
          parameter('number1) { number1 =>
            parameter('number2) { number2 => 
              request: HttpRequest[ByteChunk] =>
                try {
                  val n1 = number1.toDouble
                  val n2 = number2.toDouble
                  val sum = n1 + n2
                  
                  HttpResponse[ByteChunk](content = Some(sum.toString)).future
                } catch {
                  case e: NumberFormatException => HttpResponse[ByteChunk](status = HttpStatus(BadRequest)).future  
                }
           }
         }
        } ~
        path("/multiply" / 'number1 / 'number2) {
          parameter('number1) { number1 =>
            parameter('number2) { number2 =>
              request: HttpRequest[ByteChunk] =>
                try {
                  val n1 = number1.toDouble
                  val n2 = number2.toDouble
                  val product = n1 * n2
                
                  HttpResponse[ByteChunk](content = Some(product.toString)).future
                } catch {
                  case e: NumberFormatException => HttpResponse[ByteChunk](status = HttpStatus(BadRequest)).future
                }
           }
         }
        }
      } ->
      shutdown { config =>
        ().future
      }
  }
}
{% endhighlight %}

## Building A Server

A *server* is the software process that runs one or more services. To actually execute our calculator service we must build a server to run it. BlueEyes makes this very simple: simply extend the `BlueEyesServer` trait and mix in the traits defining the service:

{% highlight scala %}
import BlueEyes.BlueEyesServer

object CalculatorServer extends BlueEyesServer with CalculatorService
{% endhighlight %}

A server has `start` and `stop` methods, and a `main` method. By default the `main` method takes a `--configFile` command line option, specifying a file containing configuration parameters. Since our service has no configuration you can simply create an empty file and pass that on the command line.

## Next Steps

We've build a complete service, showing the basic elements of using BlueEyes. The complete code for this example service is available [here](calculator-service)

Of course there is a lot more to BlueEyes. The later chapters go into more depth on building services, as well as covering areas such as testing and persistence that we haven't touched on here.


