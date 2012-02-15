---
layout: page
title: A Quick Introduction to BlueEyes
---

In this chapter we're going to visit some of the core concepts in BlueEyes, and build a very simple REST service.

The *Service* is the core concept in BlueEyes. A service is a computation that processes an HTTP request and returns a HTTP response[^http]. As a very simple example, a service might respond to requests for the path `/index.html` with an HTML page containing a directory listing. Of course existing web servers such as Apache and Nginx already do this just fine. BlueEyes shines were we want to build more complex services that full utilise the HTTP protocol.

[^http]: Note that BlueEyes is not tied to HTTP, but most of the time that is the protocol we'll use.

Services are built out of *service combinators*, which inspect a request and decide to continue processing or to stop. In the above example, we'd use the `path` service combinator to see if the request matches `/index.html`. As the name suggests, service combinators can be combined to create more complex combinators. We could, for example, only respond to requests for the path `/index.html` *and* which accept `text/html`. Alternatively, we might want a service that responds to the path `/index.html` *or* `/index.json`.

Service combinators end in a *service handler*, a function from an HTTP request to a response, which determines what the service responds with if all the combinators leading to the handler are successful. Continuing our example, a service handler would be responsible for actually constructing the page we send in response to the path `/index.html`.

Typically a service needs some configuration parameters (for example, the database to use), and will need to create (and later destroy) some resources, such as a connection to the database. In BlueEyes the configuration parameters are called the service's *context*, and the service plus its startup and shutdown functions is called a *service descriptor*.

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
import akka.Promise
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

We pass to `startup` a function that will create any resources we need. This function must return a `Future`. Futures are an abstraction for handling concurrency, which hold a computation that will complete some time in the future. This is the first hint at how pervasively BlueEyes has adopted a high performance approach. We'll talk about futures in detail later. For now, all you need to know is that to convert a value to a future of that value you call `akka.Promise.success`.

The simplest startup function does nothing:

{% highlight scala %}
startup {
  Promise.success{()}
}
{% endhighlight %}

The `shutdown` function is similar, except it takes a function from a configuration (of the same type as the value return by the startup function) to a future. Since we haven't done anything in our startup we don't need to do anything in our shutdown:

{% highlight scala %}
shutdown { config =>
  Promise.success{()}
}
{% endhighlight %}

Our code currently looks like:

{% highlight scala %}
import akka.Promise
import blueeyes.BlueEyesServiceBuilder

trait CalculatorService extends BlueEyesServiceBuilder {
  val calculatorService = service("calculatorService", "1.0.0") {
    context =>
      startup {
        Promise.success{()}
      } ->
      request {
        // Handle requests
      } ->
      shutdown { config =>
        Promise.success{()}
      }
  }
}
{% endhighlight %}

Now we just need to write some request combinators (and handlers) and we're done!

### Request

Like `startup` and `shutdown`, we pass a function to `request`.  This time the function is from our configuration (as returned by `startup`) to our service. Remember that services are built from service combinators and service handlers. BlueEyes provides a number of combinators for matching different properties of requests. For example, the `path` combinator can be used to match the path of a URL:

{% highlight scala %}
request { config =>
  path("/foo/bar") {
    // Do something
  }
}
{% endhighlight %}

These combinators all compose. We can nest them, forming a pipeline of combinators that must all match for processing to continue. We could, for example, write the `path` above as the following nested combinators:

{% highlight scala %}
request { config =>
  path("/foo") {
    path("/bar") {
      // Do something
    }
  }
}
{% endhighlight %}

We can also branch combinators, only one of which must match. If we wanted to match the path `/foo/quux` in addition to `/foo/bar`, we could write

{% highlight scala %}
request { config =>
  path("/foo") {
    path("/bar") {
      // Do something
    } ~
    path("/quux") {
      // Do something else
    }
  }
}
{% endhighlight %}

Note we use the `~` operator to indicate branching.

In our case we want to match the path `/add` or `/mulitply`. Furthermore, we expect each path to be followed by two numbers, which we want to extract. In the above example we passed a string to `path`. The `path` combinator allows more flexible input. If we want to extract part of a path we can name those parts with symbols. We combine parts with the '/' method. The extracted values are then made available in the `parameters` map. For example, for `add` we could write:

