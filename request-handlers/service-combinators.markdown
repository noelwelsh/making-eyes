---
layout: page
title: Service Combinators
---

Service combinators are filters that process a request and decide to continue processing or to reject the request. We'll discuss first how to use combinator, then the predefined combinators, and then how to build our own.

## Using Combinators

All combinators follow the same pattern. If we want to take an action when a combinator matches, we nest that action inside the combinator. For example, to filter a path and then a `GET` request, we could write

{% highlight scala %}
path("/foo/bar") {
  get {
    // Do something
  }
}
{% endhighlight %}

At some point the filtering is complete and can we write a service handler: a function from request to response. To continue the above example, we could write

{% highlight scala %}
path("/foo/bar") {
  get {
    (req: HttpRequest[ByteChunk]) => Future { HttpResponse[ByteChunk]() }
  }
}
{% endhighlight %}

This would match `GET` requests for the path `/foo/bar` and return an empty `OK` response.

To indicate alternative filters we join them with `~`

{% highlight scala %}
path("/foo/bar") {
  // Do something if we match /foo/bar
} ~
path("/foo/baz") {
  // Do something if we match /foo/baz
}
{% endhighlight %}

## Predefined Combinators

BlueEyes predefined combinators are all provided by `blueeyes.core.service.HttpRequestHandlerCombinators`. `BlueEyesServiceBuilder` extends `HttpRequestHandlerCombinators`.

### HTTP Method

The most basic combinators match the HTTP request methods. There is a combinator for each HTTP method: `get`, `post`, `put`, `head`, `delete`, `patch`, `options`, `trace`, and `connect`. Their use is straightforward. For example, to match a `get` request:

{% highlight scala %}
get {
  // Do something after matching a GET request
}
{% endhighlight %}

### Path

Almost all services will want to dispatch on the path of the request, and BlueEyes provides a flexible `path` combinator just for this case. The most basic usage is to match a literal path. For example, to match the path `/api/v1/login` we'd write

{% highlight scala %}
path("/api/v1/login") {
  // Login here
}
{% endhighlight %}

Sometimes we want to capture any value that occurs in a particular position in a path. For example, we might want to match paths of the form `/user/<userId>` and make the user ID available for later processing. To do this we combine a symbol with a path using the `/` operator. The captured value will be available in the `parameter` map in the request:

{% highlight scala %}
path("/user" / 'userId) {
  (req: HttpRequest[ByteChunk]) => {
    val userId = req.parameters('userId) // Get the user Id
    // Do something
  }
}
{% endhighlight %}

We could also write the path above as

{% highlight scala %}
path("/user/'userId") {
  // Do something
}
{% endhighlight %}

for a slightly more compact notation.

Note that symbols do no match path separators or periods.

Finally we can match regular expressions, and also capture path fragments matching regular expressions. Any part of a string wrapped in parentheses is treated as a regular expression, and named capture groups are placed in the `parameter` map as with symbols.

To match a telephone extension of two digits in a URL we could write

{% highlight scala %}
path("/extension/([0-9]{2})") {
  // Do something
}
{% endhighlight %}

To capture the extension with the name `extension` we could write

{% highlight scala %}
path("/extension/(?<extension>[0-9]{2})") {
  // Do something
}
{% endhighlight %}


### Content Types

Handling content types is a major part of many web services. BlueEyes provides the `accepts`, `produces`, and `contentType` combinators to respectively filter incoming requests by the `Content-Type` they contain, ensure outgoing responses have a correct `Content-Type` header, and both filter incoming requests and set the `Content-Type` header on responses.

All these combinators take a MIME type as an argument. In `blueeyes.core.http.MimeTypes._` a large number of MIME types are defined, so you can just write, say, `application/json` or `audio/mp3` or `application/pdf` for your MIME type. Note that main MIME types (`application`, `text`, and so on) are objects, with a `/` method that takes a subtype (`json`, `pdf`, and so on). Thus you can compose MIME types out of fragments if the need arises.

A few examples are in order. To filter requests that contain `application/json` content we can write

{% highlight scala %}
accepts(application/json) {
  // Do something
}
{% endhighlight %}

To ensure responses have a `Content-Type` header set to `text/html` the following suffices:

{% highlight scala %}
produces(text/html) {
  // Do something
}
{% endhighlight %}

To both filter requests containing JSON content, and ensure responses have a `Content-Type` header set to `application/json` we would write

{% highlight scala %}
contentType(application/json) {
  // Do something
}
{% endhighlight %}

These combinators all have an implicit bijection parameter. The intention is to transcode the content to the apropriate type, but there is no association between the `MimeType` passed to the combinator and the bijection. This means, for example, you could specify `application/json` as the MIME type, but treat the content as XML. Since this is usually an error, two special purpose combinators are provided. `jvalue` and `xml` act like the `contentType` combinator but set the type of the bijection appropriately. You still need to make the correct bijection available to use these combinators.

TODO: service, $, orFail, commit, compress, aggregate, jsonp
