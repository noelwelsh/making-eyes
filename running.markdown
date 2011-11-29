---
layout: page
titie: Running Services
---

## Configuration

Configuation is done via the context passed to service. This context (an instance of  `blueeyes.core.service.HttpServiceContext`) has a attribute called `config` which is an instance of a [Configgy](https://github.com/robey/configgy) `ConfigMap`.

You can pass in a `--configFile` option on the command line, or construct a `ConfigMap` in code:

{% highlight scala %}
import net.lag.configgy.Configgy

Configgy.configure("/etc/my-service.conf")
val config = Configgy.config
{% endhighlight %}


## One JAR Deployment

One JAR SBT plugin

RSync
