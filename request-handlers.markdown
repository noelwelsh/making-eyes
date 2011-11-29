---
layout: page
title: Building Request Handlers
---

In the [introduction](intro.html) we saw the basics of constructing REST services in BlueEyes.

## HTTP Pattern Matching

Pattern matching on HTTP requests is done using the functions defined in `blueeyes.core.service.HttpRequestHandlerCombinators`. `BlueEyesServiceBuilder` extends `HttpRequestHandlerCombinators`.

### contentType

This combinator specifies that the service consumes *and* produces content of the given MIME type. Many common MIME types are bound to values, so you can write just, say, `application/json` rather than constructing a `MimeType` object yourself.

To access these import `blueeyes.core.http.MimeTypes._`


### Request Methods

The common HTTP request methods `get`, `post`, `put`, and `head`, as well as less common (nonstandard?) methods `delete`, `patch`, `options`, `trace`, and `connect` are specified as combinators.

## The HttpRequestHandler Types

## Bijections
