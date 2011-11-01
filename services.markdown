---
layout: page
title: Building Services
---

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

