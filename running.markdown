---
layout: page
title: Running Services
---

## Configuration

Configuation is done via the context passed to service. This context (an instance of  `blueeyes.core.service.HttpServiceContext`) has a attribute called `config` which is an instance of a [Configrity](https://github.com/paradigmatic/Configrity) `Configuration`.

You can pass in a `--configFile` option on the command line, or construct a `Configuration` in code:

{% highlight scala %}
import org.streum.configrity._

val config = Configuration.load("/etc/my-service.conf")
{% endhighlight %}


## One JAR Deployment

One JAR SBT plugin

RSync
