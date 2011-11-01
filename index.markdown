---
layout: page
title: Making Web Services with BlueEyes
---

[BlueEyes](https://github.com/jdegoes/blueeyes) is a simple web framework for Scala, aimed at producing high-performance REST services. BlueEyes differs from most Scala web frameworks by:

- using an asynchronous model throughout, starting with [Netty](http://www.jboss.org/netty) rather than a servlet container.
- leveraging Scala's type system and avoiding reflection.
- focusing exclusively on REST services.

This short book describes how to build scalable web services with BlueEyes.

## Table of Contents

- [A quick introduction to BlueEyes](intro.html), in which we build a basic web service.
- More on services
   - Bijections and content types
   - Request parsing combinators
   - HttpRequestHandler types
   - Configuration 
   - Augmenting services (health monitor, logging, etc.)
- Manipulating JSON
   - Constructing JSON using the DSL
   - Extracting elements from JSON
   - JSON data type representation
- Mongo integration
- Http Client
- Testing
- Deployment
   - OneJar
   - RSync
   - HAProxy
- Concurrency in BlueEyes
