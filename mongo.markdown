---
layout: page
title: BlueEyes' Mongo Integration
---

## Mongo Integration

Extend the `ConfigurableMongo` trait from `blueeyes.persistence.mongo`. Then in the  `startup` method, extract the mongo configuration from your configuration and pass it to the `mongo` function to construct a facade through which you can access Mongo.

{% highlight scala %}
service { context =>
  startup {
    val mongoConfig = context.config.configMap("mongo")
    // In real code you'd put mongoConfig into some data structure you can access from request
    mongoConfig
   } ->
   ...
}
{% endhighlight %}

The configuration elements are `mongo.mock`, a boolean indicating if a mock or real Mongo should be used, and `dropBeforeStart`, which is a map specifying databases to drop.

