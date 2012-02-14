---
layout: page
title: Deploying Services in Production
---

At some point you'll want to move your BlueEyes application to a live environment. There are two main options: generate a stand-alone `JAR` file that can be run from the command line using [Netty](http://www.jboss.org/netty) as the server, or deploy to servlet container that implements the Servlet 3.0 API.

## Standalone JAR

Creating a `JAR` file containing the code and all it's dependencies is the simplest way to do this. Then you can just run

    java -jar myAwesomeApp.jar

and it will Just Work. Installing the [OneJar SBT plugin](https://github.com/retronym/sbt-onejar/) is the simplest way to do this.


## Servlet Deployment

If you already have invested in setting up a servlet container, or you're using a cloud provider that only supports servlets, this is the route you'll want to take. Note your servlet container must support version 3.0 of the Servlet API.

You'll need to make a code change to go down this route. Instead of extending `BlueEyesServer`, your server should now extend `ServletServer`. For example:

{% highlight scala %}
import BlueEyes.ServletServer

object CalculatorServer extends ServletServer with CalculatorService
{% endhighlight %}

You'll then need to create a `web.xml` like the following:

{% highlight xml %}
<servlet>
  <servlet-name>Calculator Server</servlet-name>
  <servlet-class>services.servlet.engines.SampleServer</servlet-class>
  <async-supported>true</async-supported>
  <init-param>
    <param-name>configFile</param-name>
    <param-value>path/to/production.config</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
{% endhighlight %}

Note the `async-support` and `init-param` settings.
