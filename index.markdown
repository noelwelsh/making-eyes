---
layout: page
title: The Lowdown on BlueEyes
---

This short book describes [BlueEyes](https://github.com/jdegoes/blueeyes), a simple Scala web framework for building high-performance REST services. BlueEyes might be an appropriate choice for your project if:

- you want to build a REST service. You're happy to respond to requests with JSON data, for example, and don't need a templating engine.
- performance is a concern. BlueEyes is built from the ground up on an asynchronous model, starting with [Netty](http://www.jboss.org/netty) and [Akka](http://akka.io). This means it can handle a large number of simultaneous connections with ease.
- you want to leverage Scala's type system to catch errors at compile time.
- you want fast development, a quick testing cycle, and simple deployment.

This book should provide everything you need to develop in BlueEyes and deploy to production environments using many machines.

This book covers version *0.6* of BlueEyes, currently `0.6.0-SNAPSHOT`. I assume you know how to use Scala, and are comfortable with your development environment of choice. Certain aspects of this book, such as the section on deployment, go beyond pure software development. When I discuss build systems, I give examples for SBT. When discussing system administration I assume a Unix-like system.

This book is still a work in progress, and all feedback is greatly appreciated!

## News

- *0.2* Update code samples and prose to `0.6.0-SNAPSHOT`. All samples are tested.
- *0.1* Initial release.