{% highlight scala %}
path("/add" / 'number1 / 'number2) { request =>
  val number1 = request.parameters.get('number1)
  val number2 = request.parameters.get('number2)
  // Do something
}
{% endhighlight %}

The values we get back from `parameters.get` are `Option`s. Typically we'll use a `for` comprehension to extract them:

{% highlight scala %}
path("/add" / 'number1 / 'number2) { request =>
  for {
    number1 <- request.parameters.get('number1)
    number2 <- request.parameters.get('number2)
  } yield // Do something
}
{% endhighlight %}

This is a bit annoying. A better solution is in development.

We're now ready to write the `add` request handler. We simply need to convert `number1` and `number2` to numbers (let's use Ints). If this is successful we'll return the sum, otherwise we'll return an appropriate error. Here's the code:

{% highlight scala %}
path("/add" / 'number1 / 'number2) {
  try {
    val sum =
      for {
        number1 <- request.parameters.get('number1).toInt
        number2 <- request.parameters.get('number2).toInt
      } yield (number1 + number2).toString

    Promise success {
        HttpResponse[ByteChunk](content = Some(sum.toString))
    }
  } catch {
      case e: NumberFormatException =>
        Promise success {
          HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
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

Finally notice we wrap our responses in futures. Remember request handlers return futures of responses, so it is necessary to perform this wrapping.

#### Chaining Together Request Handlers

We've implemented one of the two request handlers. The code for `multiply` is a straightforward adaptation of `add`. However we now need to chain together these two handlers. BlueEyes also provides a `~` method that does this. With this addition we can write our request handlers as

{% highlight scala %}
path("/add" / 'number1 / 'number2) {
  ... // rest of add code goes here
} ~
path("/multiply" / 'number1 / 'number2) {
  try {
    val product =
      for {
        number1 <- request.parameters.get('number1).toInt
        number2 <- request.parameters.get('number2).toInt
      } yield (number1 * number2).toString

    Promise success {
      HttpResponse[ByteChunk](content = Some(product.toString))
    }
  } catch {
      case e: NumberFormatException =>
        Promise success {
          HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
        }
  }
}
{% endhighlight %}

### The Complete Service

The complete code for the calculator service is:

{% highlight scala %}
import akka.Promise
import blueeyes.BlueEyesServiceBuilder
import blueeyes.core.http.{HttpRequest, HttpResponse, HttpStatus}
import blueeyes.core.http.HttpStatusCodes._
import blueeyes.core.data.{ByteChunk, BijectionsChunkString}

trait CalculatorService extends BlueEyesServiceBuilder with BijectionsChunkString {
  val calculatorService = service("calculatorService", "1.0.0") {
    context =>
      startup {
        Promise.success{()}
      } ->
      request { config: Unit =>
        path("/add" / 'number1 / 'number2) {
          try {
            val sum =
              for {
                number1 <- request.parameters.get('number1).toInt
                number2 <- request.parameters.get('number2).toInt
              } yield (number1 + number2).toString

           Promise success {
             HttpResponse[ByteChunk](content = Some(sum.toString))
           }
          } catch {
              case e: NumberFormatException =>
                Promise success {
                  HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
                }
          }
        } ~
        path("/multiply" / 'number1 / 'number2) {
          try {
            val product =
              for {
                number1 <- request.parameters.get('number1).toInt
                number2 <- request.parameters.get('number2).toInt
              } yield (number1 * number2).toString

            Promise success {
              HttpResponse[ByteChunk](content = Some(product.toString))
            }
          } catch {
              case e: NumberFormatException =>
                Promise success {
                  HttpResponse[ByteChunk](status = HttpStatus(BadRequest))
                }
          }
        }
      } ->
      shutdown { config =>
        println("Shutting down")
        Promise.success{()}
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

## Running A Server

Most environments provide some way to run a class with a `main` method. You can use this to test your server. For example, from within `sbt` you issue the command

{% highlight bash %}
> run CalculatorService --configFile /path/to/test.config
{% endhighlight %}

In a [later section](running.html) we'll talk about how to package your complete service into one JAR file that you can upload to a production server.

## Next Steps

We've build a complete service, showing the basic elements of using BlueEyes. The complete code for this example service is available [on Github](https://github.com/noelwelsh/making-eyes/tree/master/_calculator-service)

Of course there is a lot more to BlueEyes. The later chapters go into more depth on building services, as well as covering areas such as testing and persistence that we haven't touched on here.
