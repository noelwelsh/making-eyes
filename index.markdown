---
layout: page
title: The Lowdown on BlueEyes
---

This short book describes [BlueEyes](https://github.com/jdegoes/blueeyes), a simple Scala web framework for building high-performance REST services. BlueEyes might be an appropriate choice for your project if:

- you want to build a REST service. You're happy to respond to requests with JSON data, for example, and don't need a templating engine.
- performance is a concern. BlueEyes is built from the ground up on an asynchronous model, starting with [Netty](http://www.jboss.org/netty). This means it can handle a large number of simultaneous connections with ease.
- you want to leverage Scala's type system to catch errors at compile time.
- you want fast development, a quick testing cycle, and simple deployment.


## Table of Contents

- [A quick introduction to BlueEyes](intro.html), in which we build a basic web service.
- [More on services](services.html)
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
- Comet
- Http Client
- Testing
- Deployment
   - OneJar
   - RSync
   - HAProxy
- Concurrency in BlueEyes
