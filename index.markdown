# Making Web Services with BlueEyes

[BlueEyes](https://github.com/jdegoes/blueeyes) is a simple web framework for Scala, aimed at producing high-performance REST services. BlueEyes differs from most Scala web frameworks by:

  - building on top of [Netty](http://www.jboss.org/netty), providing highly scalable services out of the box
  - using an asynchronous model throughout, further enabling high scalability
  - deploying as simple command line executables -- not need to configure and manage servlet containers
  - leveraging Scala's type system and avoiding reflection
  - focusing exclusively on REST services

This short book describes how to build scalable web services with BlueEyes.

## Core Concepts and a Quick Introduction

The *Service* is the core concept in BlueEyes. At it's heart a service is a set of related *request handlers*. A request handler is just a function, bound to a URL, from a HTTP request to a HTTP responsse.

Typically a service needs some configuration parameters (for example, the database to use), and will need to create (and later destroy) some resources, such as a connection to the database, before it runs. In BlueEyes the configuration parameters are called the service's *context*, and the request handlers plus their startup and shutdown functions are called a *service descriptor*.

Let's look at how to build a very simple service in BlueEyes.

### Building a Service

The simplest way to construct a REST service is to extend `blueeyes.BlueEyesServiceBuilder` and call the `service` function:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait MyService extends BlueEyesServiceBuilder {
  val myService = service("name", "version") {
    ... // service descriptor goes here
  }
}
{% endhighlight %}

### Service

The `service` function takes a `name`, `version`, and a function from a context to a service descriptor. The `name` and the `version` are strings. The name can be anything you want, while the version number should be a version number like `"1.0.0"`:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait MyService extends BlueEyesServiceBuilder {
  val myService = service("myService", "1.0.0") {
    context => // service descriptor
  }
}
{% endhighlight %}


### The Service Descriptor

The service descriptor is created by chaining together functions to handle `startup`, `requests`, and `shutdown`:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait MyService extends BlueEyesServiceBuilder {
  val myService = service("myService", "1.0.0") {
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

trait MyService extends BlueEyesServiceBuilder {
  val myService = service("myService", "1.0.0") {
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


## Configuration

Configuation is done via the context passed to service. This context (an instance of  `blueeyes.core.service.HttpServiceContext`) has a attribute called `config` which is an instance of a [Configgy](https://github.com/robey/configgy) `ConfigMap`.

You can pass in a `--configFile` option on the command line, or construct a `ConfigMap` in code:

{% highlight scala %}
import net.lag.configgy.Configgy

Configgy.configure("/etc/my-service.conf")
val config = Configgy.config
{% endhighlight %}


## HTTP Pattern Matching

Pattern matching on HTTP requests is done using the functions defined in `blueeyes.core.service.HttpRequestHandlerCombinators`. `BlueEyesServiceBuilder` extends `HttpRequestHandlerCombinators`.

### contentType

This combinator specifies that the service consumes *and* produces content of the given MIME type. Many common MIME types are bound to values, so you can write just, say, `application/json` rather than constructing a `MimeType` object yourself.

To access these import `blueeyes.core.http.MimeTypes._`


### Request Methods

The common HTTP request methods `get`, `post`, `put`, and `head`, as well as less common (nonstandard?) methods `delete`, `patch`, `options`, `trace`, and `connect` are specified as combinators.


## Mongo Integration

Extend the `ConfigurableMongo` trait from `blueeyes.persistence.mongo`. Then in the  `startup` method, extract the mongo configuration from your configuration and pass it to the `mongo` function to construct a facade through which you can access Mongo.

{% highlight scala %}
service { context =>
  startup {
    val mongoConfig = context.config.configMap("mongo")
    // In real code you'd put mongoConfig into some data structure you can access from request
    mongoConfig
   } ->
   ...
}
{% endhighlight %}

The configuration elements are `mongo.mock`, a boolean indicating if a mock or real Mongo should be used, and `dropBeforeStart`, which is a map specifying databases to drop.

