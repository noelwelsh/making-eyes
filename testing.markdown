---
layout: page
title: Testing BlueEyes Services
---

BlueEyes provides some custom additions to the `Specs` testing framework to allow you to easily test services. At the time of writing the current BlueEyes release (the 0.4 series) supports `Specs`. The next major release (0.5) will support `Specs 2`.

The first step is to add Specs to your project:

{% highlight scala %}
libraryDependencies ++= Seq(
  ...
  "org.scala-tools.testing"     % "specs_2.9.0-1"              % "1.6.8"
  ...
)
{% endhighlight %}

<div class="aside">
##Using Specs

If you haven't used specs before, here's a lightning quick introduction.
</div>


To test a service, create a class that extends both the service under test and `blueeyes.core.service.test.BlueEyesServiceSpecification`. For example, to test the `CalculatorService` we developed in the [introduction](intro.html):

{% highlight scala %}
import blueeyes.core.service.test.BlueEyesServiceSpecification

class CalculatorServiceSpec extends BlueEyesServiceSpecification with CalculatorService {
  ...
}
{% endhighlight %}

The `BlueEyesServiceSpecification` mix-in defines a `service` method with is a custom `HttpClient` that:

- connects directly to the service without going over the network (thus making testing aster);
- adds some special sauce to integrate with Specs. 

This client acts just the the `HttpClient` we described in the chapters on [consuming REST services](http-client.html). The methods of the `HttpClient` typically return a `Future`. `BlueEyesServiceSpecification` also defines a custom matcher, `whenDelivered` that tries a given matchers against a future's value when it is delivered (or times out waiting). Together this gives us all we need to test services.

Returning to the `CalculatorService` example, lets' test the `add` endpoint. All we need to do is pass the correct input in using the `HttpClient` that `service` gives us, and check the response is what we expect:

{% highlight scala %}
"Calculator.add" should {
  "respond with the sum of its inputs" in {
    service.get[String]("/add/1/2") must whenDelivered {
      response => response.content must beSome("3")
    }
  }
}
{% endhighlight %}

Sometimes it is inconvenient to manipulate the future's value within the `whenDelivered` block. In this case we can use a simpler method when we ask Specs to ensure the future has a value, and, if it does, manipulate that value directly"

{% highlight scala %}

{% endhighlight %}

The `must`, `eventually` and `beSomething` functions are defined by Specs. See it's documentation for more details. Once this test has passed, and we know `future` has a value, we can get it with `future.value.get`.


## Configuring Tests

Typical services need configuration to run, so our tests must provide that configuration. Rather than juggle configuration files for testing and live deployment, `BlueEyesServiceSpecification` allows the configuration file to be defined inline (in Configgy syntax). To do so, simply override the `configuration` field specifying the configuration as a `String`:

{% highlight scala %}
  override def configuration = """
    services {
      data {
        v0.1 {
          mongo {
            servers = ["localhost"]
            database = "myna"
            collections = "signups"

            dropBeforeStart {
              myna = ["signups"]
            }
          }
        }
      }
    }
  """
{% endhighlight %}
