import blueeyes.core.service.test.BlueEyesServiceSpecification
import blueeyes.core.http.HttpResponse

class CalculatorServiceSpec extends BlueEyesServiceSpecification with CalculatorService {
  "Calculator.add" should {
    "return with the sum of its inputs" in {
      val future = service.get[String]("/add/1/2")

      future must whenDelivered {
        (response: HttpResponse[String]) => response must beLike {
          case HttpResponse(status, _, Some(content), _) =>
            content mustEqual "3"
          case _ => ko
        }
      }
    }

    "return with the product of its inputs" in {
      val future = service.get[String]("/multiply/3/4")

      future must whenDelivered {
        (response: HttpResponse[String]) => response must beLike {
          case HttpResponse(status, _, Some(content), _) =>
            content mustEqual "12"
          case _ => ko
        }
      }
    }
  }
}
