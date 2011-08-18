# Testing BlueEyes Services

BlueEyes testing framework supports `Specs`, so the first thing is to add that to your project:

{% highlight scala %}
libraryDependencies ++= Seq(
  ...
  "org.scala-tools.testing"     % "specs_2.9.0-1"              % "1.6.8"
  ...
)
{% endhighlight %}

To write a test create a class that extends both the service under test and `blueeyes.core.service.test.BlueEyesServiceSpecification`. For example:

{% highlight scala %}
import blueeyes.core.service.test.BlueEyesServiceSpecification

class DataServicesSpec extends BlueEyesServiceSpecification with DataServices {
  ...
}
{% endhighlight %}

The `BlueEyesServiceSpecification` mix-in defines a `service` method from which you can start testing. The value returned by `service` is a BlueEyes `HttpClient` (with some special sauce to integrate with Specs) so all the methods defined on `HttpClient` work here.

The methods of the `HttpClient` typically return a `Future`.  To wait till the future is ready you typically do:

{% highlight scala %}
  val future = service.get("/some/url")
  future.value must eventually(beSomething)
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
