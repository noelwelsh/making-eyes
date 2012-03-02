---
layout: page
title: Building Request Handlers
---

In the [introduction](intro.html) we saw the basics of constructing REST services in BlueEyes. In this section we're going to go into detail into the major components that make up a service: requests and responses, service combinators, and service handlers. This will

## Requests, Responses, and Bijections

The [`HttpRequest`](https://github.com/jdegoes/blueeyes/blob/master/core/src/main/scala/blueeyes/core/http/HttpRequest.scala) and [`HttpResponse`](https://github.com/jdegoes/blueeyes/blob/master/core/src/main/scala/blueeyes/core/http/HttpResponse.scala) classes, both in the `blueeyes.core.http` package, are the basic types of requests and responses. Both are fairly simple case classes. Requests and responses are both parameterised by the type of the content they hold, and it here's where there is a bit of subtlety.

The types of content are all defined in `blueeyes.core.data`. The most basic type is `ByteChunk`, and it is this that is eventually sent and received on the network. A `ByteChunk` is a lazy linked list of byte arrays. Let's unpack this statement. For efficiency, data is read or written in chunks, which are represented as arrays of bytes. Since all the data may not fit in a chunk, we need to maintain a list of chunks, ordering them from beginning to end. Since we want to get on with processing data before the entire contents has been read, the list is lazy. That is, the next chunk in the chain is represented by the ubiquitous `Future`. In fact it is an `Option` of a `Future`, as the there may not be a next chunk -- the current chunk might be the last one. All said that gives us a representation that looks like this:

{% highlight scala %}
case class ByteChunk(data: Array[Byte], next: Option[Future[ByteChunk]])
{% endhighlight %}

The actual `ByteChunk` is implemented in terms of a `Chunk` class, but the above is sufficient for our purposes.

So we've looked at chunks. Now what about data types we might actually be interested in, like JSON, or XML, or good old strings? BlueEyes provides functions for converting between `ByteChunk`s and more useful data types. These conversion functions are called *bijections*, which is the fancy term for a function that has an inverse. In our case this means that `BijectionsChunkJson`, for example, contains a function for converting a `ByteChunk` to a JSON value, and a function for converting JSON back to `ByteChunk`. As you've probably guessed these functions are for the request and response sides respectively. Bijections are mostly used as implicit parameters, but occasionally you'll need to explicitly apply them.

There is one last detail before we have the complete picture. Recall the a `ByteChunk` is a lazy list, as we want to get on with processing before the data is all available. The same applies to other data types. On the input side the data will typically be wrapped in a `Future` So rather than dealing with, say, an `HttpRequest[String]` we'll have a `HttpRequest[Future[String]]`. On the output side the entire response is wrapped in a `Future`. That is, we'll return a `Future[HttpResponse[String]]` rather than just a `HttpResponse[String]`.

In summary:

- `HttpRequest` and `HttpResponse` are the basic types for requests and responses.
- Both requests and responses are parameterised by the type of their content.
- `ByteChunk` is the basic type for all requests and responses. All content must eventually be converted from or to `ByteChunk`.
- Bijections such as `BijectionsChunkJson` and `BijectionsChunkFutureJson` provide functions to convert between `ByteChunk` and more useful types.
- After converting a `ByteChunk` to a type `A`, a request will be of type `HttpRequest[Future[A]]`. A response will be of type `Future[HttpResponse[A]]`.

### Using Bijections




## Service Combinators

## HTTP Pattern Matching

Pattern matching on HTTP requests is done using the functions defined in `blueeyes.core.service.HttpRequestHandlerCombinators`. `BlueEyesServiceBuilder` extends `HttpRequestHandlerCombinators`.

### contentType

This combinator specifies that the service consumes *and* produces content of the given MIME type. Many common MIME types are bound to values, so you can write just, say, `application/json` rather than constructing a `MimeType` object yourself.

To access these import `blueeyes.core.http.MimeTypes._`


### Request Methods

The common HTTP request methods `get`, `post`, `put`, and `head`, as well as less common (nonstandard?) methods `delete`, `patch`, `options`, `trace`, and `connect` are specified as combinators.

## The HttpRequestHandler Types

## Bijections
