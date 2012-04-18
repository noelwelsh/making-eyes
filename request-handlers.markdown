---
layout: page
title: Building Request Handlers
---

In the [introduction](intro.html) we saw the basics of constructing REST services in BlueEyes. In this section we're going to go into detail into the major components that make up a service:

- [requests and responses](request-handlers/bijections.html) are clearly essential for building services. The main point here is to understand how BlueEyes converts the body of requests and responses from data read over the wire to parsed formats we can work with;
- [service combinators](request-handlers/service-combinators.html) are the DSL we use to take apart requests and decide if we want to handle them; and
- [service handlers](request-handlers/service-handlers.html) are ...
