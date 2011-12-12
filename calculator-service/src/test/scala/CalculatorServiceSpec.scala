import blueeyes.core.service.test.BlueEyesServiceSpecification
import blueeyes.core.http.HttpResponse

class CalculatorServiceSpec extends BlueEyesServiceSpecification with CalculatorService {
  "Calculator.add" should {
    "return with the sum of its inputs" in {
      val future = service.get[String]("/add/1/2")

      future must whenDelivered {
        response => response must beLike {
          case HttpResponse(status, _, Some(content), _) =>
            content mustEqual "3.0"
          case _ => ko
        }
      }
    }
  }
}

