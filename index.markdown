# Making Eyes At BlueEyes

[BlueEyes](https://github.com/jdegoes/blueeyes) is a simple web framework for Scala. It provides

   - Easy construction of REST services
   - Asynchronous, and thus highly scalable, processing by default


## Building a Service

The simplest way to construct a REST service is to extend `blueeyes.BlueEyesServiceBuilder`:

{% highlight scala %}
import blueeyes.BlueEyesServiceBuilder

trait MyService extends BlueEyesServiceBuilder {
  ...
}
{% endhighlight %}

### Service

The `service` function takes a `name`, `version`, and a function from a context to a service descriptor.

### Startup

Startup returns a `Future`, which is complete when startup has finished. There is an implicit `.future` to convert any value to a future.


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

